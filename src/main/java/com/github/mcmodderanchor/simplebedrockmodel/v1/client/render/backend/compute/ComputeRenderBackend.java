package com.github.mcmodderanchor.simplebedrockmodel.v1.client.render.backend.compute;

import com.github.mcmodderanchor.simplebedrockmodel.SimpleBedrockModel;
import com.github.mcmodderanchor.simplebedrockmodel.v1.client.render.backend.BedrockRenderBackend;
import com.github.mcmodderanchor.simplebedrockmodel.v1.client.render.backend.BedrockRenderRequest;
import com.github.mcmodderanchor.simplebedrockmodel.v1.client.render.backend.mesh.*;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockModel;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL43;
import org.lwjgl.system.MemoryUtil;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.WeakHashMap;

/**
 * Compute Shader 渲染后端。
 * <p>
 * 使用 compute shader 将 bind-pose 静态顶点按骨骼矩阵变形，
 * 输出到 NEW_ENTITY 格式的 VBO，然后用原版 RenderType/Shader 绘制。
 */
public final class ComputeRenderBackend implements BedrockRenderBackend {

    private static final String SHADER_PATH = "/assets/simplebedrockmodel/shaders/compute/bedrock_skin.comp";
    private static final int WORKGROUP_SIZE = 64;

    @Nullable
    private ComputeProgram computeProgram;
    private boolean initialized = false;
    private boolean initFailed = false;

    /** 每个模型实例的 GPU 资源缓存 */
    private final WeakHashMap<BedrockModel, GpuMeshHandle> gpuHandles = new WeakHashMap<>();

    /** 可复用的骨骼调色板数组 */
    private Matrix4f[] paletteTransforms;
    private Matrix3f[] paletteNormals;
    private boolean[] paletteVisibility;
    private int paletteCapacity = 0;

    @Override
    public boolean render(BedrockRenderRequest request) {
        if (!ensureInitialized()) {
            return false;
        }

        BedrockModel model = request.model();

        // 获取或构建扁平化网格
        FlattenedModelMesh mesh = ModelMeshCache.getOrBuild(model);
        if (mesh == null || mesh.isEmpty()) {
            return false;
        }

        // 获取或创建 GPU 资源
        GpuMeshHandle handle = gpuHandles.get(model);
        if (handle == null) {
            try {
                handle = GpuMeshHandle.create(mesh);
                gpuHandles.put(model, handle);
            } catch (Exception e) {
                SimpleBedrockModel.LOGGER.error("Failed to create GpuMeshHandle", e);
                return false;
            }
        }

        // 确保骨骼调色板数组容量足够
        ensurePaletteCapacity(mesh.boneCount());

        // 步骤 3：构建骨骼 palette
        BonePaletteBuilder.buildPalette(model, mesh, paletteTransforms, paletteNormals, paletteVisibility);

        // 步骤 4：上传骨骼 palette 到 GPU
        uploadPalette(handle, mesh.boneCount());

        // 步骤 5：上传 instance params
        uploadInstanceParams(handle, request);

        // 步骤 6：推进 ring buffer
        handle.advanceRing();

        // 步骤 7：dispatch compute
        computeProgram.bind();
        GL43.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 0, handle.sourceSSBO());
        GL43.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 1, handle.paletteSSBO());
        GL43.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 2, handle.instanceParamsSSBO());
        GL43.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 3, handle.currentDrawVBOId());

        int numGroups = (handle.vertexCount() + WORKGROUP_SIZE - 1) / WORKGROUP_SIZE;
        GL43.glDispatchCompute(numGroups, 1, 1);

        ComputeProgram.unbind();

        // 步骤 8：memory barrier
        GL42.glMemoryBarrier(GL43.GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT);

        // 步骤 9：用原版 RenderType 绘制
        RenderType renderType = request.renderType();
        renderType.setupRenderState();

        VertexBuffer drawBuffer = handle.currentDrawBuffer();
        drawBuffer.bind();

        ShaderInstance shader = RenderSystem.getShader();
        if (shader != null) {
            drawBuffer.drawWithShader(
                    RenderSystem.getModelViewMatrix(),
                    RenderSystem.getProjectionMatrix(),
                    shader
            );
        }

        VertexBuffer.unbind();
        renderType.clearRenderState();

        return true;
    }

    @Override
    public void onResourceReload() {
        // 清理所有 GPU 句柄，下次使用时重建
        for (GpuMeshHandle handle : gpuHandles.values()) {
            if (handle != null) handle.close();
        }
        gpuHandles.clear();
        ModelMeshCache.clear();
    }

    @Override
    public void close() {
        for (GpuMeshHandle handle : gpuHandles.values()) {
            if (handle != null) handle.close();
        }
        gpuHandles.clear();

        if (computeProgram != null) {
            computeProgram.close();
            computeProgram = null;
        }

        initialized = false;
        initFailed = false;
    }

    // ---- 内部方法 ----

    private boolean ensureInitialized() {
        if (initialized) return computeProgram != null && computeProgram.isValid();
        if (initFailed) return false;

        initialized = true;

        // 能力探测
        if (!ComputeCapabilityProbe.probe()) {
            SimpleBedrockModel.LOGGER.warn("Compute backend disabled: {}", ComputeCapabilityProbe.getUnsupportedReason());
            initFailed = true;
            return false;
        }

        // 加载并编译 compute shader
        String source = loadShaderSource();
        if (source == null) {
            initFailed = true;
            return false;
        }

        computeProgram = ComputeProgram.compile("bedrock_skin", source);
        if (computeProgram == null) {
            initFailed = true;
            return false;
        }

        return true;
    }

    @Nullable
    private String loadShaderSource() {
        try (InputStream is = getClass().getResourceAsStream(SHADER_PATH)) {
            if (is == null) {
                SimpleBedrockModel.LOGGER.error("Compute shader not found: {}", SHADER_PATH);
                return null;
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            SimpleBedrockModel.LOGGER.error("Failed to load compute shader", e);
            return null;
        }
    }

    private void ensurePaletteCapacity(int boneCount) {
        if (paletteCapacity < boneCount) {
            paletteTransforms = BonePaletteBuilder.allocateTransforms(boneCount);
            paletteNormals = BonePaletteBuilder.allocateNormals(boneCount);
            paletteVisibility = new boolean[boneCount];
            paletteCapacity = boneCount;
        }
    }

    private void uploadPalette(GpuMeshHandle handle, int boneCount) {
        // 每根骨骼 128 bytes: mat4(64) + 3xvec4(48) + vec4(16)
        int totalBytes = boneCount * 128;
        ByteBuffer buf = MemoryUtil.memAlloc(totalBytes);
        try {
            for (int i = 0; i < boneCount; i++) {
                Matrix4f t = paletteTransforms[i];
                Matrix3f n = paletteNormals[i];

                // mat4 transform (column-major)
                putMatrix4f(buf, t);

                // mat3 normal as 3 x vec4 (column-major, w=0 padding)
                buf.putFloat(n.m00); buf.putFloat(n.m01); buf.putFloat(n.m02); buf.putFloat(0);
                buf.putFloat(n.m10); buf.putFloat(n.m11); buf.putFloat(n.m12); buf.putFloat(0);
                buf.putFloat(n.m20); buf.putFloat(n.m21); buf.putFloat(n.m22); buf.putFloat(0);

                // vec4 flags: x = visible
                buf.putFloat(paletteVisibility[i] ? 1.0f : 0.0f);
                buf.putFloat(0); buf.putFloat(0); buf.putFloat(0);
            }
            buf.flip();
            GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, handle.paletteSSBO());
            GL15.glBufferSubData(GL43.GL_SHADER_STORAGE_BUFFER, 0, buf);
            GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, 0);
        } finally {
            MemoryUtil.memFree(buf);
        }
    }

    private void uploadInstanceParams(GpuMeshHandle handle, BedrockRenderRequest request) {
        ByteBuffer buf = MemoryUtil.memAlloc(160);
        try {
            // mat4 poseMatrix
            Matrix4f pose = request.poseStack().last().pose();
            putMatrix4f(buf, pose);

            // mat3 normalMatrix as 3 x vec4
            Matrix3f normal = request.poseStack().last().normal();
            buf.putFloat(normal.m00); buf.putFloat(normal.m01); buf.putFloat(normal.m02); buf.putFloat(0);
            buf.putFloat(normal.m10); buf.putFloat(normal.m11); buf.putFloat(normal.m12); buf.putFloat(0);
            buf.putFloat(normal.m20); buf.putFloat(normal.m21); buf.putFloat(normal.m22); buf.putFloat(0);

            // vec4 lightOverlay: packed as float bits
            buf.putFloat(Float.intBitsToFloat(request.packedLight()));
            buf.putFloat(Float.intBitsToFloat(request.packedOverlay()));
            buf.putFloat(0); buf.putFloat(0);

            // vec4 rgba
            buf.putFloat(request.red());
            buf.putFloat(request.green());
            buf.putFloat(request.blue());
            buf.putFloat(request.alpha());

            // vec4 uvRemap
            Material mat = request.material();
            if (mat != null) {
                TextureAtlasSprite sprite = mat.sprite();
                buf.putFloat(sprite.getU0());
                buf.putFloat(sprite.getV0());
                buf.putFloat(sprite.getU1() - sprite.getU0());
                buf.putFloat(sprite.getV1() - sprite.getV0());
            } else {
                // 无 remap：offset=0, scale=1
                buf.putFloat(0); buf.putFloat(0);
                buf.putFloat(1); buf.putFloat(1);
            }

            buf.flip();
            GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, handle.instanceParamsSSBO());
            GL15.glBufferSubData(GL43.GL_SHADER_STORAGE_BUFFER, 0, buf);
            GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, 0);
        } finally {
            MemoryUtil.memFree(buf);
        }
    }

    private static void putMatrix4f(ByteBuffer buf, Matrix4f m) {
        // Column-major order
        buf.putFloat(m.m00()); buf.putFloat(m.m01()); buf.putFloat(m.m02()); buf.putFloat(m.m03());
        buf.putFloat(m.m10()); buf.putFloat(m.m11()); buf.putFloat(m.m12()); buf.putFloat(m.m13());
        buf.putFloat(m.m20()); buf.putFloat(m.m21()); buf.putFloat(m.m22()); buf.putFloat(m.m23());
        buf.putFloat(m.m30()); buf.putFloat(m.m31()); buf.putFloat(m.m32()); buf.putFloat(m.m33());
    }
}

package com.github.mcmodderanchor.simplebedrockmodel.v1.client.render.backend.compute;

import com.github.mcmodderanchor.simplebedrockmodel.SimpleBedrockModel;
import com.github.mcmodderanchor.simplebedrockmodel.v1.client.render.backend.mesh.FlattenedModelMesh;
import com.github.mcmodderanchor.simplebedrockmodel.v1.client.render.backend.mesh.FlattenedVertex;
import com.github.mcmodderanchor.simplebedrockmodel.v1.mixin.client.VertexBufferAccessor;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.BufferBuilder;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL43;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

/**
 * 一个模型在 GPU 上的资源句柄。
 * <p>
 * 包含：
 * <ul>
 *   <li>source SSBO - 静态源顶点数据（bind pose 本地空间）</li>
 *   <li>draw VertexBuffer ring - 用于最终绘制的 VertexBuffer 环形缓冲</li>
 *   <li>palette SSBO - 动态骨骼矩阵缓冲</li>
 *   <li>instance params SSBO - 动态实例参数缓冲</li>
 * </ul>
 */
public final class GpuMeshHandle implements AutoCloseable {

    /** Source SSBO 中每个顶点的大小（字节）：
     *  vec4 localPos_boneIndex (16) + vec4 localNormal_flags (16) + vec2 uv (8) + 8 padding = 48 bytes
     *  std430 规则：struct array stride 必须 round up 到最大成员对齐(16)的倍数 */
    public static final int SOURCE_VERTEX_STRIDE = 48;

    /** NEW_ENTITY 格式每个顶点的大小（字节）：36 bytes (9 x 4) */
    public static final int DRAW_VERTEX_STRIDE = 36;

    /** Draw ring buffer 的槽数 */
    private static final int RING_SIZE = 3;

    private int sourceSSBO = -1;
    private int paletteSSBO = -1;
    private int instanceParamsSSBO = -1;
    private final VertexBuffer[] drawRing = new VertexBuffer[RING_SIZE];
    private final int[] drawVBOIds = new int[RING_SIZE];
    private int currentRingSlot = 0;

    private final int vertexCount;
    private final int boneCount;

    private GpuMeshHandle(int vertexCount, int boneCount) {
        this.vertexCount = vertexCount;
        this.boneCount = boneCount;
    }

    /**
     * 从扁平化网格创建 GPU 资源。必须在渲染线程上调用。
     */
    public static GpuMeshHandle create(FlattenedModelMesh mesh) {
        int vertexCount = mesh.vertexCount();
        int boneCount = mesh.boneCount();
        GpuMeshHandle handle = new GpuMeshHandle(vertexCount, boneCount);

        // 1. 创建 source SSBO
        handle.sourceSSBO = GL15.glGenBuffers();
        ByteBuffer sourceData = MemoryUtil.memAlloc(vertexCount * SOURCE_VERTEX_STRIDE);
        try {
            FlattenedVertex[] vertices = mesh.vertices();
            for (FlattenedVertex v : vertices) {
                // vec4 localPos_boneIndex: x, y, z, intBitsToFloat(boneIndex)
                sourceData.putFloat(v.posX);
                sourceData.putFloat(v.posY);
                sourceData.putFloat(v.posZ);
                sourceData.putFloat(Float.intBitsToFloat(v.boneIndex));
                // vec4 localNormal_flags: nx, ny, nz, intBitsToFloat(flags)
                sourceData.putFloat(v.normalX);
                sourceData.putFloat(v.normalY);
                sourceData.putFloat(v.normalZ);
                int flags = v.fullBright ? 1 : 0;
                sourceData.putFloat(Float.intBitsToFloat(flags));
                // vec2 uv
                sourceData.putFloat(v.u);
                sourceData.putFloat(v.v);
                // std430 struct array padding: pad SourceVertex from 40 to 48 bytes
                sourceData.putFloat(0.0f);
                sourceData.putFloat(0.0f);
            }
            sourceData.flip();
            GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, handle.sourceSSBO);
            GL15.glBufferData(GL43.GL_SHADER_STORAGE_BUFFER, sourceData, GL15.GL_STATIC_DRAW);
            GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, 0);
        } finally {
            MemoryUtil.memFree(sourceData);
        }

        // 2. 创建 palette SSBO
        // 每根骨骼：mat4 (64 bytes) + mat3 as 3xvec4 (48 bytes) + vec4 flags (16 bytes) = 128 bytes
        handle.paletteSSBO = GL15.glGenBuffers();
        GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, handle.paletteSSBO);
        GL15.glBufferData(GL43.GL_SHADER_STORAGE_BUFFER, (long) boneCount * 128, GL15.GL_DYNAMIC_DRAW);
        GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, 0);

        // 3. 创建 instance params SSBO
        // mat4 poseMatrix (64) + mat3 normalMatrix as 3xvec4 (48) + vec4 lightOverlay (16) + vec4 rgba (16) + vec4 uvRemap (16) = 160 bytes
        handle.instanceParamsSSBO = GL15.glGenBuffers();
        GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, handle.instanceParamsSSBO);
        GL15.glBufferData(GL43.GL_SHADER_STORAGE_BUFFER, 160, GL15.GL_DYNAMIC_DRAW);
        GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, 0);

        // 4. 创建 draw ring VertexBuffers
        for (int i = 0; i < RING_SIZE; i++) {
            VertexBuffer vb = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
            handle.drawRing[i] = vb;

            // 用占位数据初始化，让 VertexBuffer 建好 VAO/格式/索引状态
            BufferBuilder builder = new BufferBuilder(vertexCount * DRAW_VERTEX_STRIDE);
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);
            for (int v = 0; v < vertexCount; v++) {
                builder.vertex(0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 1);
            }
            BufferBuilder.RenderedBuffer rendered = builder.end();
            vb.bind();
            vb.upload(rendered);
            VertexBuffer.unbind();
        }

        // 5. 提取每个 VertexBuffer 底层的 VBO id（用于 compute 写入）
        for (int i = 0; i < RING_SIZE; i++) {
            handle.drawVBOIds[i] = ((VertexBufferAccessor) handle.drawRing[i]).simplebedrockmodel$getVertexBufferId();
        }

        return handle;
    }

    /**
     * 获取当前 ring slot 的 VertexBuffer（用于最终 draw）。
     */
    public VertexBuffer currentDrawBuffer() {
        return drawRing[currentRingSlot];
    }

    /**
     * 获取当前 ring slot 的底层 VBO id（用于 compute 写入）。
     */
    public int currentDrawVBOId() {
        return drawVBOIds[currentRingSlot];
    }

    /**
     * 推进到下一个 ring slot。
     */
    public void advanceRing() {
        currentRingSlot = (currentRingSlot + 1) % RING_SIZE;
    }

    public int sourceSSBO() { return sourceSSBO; }
    public int paletteSSBO() { return paletteSSBO; }
    public int instanceParamsSSBO() { return instanceParamsSSBO; }
    public int vertexCount() { return vertexCount; }
    public int boneCount() { return boneCount; }

    @Override
    public void close() {
        if (sourceSSBO > 0) {
            GL15.glDeleteBuffers(sourceSSBO);
            sourceSSBO = -1;
        }
        if (paletteSSBO > 0) {
            GL15.glDeleteBuffers(paletteSSBO);
            paletteSSBO = -1;
        }
        if (instanceParamsSSBO > 0) {
            GL15.glDeleteBuffers(instanceParamsSSBO);
            instanceParamsSSBO = -1;
        }
        for (int i = 0; i < RING_SIZE; i++) {
            if (drawRing[i] != null) {
                drawRing[i].close();
                drawRing[i] = null;
            }
        }
    }

}

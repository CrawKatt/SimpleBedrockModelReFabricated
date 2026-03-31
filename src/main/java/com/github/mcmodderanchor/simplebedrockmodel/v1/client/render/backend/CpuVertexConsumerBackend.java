package com.github.mcmodderanchor.simplebedrockmodel.v1.client.render.backend;

import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

/**
 * CPU 后端：直接使用现有的 {@link BedrockModel#renderToBuffer} + {@link VertexConsumer} 路径。
 * <p>
 * 这是对现有渲染逻辑的薄封装，作为 fallback 后端始终可用。
 */
public final class CpuVertexConsumerBackend implements BedrockRenderBackend {

    public static final CpuVertexConsumerBackend INSTANCE = new CpuVertexConsumerBackend();

    private CpuVertexConsumerBackend() {}

    @Override
    public boolean render(BedrockRenderRequest request) {
        BedrockModel model = request.model();
        PoseStack poseStack = request.poseStack();
        MultiBufferSource bufferSource = request.bufferSource();
        RenderType renderType = request.renderType();
        int light = request.packedLight();
        int overlay = request.packedOverlay();
        float r = request.red();
        float g = request.green();
        float b = request.blue();
        float a = request.alpha();

        VertexConsumer consumer;
        if (request.material() != null && request.renderTypeFactory() != null) {
            consumer = request.material().buffer(bufferSource, request.renderTypeFactory());
        } else {
            consumer = bufferSource.getBuffer(renderType);
        }

        model.renderToBuffer(poseStack, consumer, light, overlay, r, g, b, a);
        return true;
    }

    @Override
    public void onResourceReload() {
        // CPU 后端无需缓存管理
    }

    @Override
    public void close() {
        // CPU 后端无需资源释放
    }
}

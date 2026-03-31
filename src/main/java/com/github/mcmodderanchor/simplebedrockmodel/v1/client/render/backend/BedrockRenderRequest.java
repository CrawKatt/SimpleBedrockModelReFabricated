package com.github.mcmodderanchor.simplebedrockmodel.v1.client.render.backend;

import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.Material;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * 一次模型绘制请求的完整描述。
 * <p>
 * 封装了渲染一个 {@link BedrockModel} 所需的全部上下文信息，
 * 使得不同后端可以按自己的方式消费这些信息。
 */
public final class BedrockRenderRequest {
    private final BedrockModel model;
    private final PoseStack poseStack;
    private final MultiBufferSource bufferSource;
    private final RenderType renderType;
    private final int packedLight;
    private final int packedOverlay;
    private final float red;
    private final float green;
    private final float blue;
    private final float alpha;

    /**
     * 如果调用方使用的是 Material 路径（atlas sprite），这里保存 Material 引用，
     * 用于 compute 后端解析 UV remap 参数。
     * 如果调用方直接使用 RenderType，则为 null。
     */
    @Nullable
    private final Material material;

    /**
     * 当 material 非 null 时，保存 renderType 工厂函数引用，
     * 用于 fallback 路径重新获取 VertexConsumer。
     */
    @Nullable
    private final Function<net.minecraft.resources.ResourceLocation, RenderType> renderTypeFactory;

    private BedrockRenderRequest(Builder builder) {
        this.model = builder.model;
        this.poseStack = builder.poseStack;
        this.bufferSource = builder.bufferSource;
        this.renderType = builder.renderType;
        this.packedLight = builder.packedLight;
        this.packedOverlay = builder.packedOverlay;
        this.red = builder.red;
        this.green = builder.green;
        this.blue = builder.blue;
        this.alpha = builder.alpha;
        this.material = builder.material;
        this.renderTypeFactory = builder.renderTypeFactory;
    }

    public BedrockModel model() { return model; }
    public PoseStack poseStack() { return poseStack; }
    public MultiBufferSource bufferSource() { return bufferSource; }
    public RenderType renderType() { return renderType; }
    public int packedLight() { return packedLight; }
    public int packedOverlay() { return packedOverlay; }
    public float red() { return red; }
    public float green() { return green; }
    public float blue() { return blue; }
    public float alpha() { return alpha; }
    @Nullable public Material material() { return material; }
    @Nullable public Function<net.minecraft.resources.ResourceLocation, RenderType> renderTypeFactory() { return renderTypeFactory; }

    public static Builder builder(BedrockModel model, PoseStack poseStack, MultiBufferSource bufferSource, RenderType renderType) {
        return new Builder(model, poseStack, bufferSource, renderType);
    }

    public static final class Builder {
        private final BedrockModel model;
        private final PoseStack poseStack;
        private final MultiBufferSource bufferSource;
        private final RenderType renderType;
        private int packedLight;
        private int packedOverlay;
        private float red = 1.0f;
        private float green = 1.0f;
        private float blue = 1.0f;
        private float alpha = 1.0f;
        @Nullable private Material material;
        @Nullable private Function<net.minecraft.resources.ResourceLocation, RenderType> renderTypeFactory;

        private Builder(BedrockModel model, PoseStack poseStack, MultiBufferSource bufferSource, RenderType renderType) {
            this.model = model;
            this.poseStack = poseStack;
            this.bufferSource = bufferSource;
            this.renderType = renderType;
        }

        public Builder light(int packedLight) { this.packedLight = packedLight; return this; }
        public Builder overlay(int packedOverlay) { this.packedOverlay = packedOverlay; return this; }
        public Builder color(float r, float g, float b, float a) {
            this.red = r; this.green = g; this.blue = b; this.alpha = a;
            return this;
        }
        public Builder material(@Nullable Material material, @Nullable Function<net.minecraft.resources.ResourceLocation, RenderType> renderTypeFactory) {
            this.material = material;
            this.renderTypeFactory = renderTypeFactory;
            return this;
        }

        public BedrockRenderRequest build() {
            return new BedrockRenderRequest(this);
        }
    }
}

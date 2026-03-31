package com.github.mcmodderanchor.simplebedrockmodel.v1.client.render.backend;

import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

/**
 * 基岩版模型渲染后端接口。
 * <p>
 * 每个实现负责将一个 {@link BedrockModel} 按照给定的渲染参数绘制到屏幕上。
 * 不同实现可以选择不同的底层策略（CPU 顶点提交、GPU Compute 预变形等）。
 */
public interface BedrockRenderBackend {

    /**
     * 尝试渲染模型。
     *
     * @return {@code true} 表示本次绘制已被该后端成功处理；
     *         {@code false} 表示该后端无法处理本次绘制，调用方应回退到其他后端。
     */
    boolean render(BedrockRenderRequest request);

    /**
     * 当资源重载完成后调用，用于清理/重建内部缓存。
     */
    void onResourceReload();

    /**
     * 释放该后端持有的所有 GPU/CPU 资源。
     */
    void close();
}

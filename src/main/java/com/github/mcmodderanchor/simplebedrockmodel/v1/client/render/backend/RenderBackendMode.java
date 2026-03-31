package com.github.mcmodderanchor.simplebedrockmodel.v1.client.render.backend;

/**
 * 渲染后端模式。
 * <ul>
 *   <li>{@link #OFF} - 始终使用现有 CPU/VertexConsumer 路径</li>
 *   <li>{@link #AUTO} - 运行时探测 GPU 能力，满足条件时启用 Compute 后端，否则回退 CPU</li>
 *   <li>{@link #FORCE_COMPUTE} - 尽量启用 Compute 后端；若初始化失败则打印错误并回退 CPU</li>
 * </ul>
 */
public enum RenderBackendMode {
    OFF,
    AUTO,
    FORCE_COMPUTE
}

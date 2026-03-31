package com.github.mcmodderanchor.simplebedrockmodel.v1.client.render.backend;

import com.github.mcmodderanchor.simplebedrockmodel.SimpleBedrockModel;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockModel;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * 基岩版模型渲染的统一入口。
 * <p>
 * 所有内部 renderer 应通过此类提交绘制请求，而不是直接调用
 * {@link BedrockModel#renderToBuffer}。
 * <p>
 * 该类负责：
 * <ol>
 *   <li>根据当前配置和 GPU 能力选择合适的后端</li>
 *   <li>对不适合高性能后端的请求自动 fallback 到 CPU 路径</li>
 *   <li>管理后端生命周期（初始化、资源重载、关闭）</li>
 * </ol>
 */
@OnlyIn(Dist.CLIENT)
public final class BedrockRenderDispatcher {

    private static RenderBackendMode mode = RenderBackendMode.OFF;

    /**
     * 高性能后端（Compute）。为 null 表示尚未初始化或不可用。
     */
    @Nullable
    private static BedrockRenderBackend computeBackend;

    /**
     * CPU fallback 后端，始终可用。
     */
    private static final CpuVertexConsumerBackend cpuBackend = CpuVertexConsumerBackend.INSTANCE;

    /**
     * 全局故障标记。一旦 compute 后端在运行时出错，整个 session 切回 CPU。
     */
    private static boolean computeFaulted = false;

    // ---- 配置 ----

    public static void setMode(RenderBackendMode newMode) {
        mode = newMode;
    }

    public static RenderBackendMode getMode() {
        return mode;
    }

    /**
     * 注册 compute 后端实例。应在客户端初始化阶段、capability probe 通过后调用。
     */
    public static void setComputeBackend(@Nullable BedrockRenderBackend backend) {
        computeBackend = backend;
    }

    // ---- 统一渲染入口 ----

    /**
     * 使用 RenderType 路径提交绘制请求。
     */
    public static void render(BedrockModel model, PoseStack poseStack, MultiBufferSource bufferSource,
                              RenderType renderType, int packedLight, int packedOverlay) {
        render(model, poseStack, bufferSource, renderType, packedLight, packedOverlay,
                1.0f, 1.0f, 1.0f, 1.0f);
    }

    /**
     * 使用 RenderType 路径提交绘制请求（带颜色）。
     */
    public static void render(BedrockModel model, PoseStack poseStack, MultiBufferSource bufferSource,
                              RenderType renderType, int packedLight, int packedOverlay,
                              float r, float g, float b, float a) {
        BedrockRenderRequest request = BedrockRenderRequest
                .builder(model, poseStack, bufferSource, renderType)
                .light(packedLight)
                .overlay(packedOverlay)
                .color(r, g, b, a)
                .build();
        dispatch(request);
    }

    /**
     * 使用 Material（atlas sprite）路径提交绘制请求。
     */
    public static void render(BedrockModel model, PoseStack poseStack, MultiBufferSource bufferSource,
                              Material material, Function<ResourceLocation, RenderType> renderTypeFactory,
                              int packedLight, int packedOverlay) {
        render(model, poseStack, bufferSource, material, renderTypeFactory,
                packedLight, packedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    /**
     * 使用 Material（atlas sprite）路径提交绘制请求（带颜色）。
     */
    public static void render(BedrockModel model, PoseStack poseStack, MultiBufferSource bufferSource,
                              Material material, Function<ResourceLocation, RenderType> renderTypeFactory,
                              int packedLight, int packedOverlay,
                              float r, float g, float b, float a) {
        // 解析实际 RenderType（与 Material.renderType(...) 保持一致，使用 atlasLocation）
        RenderType renderType = renderTypeFactory.apply(material.atlasLocation());
        BedrockRenderRequest request = BedrockRenderRequest
                .builder(model, poseStack, bufferSource, renderType)
                .light(packedLight)
                .overlay(packedOverlay)
                .color(r, g, b, a)
                .material(material, renderTypeFactory)
                .build();
        dispatch(request);
    }

    // ---- 内部调度 ----

    private static void dispatch(BedrockRenderRequest request) {
        // 尝试 compute 后端
        if (shouldTryCompute(request)) {
            try {
                boolean handled = computeBackend.render(request);
                if (handled) {
                    return;
                }
                // compute 后端返回 false，表示本次不适合，fallback
            } catch (Exception e) {
                // compute 运行时故障，熔断到 CPU
                SimpleBedrockModel.LOGGER.error("Compute render backend faulted, falling back to CPU for this session", e);
                computeFaulted = true;
            }
        }

        // CPU fallback
        cpuBackend.render(request);
    }

    private static boolean shouldTryCompute(BedrockRenderRequest request) {
        if (mode == RenderBackendMode.OFF) return false;
        if (computeBackend == null) return false;
        if (computeFaulted) return false;

        // 初版只接管 NEW_ENTITY + QUADS
        RenderType rt = request.renderType();
        if (rt.format() != DefaultVertexFormat.NEW_ENTITY) return false;
        if (rt.mode() != VertexFormat.Mode.QUADS) return false;

        return true;
    }

    // ---- 生命周期 ----

    /**
     * 资源重载完成后调用。
     */
    public static void onResourceReload() {
        if (computeBackend != null) {
            computeBackend.onResourceReload();
        }
    }

    /**
     * 客户端关闭时调用，释放所有 GPU 资源。
     */
    public static void close() {
        if (computeBackend != null) {
            computeBackend.close();
            computeBackend = null;
        }
        computeFaulted = false;
    }

    /**
     * 重置故障状态（例如用户在设置中重新启用 compute 后端时）。
     */
    public static void resetFault() {
        computeFaulted = false;
    }

    public static boolean isComputeFaulted() {
        return computeFaulted;
    }

    public static boolean isComputeActive() {
        return mode != RenderBackendMode.OFF && computeBackend != null && !computeFaulted;
    }

    private BedrockRenderDispatcher() {}
}

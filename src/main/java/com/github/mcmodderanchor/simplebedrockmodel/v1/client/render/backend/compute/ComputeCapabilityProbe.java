package com.github.mcmodderanchor.simplebedrockmodel.v1.client.render.backend.compute;

import com.github.mcmodderanchor.simplebedrockmodel.SimpleBedrockModel;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

/**
 * OpenGL Compute Shader 能力探测。
 * <p>
 * Minecraft 1.20.1 请求的是 OpenGL 3.2 Core 上下文，
 * 而 Compute Shader 需要 OpenGL 4.3 或 {@code GL_ARB_compute_shader} 扩展。
 * <p>
 * 该类在渲染线程上执行一次性探测，结果缓存供后续使用。
 */
public final class ComputeCapabilityProbe {

    private static Boolean supported = null;
    private static String unsupportedReason = null;

    private ComputeCapabilityProbe() {}

    /**
     * 执行能力探测。必须在渲染线程上调用。
     *
     * @return true 如果当前 GL 上下文支持 compute shader + SSBO
     */
    public static boolean probe() {
        if (supported != null) {
            return supported;
        }

        try {
            GLCapabilities caps = GL.getCapabilities();

            // 检查 compute shader 支持
            if (!caps.GL_ARB_compute_shader && !caps.OpenGL43) {
                unsupportedReason = "Neither GL_ARB_compute_shader nor OpenGL 4.3 is available";
                supported = false;
                return false;
            }

            // 检查 SSBO 支持
            if (!caps.GL_ARB_shader_storage_buffer_object && !caps.OpenGL43) {
                unsupportedReason = "Neither GL_ARB_shader_storage_buffer_object nor OpenGL 4.3 is available";
                supported = false;
                return false;
            }

            supported = true;
            SimpleBedrockModel.LOGGER.info("Compute shader capability probe passed");
            return true;

        } catch (Exception e) {
            unsupportedReason = "Exception during capability probe: " + e.getMessage();
            supported = false;
            SimpleBedrockModel.LOGGER.warn("Compute capability probe failed", e);
            return false;
        }
    }

    public static boolean isSupported() {
        return supported != null && supported;
    }

    public static String getUnsupportedReason() {
        return unsupportedReason;
    }

    /**
     * 重置探测结果（用于测试或上下文切换）。
     */
    public static void reset() {
        supported = null;
        unsupportedReason = null;
    }
}

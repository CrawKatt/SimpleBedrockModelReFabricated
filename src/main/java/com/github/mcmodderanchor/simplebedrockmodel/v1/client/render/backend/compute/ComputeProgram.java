package com.github.mcmodderanchor.simplebedrockmodel.v1.client.render.backend.compute;

import com.github.mcmodderanchor.simplebedrockmodel.SimpleBedrockModel;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL43;

import javax.annotation.Nullable;

import static org.lwjgl.opengl.GL20.*;

/**
 * Compute Shader 程序的编译、链接和管理。
 * <p>
 * 封装了一个 compute shader program 的完整生命周期。
 */
public final class ComputeProgram implements AutoCloseable {

    private int programId = -1;
    private final String name;

    private ComputeProgram(String name, int programId) {
        this.name = name;
        this.programId = programId;
    }

    /**
     * 从 GLSL 源码编译并链接一个 compute program。
     *
     * @param name       程序名称（用于日志）
     * @param glslSource compute shader 的完整 GLSL 源码
     * @return 编译成功的 ComputeProgram，失败时返回 null
     */
    @Nullable
    public static ComputeProgram compile(String name, String glslSource) {
        int shader = GL43.glCreateShader(GL43.GL_COMPUTE_SHADER);
        if (shader == 0) {
            SimpleBedrockModel.LOGGER.error("Failed to create compute shader object for '{}'", name);
            return null;
        }

        GL20.glShaderSource(shader, glslSource);
        GL20.glCompileShader(shader);

        if (GL20.glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            String log = GL20.glGetShaderInfoLog(shader, 32768);
            GL20.glDeleteShader(shader);
            SimpleBedrockModel.LOGGER.error("Failed to compile compute shader '{}': {}", name, log);
            return null;
        }

        int program = GL20.glCreateProgram();
        if (program == 0) {
            GL20.glDeleteShader(shader);
            SimpleBedrockModel.LOGGER.error("Failed to create program object for '{}'", name);
            return null;
        }

        GL20.glAttachShader(program, shader);
        GL20.glLinkProgram(program);

        if (GL20.glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            String log = GL20.glGetProgramInfoLog(program, 32768);
            GL20.glDeleteProgram(program);
            GL20.glDeleteShader(shader);
            SimpleBedrockModel.LOGGER.error("Failed to link compute program '{}': {}", name, log);
            return null;
        }

        // shader 对象在链接后可以删除
        GL20.glDeleteShader(shader);

        SimpleBedrockModel.LOGGER.info("Compute program '{}' compiled and linked successfully (id={})", name, program);
        return new ComputeProgram(name, program);
    }

    /**
     * 绑定此 compute program 为当前活动程序。
     */
    public void bind() {
        if (programId > 0) {
            GL20.glUseProgram(programId);
        }
    }

    /**
     * 解绑当前程序。
     */
    public static void unbind() {
        GL20.glUseProgram(0);
    }

    /**
     * 获取 uniform 位置。
     */
    public int getUniformLocation(String uniformName) {
        return GL20.glGetUniformLocation(programId, uniformName);
    }

    public int programId() {
        return programId;
    }

    public String name() {
        return name;
    }

    public boolean isValid() {
        return programId > 0;
    }

    @Override
    public void close() {
        if (programId > 0) {
            GL20.glDeleteProgram(programId);
            programId = -1;
        }
    }
}

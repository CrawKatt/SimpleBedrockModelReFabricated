package com.github.mcmodderanchor.simplebedrockmodel.v1.mixin.client;

import com.mojang.blaze3d.vertex.VertexBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 暴露 {@link VertexBuffer} 底层 vertex VBO id 的 accessor。
 * <p>
 * Compute 后端需要直接把结果顶点写入现有 VertexBuffer 维护的 VBO，
 * 因此需要读取其私有字段。
 */
@Mixin(VertexBuffer.class)
public interface VertexBufferAccessor {
    @Accessor("vertexBufferId")
    int simplebedrockmodel$getVertexBufferId();
}

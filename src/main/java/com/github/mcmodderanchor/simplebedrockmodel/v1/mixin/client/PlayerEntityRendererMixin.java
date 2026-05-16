package com.github.mcmodderanchor.simplebedrockmodel.v1.mixin.client;

import com.github.mcmodderanchor.simplebedrockmodel.v1.client.handler.FirstPersonArmorHandler;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {
    @Inject(method = "renderRightArm", at = @At("TAIL"))
    private void sbm$renderRightArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                                    AbstractClientPlayerEntity player, CallbackInfo ci) {
        FirstPersonArmorHandler.renderArm(player, Arm.RIGHT, matrices, vertexConsumers, light);
    }

    @Inject(method = "renderLeftArm", at = @At("TAIL"))
    private void sbm$renderLeftArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                                   AbstractClientPlayerEntity player, CallbackInfo ci) {
        FirstPersonArmorHandler.renderArm(player, Arm.LEFT, matrices, vertexConsumers, light);
    }
}

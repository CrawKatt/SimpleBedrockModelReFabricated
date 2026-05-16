package com.github.mcmodderanchor.simplebedrockmodel.v1.mixin.client;

import com.github.mcmodderanchor.simplebedrockmodel.v1.client.event.SimpleBedrockModelEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Unique
    private boolean sbm$renderingHand;

    @Shadow
    public abstract MinecraftClient getClient();


    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    public void onBobHurt(MatrixStack pMatrixStack, float pPartialTicks, CallbackInfo ci) {
        boolean cancel = sbm$renderingHand
                ? SimpleBedrockModelEvents.BOB_HURT_ITEM_IN_HAND.invoker().shouldCancel()
                : SimpleBedrockModelEvents.BOB_HURT_LEVEL.invoker().shouldCancel();
        if (cancel) {
            ci.cancel();
        }
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    public void onBobView(MatrixStack pMatrixStack, float pPartialTicks, CallbackInfo ci) {
        boolean cancel = sbm$renderingHand
                ? SimpleBedrockModelEvents.BOB_VIEW_ITEM_IN_HAND.invoker().shouldCancel()
                : SimpleBedrockModelEvents.BOB_VIEW_LEVEL.invoker().shouldCancel();
        if (cancel) {
            ci.cancel();
        }
    }

    @Inject(method = "renderHand", at = @At("HEAD"))
    private void sbm$markRenderingHand(Camera camera, float tickProgress, org.joml.Matrix4f matrix4f, CallbackInfo ci) {
        this.sbm$renderingHand = true;
    }

    @Inject(method = "renderHand", at = @At("TAIL"))
    private void sbm$clearRenderingHand(Camera camera, float tickProgress, org.joml.Matrix4f matrix4f, CallbackInfo ci) {
        this.sbm$renderingHand = false;
    }
}

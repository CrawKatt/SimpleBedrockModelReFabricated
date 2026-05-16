package com.github.mcmodderanchor.simplebedrockmodel.v1.mixin.client;

import com.github.mcmodderanchor.simplebedrockmodel.v1.client.event.SimpleBedrockModelEvents;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererRenderMixin {
    @Inject(
            method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
            at = @At("HEAD")
    )
    private void sbm$beforeRenderHands(float tickProgress, MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers,
                                       ClientPlayerEntity player, int light, CallbackInfo ci) {
        SimpleBedrockModelEvents.BEFORE_RENDER_HAND.invoker().onBeforeRenderHand(matrices, tickProgress);
    }

    @Redirect(
            method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
                    ordinal = 0
            )
    )
    private void sbm$renderMainHand(HeldItemRenderer instance, AbstractClientPlayerEntity player, float tickProgress, float pitch,
                                    Hand hand, float swingProgress, ItemStack stack, float equipProgress, MatrixStack matrices,
                                    VertexConsumerProvider vertexConsumers, int light) {
        if (!SimpleBedrockModelEvents.RENDER_HAND.invoker().onRenderHand(hand, stack, matrices, vertexConsumers, light, tickProgress)) {
            this.sbm$callVanillaRenderFirstPersonItem(player, tickProgress, pitch, hand, swingProgress, stack, equipProgress, matrices, vertexConsumers, light);
        }
    }

    @Redirect(
            method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
                    ordinal = 1
            )
    )
    private void sbm$renderOffHand(HeldItemRenderer instance, AbstractClientPlayerEntity player, float tickProgress, float pitch,
                                   Hand hand, float swingProgress, ItemStack stack, float equipProgress, MatrixStack matrices,
                                   VertexConsumerProvider vertexConsumers, int light) {
        if (!SimpleBedrockModelEvents.RENDER_HAND.invoker().onRenderHand(hand, stack, matrices, vertexConsumers, light, tickProgress)) {
            this.sbm$callVanillaRenderFirstPersonItem(player, tickProgress, pitch, hand, swingProgress, stack, equipProgress, matrices, vertexConsumers, light);
        }
    }

    @SuppressWarnings("TargetMethodParameters")
    @Invoker("renderFirstPersonItem")
    protected abstract void sbm$callVanillaRenderFirstPersonItem(AbstractClientPlayerEntity player, float tickProgress, float pitch, Hand hand,
                                                                 float swingProgress, ItemStack stack, float equipProgress, MatrixStack matrices,
                                                                 VertexConsumerProvider vertexConsumers, int light);
}

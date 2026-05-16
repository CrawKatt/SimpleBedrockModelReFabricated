package com.github.mcmodderanchor.simplebedrockmodel.v1.mixin.client;

import com.github.mcmodderanchor.simplebedrockmodel.v1.client.handler.FirstPersonRenderHandler;
import net.minecraft.client.render.item.HeldItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = HeldItemRenderer.class, priority = 2000)
public class HeldItemRendererMixin {

    @Shadow
    private float equipProgressMainHand;

    @Shadow
    private float prevEquipProgressMainHand;

    @Inject(method = "updateHeldItems", at = @At("HEAD"), cancellable = true)
    private void onTickHead(CallbackInfo ci) {
        if (FirstPersonRenderHandler.shouldLockVanilla()) {
            this.equipProgressMainHand = 1.0F;
            this.prevEquipProgressMainHand = 1.0F;
            ci.cancel();
        }
    }
}

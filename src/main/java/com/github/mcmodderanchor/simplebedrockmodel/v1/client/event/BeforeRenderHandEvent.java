package com.github.mcmodderanchor.simplebedrockmodel.v1.client.event;

import net.minecraft.client.util.math.MatrixStack;
import net.neoforged.bus.api.Event;


/**
 * 在调用 ItemInHandRenderer#renderHandsWithItems 方法时触发该事件
 * 用于相机动画相关调用
 */
public class BeforeRenderHandEvent extends Event {
    private final MatrixStack poseStack;
    private final float partialTick;

    public BeforeRenderHandEvent(MatrixStack poseStack, float partialTicks) {
        this.poseStack = poseStack;
        this.partialTick = partialTicks;
    }

    public MatrixStack getPoseStack() {
        return poseStack;
    }

    public float getPartialTick() {
        return partialTick;
    }
}

package com.github.mcmodderanchor.simplebedrockmodel.v1.client.handler;

import com.github.mcmodderanchor.simplebedrockmodel.v1.client.animation.IFPAnimationInstance;
import com.github.mcmodderanchor.simplebedrockmodel.v1.client.renderer.IFPGeoItemRenderer;
import com.github.mcmodderanchor.simplebedrockmodel.v1.registry.GeoItemRendererRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public class FirstPersonRenderHandler {

    private static int realSelectedSlot = -1;
    private static ItemStack realMainHand = ItemStack.EMPTY;

    private static boolean transitioning = false;

    private static IFPAnimationInstance activeInstance = null;
    private static IFPAnimationInstance previousInstance = null;

    private static ItemStack pendingTarget = ItemStack.EMPTY;

    private static long switchStartTime = 0L;
    private static long currentSheatheDuration = 0L;

    private static boolean lockVanilla = false;
    private static boolean nextIsCustom = false;

    private static boolean forceHandSwapFlag = false;

    public static void reset() {
        realSelectedSlot = -1;
        realMainHand = ItemStack.EMPTY;
        transitioning = false;
        activeInstance = null;
        previousInstance = null;
        pendingTarget = ItemStack.EMPTY;
        lockVanilla = false;
        nextIsCustom = false;
        forceHandSwapFlag = false;
    }


    public static void onSwapItemWithOffhand() {
        forceHandSwapFlag = true;
    }

    public static void onRenderFrame() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        int newSlot = player.getInventory().selectedSlot;
        ItemStack newMain = player.getMainHandStack();

        boolean slotChanged = newSlot != realSelectedSlot || forceHandSwapFlag;
        boolean itemChanged = !isSameItemStacks(realMainHand, newMain);
        forceHandSwapFlag = false;

        realSelectedSlot = newSlot;
        realMainHand = newMain;

        if (slotChanged) {
            onSlotChanged(newMain);
        } else if (itemChanged) {
            onItemChangedInSameSlot(newMain);
        }if (activeInstance != null) {
            activeInstance.updateItem(newMain);
        }

        tickStates();
    }

    private static void onSlotChanged(ItemStack newStack) {
        pendingTarget = newStack;
        nextIsCustom = isCustomItem(newStack);

        if (transitioning) {
            return;
        }

        boolean oldIsCustom = activeInstance != null;

        previousInstance = activeInstance;
        activeInstance = createInstance(newStack);

        if (oldIsCustom) {
            // Custom → Any：播放 Putaway
            transitioning = true;
            lockVanilla = true;

            switchStartTime = System.currentTimeMillis();
            currentSheatheDuration = calculateSheatheDuration(previousInstance.currentItem());

            previousInstance.triggerPutAway();
        } else {
            // Vanilla → Custom / Vanilla：直接切
            transitioning = false;
            lockVanilla = false;
        }
    }

    private static void onItemChangedInSameSlot(ItemStack newStack) {
        pendingTarget = newStack;
        nextIsCustom = isCustomItem(newStack);

        if (transitioning) {
            return;
        }

        boolean oldIsCustom = activeInstance != null;

        previousInstance = activeInstance;
        activeInstance = createInstance(newStack);

        if (oldIsCustom) {
            transitioning = true;
            lockVanilla = true;

            switchStartTime = System.currentTimeMillis();
            currentSheatheDuration = calculateSheatheDuration(previousInstance.currentItem());

            previousInstance.triggerPutAway();
        } else {
            // Vanilla → Custom
            lockVanilla = false;
        }
    }

    private static void tickStates() {
        if (transitioning) {
            if (getSheatheProgress() >= 1.0f) {
                // Putaway 完成
                transitioning = false;
                lockVanilla = false;

                activeInstance = createInstance(pendingTarget);
                previousInstance = null;
            }
        }
    }

    public static void tickAnimation(float partialTick) {
        var ani = getActiveAnimationInstance();
        if (ani != null) {
            ani.triggerDraw();
            ani.tick(partialTick);
        }
    }

    public static boolean onRenderHand(Hand hand, ItemStack ignoredStack, MatrixStack matrixStack, VertexConsumerProvider bufferSource, int light, float partialTick) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return false;
        }

        IFPAnimationInstance inst = getActiveAnimationInstance();
        if (inst == null) {
            return false;
        }

        ItemStack stack = inst.currentItem();
        if (stack.isEmpty()) {
            return false;
        }

        return getRenderer(stack).map(renderer -> {
            if (hand == Hand.OFF_HAND) {
                return renderer.blockOffhandRender();
            }

            ModelTransformationMode transformType;
            if (hand == Hand.MAIN_HAND) {
                transformType = ModelTransformationMode.FIRST_PERSON_RIGHT_HAND;
            } else {
                transformType = ModelTransformationMode.FIRST_PERSON_LEFT_HAND;
            }
            renderer.renderFirstPerson(
                    player,
                    stack,
                    transformType,
                    matrixStack,
                    bufferSource,
                    light,
                    partialTick
            );
            return true;
        }).orElse(false);
    }

    public static boolean shouldLockVanilla() {
        return lockVanilla;
    }


    public static float getTargetHeight() {
        return nextIsCustom ? 1.0F : 0.0F;
    }

    public static IFPAnimationInstance getActiveAnimationInstance() {
        return transitioning ? previousInstance : activeInstance;
    }

    private static IFPAnimationInstance createInstance(ItemStack stack) {
        return getRenderer(stack)
                .map(r -> r.createAnimationInstance(stack, MinecraftClient.getInstance().getCameraEntity()))
                .orElse(null);
    }

    private static boolean isCustomItem(ItemStack stack) {
        return getRenderer(stack).isPresent();
    }

    private static long calculateSheatheDuration(ItemStack stack) {
        return getRenderer(stack)
                .map(r -> r.getPutAwayDuration(stack))
                .orElse(0L);
    }

    private static float getSheatheProgress() {
        if (currentSheatheDuration <= 0) return 1.0f;
        long elapsed = System.currentTimeMillis() - switchStartTime;
        return Math.min(1.0f, (float) elapsed / currentSheatheDuration);
    }

    private static Optional<IFPGeoItemRenderer> getRenderer(ItemStack stack) {
        return GeoItemRendererRegistry.getFPRenderer(stack);
    }

    private static boolean isSameItemStacks(ItemStack oldStack, ItemStack newStack) {
        if (oldStack == newStack) return true;
        if (oldStack.isEmpty() && newStack.isEmpty()) return true;
        if (oldStack.isEmpty() || newStack.isEmpty()) return false;

        return getRenderer(oldStack)
                .map(r -> r.isSameItem(oldStack, newStack))
                .orElseGet(() -> ItemStack.areEqual(oldStack, newStack));
    }
}

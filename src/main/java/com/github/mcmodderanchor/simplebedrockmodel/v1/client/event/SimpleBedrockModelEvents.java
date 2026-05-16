package com.github.mcmodderanchor.simplebedrockmodel.v1.client.event;

import com.github.mcmodderanchor.simplebedrockmodel.v1.client.handler.CameraEventHandler;
import com.github.mcmodderanchor.simplebedrockmodel.v1.event.RegisterBedrockAnimationEvent;
import com.github.mcmodderanchor.simplebedrockmodel.v1.event.RegisterBedrockAnimationReloadListenerEvent;
import com.github.mcmodderanchor.simplebedrockmodel.v1.event.RegisterBedrockModelEvent;
import com.github.mcmodderanchor.simplebedrockmodel.v1.event.RegisterBedrockModelReloadListenerEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Fabric Events for SimpleBedrockModel.
 * Use these to register your models, animations, and rendering callbacks.
 */
@Environment(EnvType.CLIENT)
public final class SimpleBedrockModelEvents {

    // ---- Registration events (client-side) ----

    /** Fire to register client-side bedrock models. */
    public static final Event<RegisterBedrockModelCallback> REGISTER_CLIENT_MODELS =
            EventFactory.createArrayBacked(RegisterBedrockModelCallback.class,
                    callbacks -> event -> { for (var cb : callbacks) cb.onRegister(event); });

    /** Fire to register client-side bedrock animations. */
    public static final Event<RegisterBedrockAnimationCallback> REGISTER_CLIENT_ANIMATIONS =
            EventFactory.createArrayBacked(RegisterBedrockAnimationCallback.class,
                    callbacks -> event -> { for (var cb : callbacks) cb.onRegister(event); });

    /** Register a listener that receives the model map after resource reload (client). */
    public static final Event<RegisterBedrockModelReloadListenerCallback> REGISTER_CLIENT_MODEL_RELOAD_LISTENERS =
            EventFactory.createArrayBacked(RegisterBedrockModelReloadListenerCallback.class,
                    callbacks -> event -> { for (var cb : callbacks) cb.onRegister(event); });

    /** Register a listener that receives the animation map after resource reload (client). */
    public static final Event<RegisterBedrockAnimationReloadListenerCallback> REGISTER_CLIENT_ANIMATION_RELOAD_LISTENERS =
            EventFactory.createArrayBacked(RegisterBedrockAnimationReloadListenerCallback.class,
                    callbacks -> event -> { for (var cb : callbacks) cb.onRegister(event); });

    // ---- Registration events (server-side) ----

    /** Fire to register server-side bedrock models. */
    public static final Event<RegisterBedrockModelCallback> REGISTER_SERVER_MODELS =
            EventFactory.createArrayBacked(RegisterBedrockModelCallback.class,
                    callbacks -> event -> { for (var cb : callbacks) cb.onRegister(event); });

    /** Fire to register server-side bedrock animations. */
    public static final Event<RegisterBedrockAnimationCallback> REGISTER_SERVER_ANIMATIONS =
            EventFactory.createArrayBacked(RegisterBedrockAnimationCallback.class,
                    callbacks -> event -> { for (var cb : callbacks) cb.onRegister(event); });

    /** Register a listener for server-side model reload. */
    public static final Event<RegisterBedrockModelReloadListenerCallback> REGISTER_SERVER_MODEL_RELOAD_LISTENERS =
            EventFactory.createArrayBacked(RegisterBedrockModelReloadListenerCallback.class,
                    callbacks -> event -> { for (var cb : callbacks) cb.onRegister(event); });

    /** Register a listener for server-side animation reload. */
    public static final Event<RegisterBedrockAnimationReloadListenerCallback> REGISTER_SERVER_ANIMATION_RELOAD_LISTENERS =
            EventFactory.createArrayBacked(RegisterBedrockAnimationReloadListenerCallback.class,
                    callbacks -> event -> { for (var cb : callbacks) cb.onRegister(event); });

    // ---- Rendering events ----

    /**
     * Fires just before first-person hand rendering (in ItemInHandRenderer).
     * Can be used for camera animation setup.
     */
    public static final Event<BeforeRenderHandCallback> BEFORE_RENDER_HAND =
            EventFactory.createArrayBacked(BeforeRenderHandCallback.class,
                    callbacks -> (poseStack, partialTick) -> {
                        for (var cb : callbacks) cb.onBeforeRenderHand(poseStack, partialTick);
                    });

    /**
     * Fires when the item-in-hand "bob hurt" animation is about to be applied.
     * Return true to cancel.
     */
    public static final Event<BobCancelCallback> BOB_HURT_ITEM_IN_HAND =
            EventFactory.createArrayBacked(BobCancelCallback.class,
                    callbacks -> () -> {
                        for (var cb : callbacks) { if (cb.shouldCancel()) return true; }
                        return false;
                    });

    /**
     * Fires when the item-in-hand "bob view" animation is about to be applied.
     * Return true to cancel.
     */
    public static final Event<BobCancelCallback> BOB_VIEW_ITEM_IN_HAND =
            EventFactory.createArrayBacked(BobCancelCallback.class,
                    callbacks -> () -> {
                        for (var cb : callbacks) { if (cb.shouldCancel()) return true; }
                        return false;
                    });

    /**
     * Fires when the level "bob hurt" animation is about to be applied.
     * Return true to cancel.
     */
    public static final Event<BobCancelCallback> BOB_HURT_LEVEL =
            EventFactory.createArrayBacked(BobCancelCallback.class,
                    callbacks -> () -> {
                        for (var cb : callbacks) { if (cb.shouldCancel()) return true; }
                        return false;
                    });

    /**
     * Fires when the level "bob view" animation is about to be applied.
     * Return true to cancel.
     */
    public static final Event<BobCancelCallback> BOB_VIEW_LEVEL =
            EventFactory.createArrayBacked(BobCancelCallback.class,
                    callbacks -> () -> {
                        for (var cb : callbacks) { if (cb.shouldCancel()) return true; }
                        return false;
                    });

    /**
     * Fires when the player swaps their main hand item with their offhand.
     */
    public static final Event<SwapItemCallback> SWAP_ITEM_WITH_OFFHAND =
            EventFactory.createArrayBacked(SwapItemCallback.class,
                    callbacks -> () -> { for (var cb : callbacks) cb.onSwap(); });

    // ---- Callback interfaces ----

    @FunctionalInterface
    public interface RegisterBedrockModelCallback {
        void onRegister(RegisterBedrockModelEvent event);
    }

    @FunctionalInterface
    public interface RegisterBedrockAnimationCallback {
        void onRegister(RegisterBedrockAnimationEvent event);
    }

    @FunctionalInterface
    public interface RegisterBedrockModelReloadListenerCallback {
        void onRegister(RegisterBedrockModelReloadListenerEvent event);
    }

    @FunctionalInterface
    public interface RegisterBedrockAnimationReloadListenerCallback {
        void onRegister(RegisterBedrockAnimationReloadListenerEvent event);
    }

    @FunctionalInterface
    public interface BeforeRenderHandCallback {
        void onBeforeRenderHand(MatrixStack matrixStack, float partialTick);
    }

    @FunctionalInterface
    public interface BobCancelCallback {
        /** @return true to cancel the bobbing animation */
        boolean shouldCancel();
    }

    @FunctionalInterface
    public interface SwapItemCallback {
        void onSwap();
    }

    /**
     * Called from ClientModInitializer to hook internal rendering handlers into the events.
     */
    public static void registerRenderEventHandlers() {
        BOB_VIEW_ITEM_IN_HAND.register(CameraEventHandler::shouldCancelItemInHandViewBobbing);
        BEFORE_RENDER_HAND.register(CameraEventHandler::onBeforeRenderHands);
    }

    private SimpleBedrockModelEvents() {}
}

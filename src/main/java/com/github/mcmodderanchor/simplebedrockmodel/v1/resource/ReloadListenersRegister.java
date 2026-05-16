package com.github.mcmodderanchor.simplebedrockmodel.v1.resource;

import com.github.mcmodderanchor.simplebedrockmodel.v1.client.event.SimpleBedrockModelEvents;
import com.github.mcmodderanchor.simplebedrockmodel.v1.event.RegisterBedrockAnimationEvent;
import com.github.mcmodderanchor.simplebedrockmodel.v1.event.RegisterBedrockAnimationReloadListenerEvent;
import com.github.mcmodderanchor.simplebedrockmodel.v1.event.RegisterBedrockModelEvent;
import com.github.mcmodderanchor.simplebedrockmodel.v1.event.RegisterBedrockModelReloadListenerEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

public class ReloadListenersRegister {

    /**
     * Called from ClientModInitializer to register client-side reload listeners.
     */
    public static void registerClientListeners() {
        RegisterBedrockModelEvent modelEvent = new RegisterBedrockModelEvent(EnvType.CLIENT);
        SimpleBedrockModelEvents.REGISTER_CLIENT_MODELS.invoker().onRegister(modelEvent);

        RegisterBedrockModelReloadListenerEvent modelReloadEvent = new RegisterBedrockModelReloadListenerEvent();
        SimpleBedrockModelEvents.REGISTER_CLIENT_MODEL_RELOAD_LISTENERS.invoker().onRegister(modelReloadEvent);

        BedrockModelResourceSet.INSTANCE = new BedrockModelResourceSet(
                modelEvent.getModelRegistry(), modelReloadEvent.getListeners());

        RegisterBedrockAnimationEvent animEvent = new RegisterBedrockAnimationEvent(EnvType.CLIENT);
        SimpleBedrockModelEvents.REGISTER_CLIENT_ANIMATIONS.invoker().onRegister(animEvent);

        RegisterBedrockAnimationReloadListenerEvent animReloadEvent = new RegisterBedrockAnimationReloadListenerEvent();
        SimpleBedrockModelEvents.REGISTER_CLIENT_ANIMATION_RELOAD_LISTENERS.invoker().onRegister(animReloadEvent);

        BedrockAnimationResourceSet.INSTANCE = new BedrockAnimationResourceSet(
                animEvent.getAnimationRegistry(), animReloadEvent.getListeners());

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
                .registerReloadListener(BedrockModelResourceSet.INSTANCE);
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
                .registerReloadListener(BedrockAnimationResourceSet.INSTANCE);
    }

    /**
     * Called from server-side (ModInitializer / AddReloadListenerEvent equivalent) to register
     * server-side reload listeners.
     */
    public static void registerServerListeners() {
        RegisterBedrockModelEvent modelEvent = new RegisterBedrockModelEvent(EnvType.SERVER);
        SimpleBedrockModelEvents.REGISTER_SERVER_MODELS.invoker().onRegister(modelEvent);

        RegisterBedrockModelReloadListenerEvent modelReloadEvent = new RegisterBedrockModelReloadListenerEvent();
        SimpleBedrockModelEvents.REGISTER_SERVER_MODEL_RELOAD_LISTENERS.invoker().onRegister(modelReloadEvent);

        BedrockModelResourceSet.INSTANCE = new BedrockModelResourceSet(
                modelEvent.getModelRegistry(), modelReloadEvent.getListeners());

        RegisterBedrockAnimationEvent animEvent = new RegisterBedrockAnimationEvent(EnvType.SERVER);
        SimpleBedrockModelEvents.REGISTER_SERVER_ANIMATIONS.invoker().onRegister(animEvent);

        RegisterBedrockAnimationReloadListenerEvent animReloadEvent = new RegisterBedrockAnimationReloadListenerEvent();
        SimpleBedrockModelEvents.REGISTER_SERVER_ANIMATION_RELOAD_LISTENERS.invoker().onRegister(animReloadEvent);

        BedrockAnimationResourceSet.INSTANCE = new BedrockAnimationResourceSet(
                animEvent.getAnimationRegistry(), animReloadEvent.getListeners());

        ResourceManagerHelper.get(ResourceType.SERVER_DATA)
                .registerReloadListener(BedrockModelResourceSet.INSTANCE);
        ResourceManagerHelper.get(ResourceType.SERVER_DATA)
                .registerReloadListener(BedrockAnimationResourceSet.INSTANCE);
    }
}

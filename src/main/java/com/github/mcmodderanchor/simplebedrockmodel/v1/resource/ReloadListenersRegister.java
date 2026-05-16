package com.github.mcmodderanchor.simplebedrockmodel.v1.resource;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

public class ReloadListenersRegister {
    private static boolean clientRegistered;
    private static boolean serverRegistered;

    /**
     * Called from ClientModInitializer to register client-side reload listeners.
     */
    public static void registerClientListeners() {
        if (clientRegistered) {
            return;
        }
        clientRegistered = true;

        BedrockModelResourceSet.INSTANCE = new BedrockModelResourceSet(EnvType.CLIENT);
        BedrockAnimationResourceSet.INSTANCE = new BedrockAnimationResourceSet(EnvType.CLIENT);

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
        if (serverRegistered) {
            return;
        }
        serverRegistered = true;

        BedrockModelResourceSet.INSTANCE = new BedrockModelResourceSet(EnvType.SERVER);
        BedrockAnimationResourceSet.INSTANCE = new BedrockAnimationResourceSet(EnvType.SERVER);

        ResourceManagerHelper.get(ResourceType.SERVER_DATA)
                .registerReloadListener(BedrockModelResourceSet.INSTANCE);
        ResourceManagerHelper.get(ResourceType.SERVER_DATA)
                .registerReloadListener(BedrockAnimationResourceSet.INSTANCE);
    }
}

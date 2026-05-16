package com.github.mcmodderanchor.simplebedrockmodel;

import com.github.mcmodderanchor.simplebedrockmodel.v1.client.compat.sodium.SodiumCompat;
import com.github.mcmodderanchor.simplebedrockmodel.v1.client.event.SimpleBedrockModelEvents;
import com.github.mcmodderanchor.simplebedrockmodel.v1.client.handler.FirstPersonRenderHandler;
import com.github.mcmodderanchor.simplebedrockmodel.v1.network.ClientNetworkHandler;
import com.github.mcmodderanchor.simplebedrockmodel.v1.resource.ReloadListenersRegister;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class SimpleBedrockModelClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Init compat
        SodiumCompat.init();

        // Register client network handler
        ClientNetworkHandler.register();

        // Register resource reload listeners (client-side)
        ReloadListenersRegister.registerClientListeners();

        // Register player logout handler
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> FirstPersonRenderHandler.reset());

        // Register rendering event handlers
        SimpleBedrockModelEvents.registerRenderEventHandlers();
    }
}

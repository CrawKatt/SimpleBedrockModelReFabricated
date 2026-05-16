package com.github.mcmodderanchor.simplebedrockmodel.v1.event;

import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockModel;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class RegisterBedrockModelReloadListenerEvent {
    private final List<Consumer<Map<Identifier, BedrockModel>>> listeners = new ArrayList<>();

    public void register(Consumer<Map<Identifier, BedrockModel>> listener) {
        this.listeners.add(listener);
    }

    public List<Consumer<Map<Identifier, BedrockModel>>> getListeners() {
        return listeners;
    }
}

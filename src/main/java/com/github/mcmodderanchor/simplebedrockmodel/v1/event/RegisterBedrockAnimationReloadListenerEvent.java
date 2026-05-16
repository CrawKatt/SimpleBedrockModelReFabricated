package com.github.mcmodderanchor.simplebedrockmodel.v1.event;

import com.github.mcmodderanchor.simplebedrockmodel.v1.common.animation.BedrockAnimation;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class RegisterBedrockAnimationReloadListenerEvent {
    private final List<Consumer<Map<Identifier, List<BedrockAnimation>>>> listeners = new ArrayList<>();

    public void register(Consumer<Map<Identifier, List<BedrockAnimation>>> listener) {
        this.listeners.add(listener);
    }

    public List<Consumer<Map<Identifier, List<BedrockAnimation>>>> getListeners() {
        return listeners;
    }
}

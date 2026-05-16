package com.github.mcmodderanchor.simplebedrockmodel.v1.event;

import com.github.mcmodderanchor.simplebedrockmodel.v1.common.animation.BedrockAnimation;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockModel;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.BedrockAnimationFile;
import com.github.mcmodderanchor.simplebedrockmodel.v1.resource.BedrockAnimationResourceProcessor;
import com.github.mcmodderanchor.simplebedrockmodel.v1.resource.RawResourceLoader;
import com.google.common.collect.Maps;
import net.fabricmc.api.EnvType;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class RegisterBedrockAnimationEvent {
    private final Map<Identifier, BedrockAnimationResourceProcessor> animationRegistry;
    private final EnvType envType;

    public RegisterBedrockAnimationEvent(EnvType envType) {
        this.animationRegistry = Maps.newHashMap();
        this.envType = envType;
    }

    public void register(Identifier animationLocation,
                         Identifier modelLocation,
                         RawResourceLoader loader,
                         BiFunction<BedrockAnimationFile, BedrockModel, List<BedrockAnimation>> converter) {
        animationRegistry.put(animationLocation, new BedrockAnimationResourceProcessor(loader, modelLocation, converter));
    }

    public void register(Identifier animationLocation,
                         Identifier modelLocation,
                         RawResourceLoader loader) {
        register(animationLocation, modelLocation, loader, BedrockAnimation::createAnimation);
    }

    public EnvType getEnvType() {
        return envType;
    }

    public Map<Identifier, BedrockAnimationResourceProcessor> getAnimationRegistry() {
        return animationRegistry;
    }
}

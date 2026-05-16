package com.github.mcmodderanchor.simplebedrockmodel.v1.resource;

import com.github.mcmodderanchor.simplebedrockmodel.SimpleBedrockModel;
import com.github.mcmodderanchor.simplebedrockmodel.v1.client.event.SimpleBedrockModelEvents;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.animation.BedrockAnimation;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockModel;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.BedrockAnimationFile;
import com.github.mcmodderanchor.simplebedrockmodel.v1.event.RegisterBedrockAnimationEvent;
import com.github.mcmodderanchor.simplebedrockmodel.v1.event.RegisterBedrockAnimationReloadListenerEvent;
import com.google.common.collect.Maps;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.UnmodifiableView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class BedrockAnimationResourceSet implements SimpleResourceReloadListener<Map<Identifier, BedrockAnimationFile>> {
    private final EnvType envType;
    private final Map<Identifier, List<BedrockAnimation>> animationCache;
    private Map<Identifier, BedrockAnimationResourceProcessor> processors;
    private List<Consumer<Map<Identifier, List<BedrockAnimation>>>> listeners;

    static BedrockAnimationResourceSet INSTANCE;

    public static BedrockAnimationResourceSet getInstance() {
        return INSTANCE;
    }

    BedrockAnimationResourceSet(EnvType envType) {
        this.envType = envType;
        this.processors = Collections.emptyMap();
        this.listeners = Collections.emptyList();
        this.animationCache = Maps.newHashMap();
    }

    @Override
    public Identifier getFabricId() {
        return SimpleBedrockModel.modLoc("bedrock_animation_resource_set");
    }

    @Override
    public CompletableFuture<Map<Identifier, BedrockAnimationFile>> load(ResourceManager resourceManager, Profiler profiler, Executor executor) {
        RegistrationSnapshot snapshot = collectRegistrations();
        this.processors = snapshot.processors();
        this.listeners = snapshot.listeners();

        return CompletableFuture.supplyAsync(() -> {
            Map<Identifier, BedrockAnimationFile> pojoMap = new HashMap<>();
            snapshot.processors().forEach((location, processor) -> {
                // Identifier.fromNamespaceAndPath -> Identifier.of
                Identifier path = Identifier.of(location.getNamespace(), "animations/" + location.getPath() + ".json");
                resourceManager.getResource(path).ifPresentOrElse(resource -> {
                    // resource.open() -> resource.getInputStream()
                    try (InputStream stream = resource.getInputStream()) {
                        BedrockAnimationFile pojo = processor.rawLoader().load(stream, BedrockAnimationFile.class);
                        if (pojo != null) {
                            pojoMap.put(location, pojo);
                        }
                    } catch (IOException e) {
                        SimpleBedrockModel.LOGGER.error("Failed to load animation file: {}", path, e);
                    }
                }, () -> SimpleBedrockModel.LOGGER.error("Not found animation file: {}", path));
            });
            return pojoMap;
        }, executor);
    }

    @Override
    public CompletableFuture<Void> apply(Map<Identifier, BedrockAnimationFile> pojoMap, ResourceManager pResourceManager, Profiler pProfiler, Executor executor) {
        return CompletableFuture.runAsync(() -> {
            animationCache.clear();
            processors.forEach((location, processor) -> {
                BedrockAnimationFile pojo = pojoMap.get(location);
                if (pojo == null) return;
                Identifier modelKey = processor.modelKey();
                BedrockModel model = modelKey == null ? null : BedrockModelResourceSet.getInstance().getModel(modelKey);
                List<BedrockAnimation> animations = processor.converter().apply(pojo, model);
                if (animations != null) {
                    animationCache.put(location, animations);
                }
            });
            Map<Identifier, List<BedrockAnimation>> animationMap = getAllAnimations();
            for (Consumer<Map<Identifier, List<BedrockAnimation>>> listener : listeners) {
                listener.accept(animationMap);
            }
        }, executor);
    }

    private RegistrationSnapshot collectRegistrations() {
        RegisterBedrockAnimationEvent animEvent = new RegisterBedrockAnimationEvent(envType);
        RegisterBedrockAnimationReloadListenerEvent animReloadEvent = new RegisterBedrockAnimationReloadListenerEvent();

        if (envType == EnvType.CLIENT) {
            SimpleBedrockModelEvents.REGISTER_CLIENT_ANIMATIONS.invoker().onRegister(animEvent);
            SimpleBedrockModelEvents.REGISTER_CLIENT_ANIMATION_RELOAD_LISTENERS.invoker().onRegister(animReloadEvent);
        } else {
            SimpleBedrockModelEvents.REGISTER_SERVER_ANIMATIONS.invoker().onRegister(animEvent);
            SimpleBedrockModelEvents.REGISTER_SERVER_ANIMATION_RELOAD_LISTENERS.invoker().onRegister(animReloadEvent);
        }

        return new RegistrationSnapshot(
                new LinkedHashMap<>(animEvent.getAnimationRegistry()),
                List.copyOf(animReloadEvent.getListeners())
        );
    }

    public List<BedrockAnimation> getAnimations(Identifier location) {
        return animationCache.get(location);
    }

    @UnmodifiableView
    public Map<Identifier, List<BedrockAnimation>> getAllAnimations() {
        return Collections.unmodifiableMap(animationCache);
    }

    private record RegistrationSnapshot(Map<Identifier, BedrockAnimationResourceProcessor> processors,
                                        List<Consumer<Map<Identifier, List<BedrockAnimation>>>> listeners) {
    }
}

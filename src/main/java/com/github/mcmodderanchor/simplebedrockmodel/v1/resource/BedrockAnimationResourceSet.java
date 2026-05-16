package com.github.mcmodderanchor.simplebedrockmodel.v1.resource;

import com.github.mcmodderanchor.simplebedrockmodel.SimpleBedrockModel;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.animation.BedrockAnimation;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockModel;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.BedrockAnimationFile;
import com.google.common.collect.Maps;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class BedrockAnimationResourceSet implements SimpleResourceReloadListener<Map<Identifier, BedrockAnimationFile>> {
    private final Map<Identifier, BedrockAnimationResourceProcessor> processors;
    private final List<Consumer<Map<Identifier, List<BedrockAnimation>>>> listeners;
    private final Map<Identifier, List<BedrockAnimation>> animationCache;

    static BedrockAnimationResourceSet INSTANCE;

    public static BedrockAnimationResourceSet getInstance() {
        return INSTANCE;
    }

    BedrockAnimationResourceSet(Map<Identifier, BedrockAnimationResourceProcessor> processors,
                                List<Consumer<Map<Identifier, List<BedrockAnimation>>>> listeners) {
        this.processors = processors;
        this.listeners = listeners;
        this.animationCache = Maps.newHashMap();
    }

    @Override
    public Identifier getFabricId() {
        return SimpleBedrockModel.modLoc("bedrock_animation_resource_set");
    }

    @Override
    public CompletableFuture<Map<Identifier, BedrockAnimationFile>> load(ResourceManager resourceManager, Profiler profiler, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            Map<Identifier, BedrockAnimationFile> pojoMap = new HashMap<>();
            processors.forEach((location, processor) -> {
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

    public List<BedrockAnimation> getAnimations(Identifier location) {
        return animationCache.get(location);
    }

    @UnmodifiableView
    public Map<Identifier, List<BedrockAnimation>> getAllAnimations() {
        return Collections.unmodifiableMap(animationCache);
    }
}

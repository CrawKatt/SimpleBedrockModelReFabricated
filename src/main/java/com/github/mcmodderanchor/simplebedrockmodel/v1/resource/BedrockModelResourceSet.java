package com.github.mcmodderanchor.simplebedrockmodel.v1.resource;

import com.github.mcmodderanchor.simplebedrockmodel.SimpleBedrockModel;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockModel;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.BedrockModelPOJO;
import com.google.common.collect.Maps;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.UnmodifiableView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class BedrockModelResourceSet implements SimpleResourceReloadListener<Map<Identifier, BedrockModelPOJO>> {
    private final Map<Identifier, BedrockModelResourceProcessor> processors;
    private final Map<Identifier, BedrockModel> modelCache;
    private final List<Consumer<Map<Identifier, BedrockModel>>> listeners;

    static BedrockModelResourceSet INSTANCE;

    public static BedrockModelResourceSet getInstance() {
        return INSTANCE;
    }

    BedrockModelResourceSet(Map<Identifier, BedrockModelResourceProcessor> processors,
                            List<Consumer<Map<Identifier, BedrockModel>>> listeners) {
        this.processors = processors;
        this.listeners = listeners;
        this.modelCache = Maps.newHashMap();
    }

    @Override
    public Identifier getFabricId() {
        return SimpleBedrockModel.modLoc("bedrock_model_resource_set");
    }

    @Override
    public CompletableFuture<Map<Identifier, BedrockModelPOJO>> load(ResourceManager resourceManager, Profiler profiler, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            Map<Identifier, BedrockModelPOJO> pojoMap = Maps.newHashMap();
            processors.forEach((location, processor) -> {
                Identifier path = Identifier.of(location.getNamespace(), "models/bedrock/" + location.getPath() + ".json");
                resourceManager.getResource(path).ifPresentOrElse(resource -> {
                    try (InputStream stream = resource.getInputStream()) {
                        BedrockModelPOJO pojo = processor.rawLoader().load(stream, BedrockModelPOJO.class);
                        if (pojo != null) {
                            pojoMap.put(location, pojo);
                        }
                    } catch (IOException e) {
                        SimpleBedrockModel.LOGGER.error("Failed to load model file: {}", path, e);
                    }
                }, () -> SimpleBedrockModel.LOGGER.error("Not found model file: {}", path));
            });
            return pojoMap;
        }, executor);
    }


    @Override
    public CompletableFuture<Void> apply(Map<Identifier, BedrockModelPOJO> pojoMap, ResourceManager resourceManager, Profiler profiler, Executor executor) {
        return CompletableFuture.runAsync(() -> {
            modelCache.clear();
            processors.forEach((location, processor) -> {
                BedrockModelPOJO pojo = pojoMap.get(location);
                if (pojo == null) return;
                BedrockModel model = processor.converter().apply(pojo);
                if (model != null) {
                    modelCache.put(location, model);
                }
            });
            Map<Identifier, BedrockModel> modelMap = getAllModels();
            for (Consumer<Map<Identifier, BedrockModel>> listener : listeners) {
                listener.accept(modelMap);
            }
        }, executor);
    }

    public BedrockModel getModel(Identifier location) {
        return modelCache.get(location);
    }

    @UnmodifiableView
    public Map<Identifier, BedrockModel> getAllModels() {
        return Collections.unmodifiableMap(modelCache);
    }
}

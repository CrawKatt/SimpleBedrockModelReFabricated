package com.github.mcmodderanchor.simplebedrockmodel.v1.client.render.backend.mesh;

import com.github.mcmodderanchor.simplebedrockmodel.SimpleBedrockModel;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockModel;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * 模型扁平化网格的缓存。
 * <p>
 * 使用 {@link WeakHashMap} 以 {@link BedrockModel} 实例为 key，
 * 当模型被资源重载替换后，旧的缓存条目会自动被 GC 回收。
 */
public final class ModelMeshCache {

    private static final WeakHashMap<BedrockModel, FlattenedModelMesh> cache = new WeakHashMap<>();

    private ModelMeshCache() {}

    /**
     * 获取或构建指定模型的扁平化网格。
     *
     * @param model 基岩版模型
     * @return 扁平化网格，构建失败时返回 null
     */
    public static FlattenedModelMesh getOrBuild(BedrockModel model) {
        return cache.computeIfAbsent(model, m -> {
            try {
                FlattenedModelMesh mesh = FlattenedMeshBuilder.build(m);
                if (mesh.isEmpty()) {
                    SimpleBedrockModel.LOGGER.warn("FlattenedMeshBuilder produced empty mesh for model: {}", m);
                    return null;
                }
                return mesh;
            } catch (Exception e) {
                SimpleBedrockModel.LOGGER.error("Failed to build FlattenedModelMesh", e);
                return null;
            }
        });
    }

    /**
     * 清除所有缓存。通常在资源重载时调用。
     */
    public static void clear() {
        cache.clear();
    }

    /**
     * 当前缓存的模型数量。
     */
    public static int size() {
        return cache.size();
    }
}

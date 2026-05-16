package com.github.mcmodderanchor.simplebedrockmodel.v1.registry;

import com.github.mcmodderanchor.simplebedrockmodel.v1.client.renderer.AbstractGeoItemRenderer;
import com.github.mcmodderanchor.simplebedrockmodel.v1.client.renderer.IFPGeoItemRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registry for custom item renderers (Fabric replacement for IClientItemExtensions.getCustomRenderer()).
 * Register your items here along with their renderers.
 */
@Environment(EnvType.CLIENT)
public final class GeoItemRendererRegistry {

    private static final Map<Item, BuiltinModelItemRenderer> RENDERERS = new IdentityHashMap<>();

    /**
     * Registers a custom BEWLR for an item.
     * Also registers it with Fabric's BuiltinItemRendererRegistry for inventory/world rendering.
     */
    public static void register(Item item, BuiltinModelItemRenderer renderer) {
        RENDERERS.put(item, renderer);
    }

    @Nullable
    public static BuiltinModelItemRenderer getRenderer(Item item) {
        return RENDERERS.get(item);
    }

    /**
     * Returns an AbstractGeoItemRenderer if one is registered for this item stack.
     */
    public static Optional<AbstractGeoItemRenderer<?>> getAbstractGeoRenderer(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        BuiltinModelItemRenderer renderer = RENDERERS.get(stack.getItem());
        if (renderer instanceof AbstractGeoItemRenderer<?> geo) {
            return Optional.of(geo);
        }
        return Optional.empty();
    }

    /**
     * Returns an IFPGeoItemRenderer if one is registered for this item stack.
     */
    public static Optional<IFPGeoItemRenderer> getFPRenderer(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        BuiltinModelItemRenderer renderer = RENDERERS.get(stack.getItem());
        if (renderer instanceof IFPGeoItemRenderer fp) {
            return Optional.of(fp);
        }
        return Optional.empty();
    }

    private GeoItemRendererRegistry() {}
}

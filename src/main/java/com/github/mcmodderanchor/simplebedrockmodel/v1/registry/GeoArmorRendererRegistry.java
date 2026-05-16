package com.github.mcmodderanchor.simplebedrockmodel.v1.registry;

import com.github.mcmodderanchor.simplebedrockmodel.v1.client.renderer.GeoArmorRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Registry for custom armor renderers (Fabric replacement for IClientItemExtensions.getHumanoidArmorModel()).
 */
@Environment(EnvType.CLIENT)
public final class GeoArmorRendererRegistry {

    private static final Map<Item, Supplier<GeoArmorRenderer>> RENDERERS = new IdentityHashMap<>();

    /**
     * Registers a GeoArmorRenderer for an item.
     */
    public static void register(Item item, Supplier<GeoArmorRenderer> rendererFactory) {
        RENDERERS.put(item, rendererFactory);
    }

    @Nullable
    public static GeoArmorRenderer getRenderer(ItemStack stack) {
        if (stack.isEmpty()) return null;
        Supplier<GeoArmorRenderer> supplier = RENDERERS.get(stack.getItem());
        return supplier != null ? supplier.get() : null;
    }

    private GeoArmorRendererRegistry() {}
}

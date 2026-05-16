package com.github.mcmodderanchor.simplebedrockmodel.v1.registry;

import com.github.mcmodderanchor.simplebedrockmodel.v1.client.renderer.GeoArmorRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Registry for custom armor renderers (Fabric replacement for IClientItemExtensions.getHumanoidArmorModel()).
 */
@Environment(EnvType.CLIENT)
public final class GeoArmorRendererRegistry {

    private static final Map<Item, GeoArmorRenderer> RENDERERS = new IdentityHashMap<>();

    /**
     * Registers a GeoArmorRenderer for an item.
     */
    public static void register(Item item, GeoArmorRenderer renderer) {
        RENDERERS.put(item, renderer);
    }

    @Nullable
    public static GeoArmorRenderer getRenderer(ItemStack stack) {
        if (stack.isEmpty()) return null;
        return RENDERERS.get(stack.getItem());
    }

    private GeoArmorRendererRegistry() {}
}

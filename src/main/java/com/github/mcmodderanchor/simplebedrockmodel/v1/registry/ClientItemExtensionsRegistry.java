package com.github.mcmodderanchor.simplebedrockmodel.v1.registry;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

public final class ClientItemExtensionsRegistry {
    private static final Map<Item, IClientItemExtensions> EXTENSIONS = new IdentityHashMap<>();

    public static void register(IClientItemExtensions extensions, Item... items) {
        for (Item item : items) {
            EXTENSIONS.put(item, extensions);
        }
    }

    @Nullable
    public static IClientItemExtensions get(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        return EXTENSIONS.get(stack.getItem());
    }

    private ClientItemExtensionsRegistry() {}
}

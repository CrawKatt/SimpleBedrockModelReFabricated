package net.neoforged.neoforge.client.extensions.common;

import com.github.mcmodderanchor.simplebedrockmodel.v1.registry.ClientItemExtensionsRegistry;
import net.minecraft.item.Item;

public class RegisterClientExtensionsEvent {
    public void registerItem(IClientItemExtensions extensions, Item... items) {
        ClientItemExtensionsRegistry.register(extensions, items);
    }
}

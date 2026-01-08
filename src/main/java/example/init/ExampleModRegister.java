package example.init;

import example.animation.FPGunAnimationInstance;
import example.capability.FPGunAnimationCapability;
import example.item.DeagleItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.Supplier;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class ExampleModRegister {
    /**
     * 注册名用 example，方便 build 时排除
     */
    public static final String MOD_ID = "example";

    public static Item DEAGLE_ITEM;
    public static AttachmentType<FPGunAnimationCapability> FP_GUN_ANIMATION;

    @SubscribeEvent // on the mod event bus
    public static void register(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.ITEM)) {
            DEAGLE_ITEM = new DeagleItem();
            event.register(
                    Registries.ITEM,
                    registry -> {
                        registry.register(modLoc("deagle"), DEAGLE_ITEM);
                    }
            );
        } else if (event.getRegistryKey().equals(NeoForgeRegistries.ATTACHMENT_TYPES.key())) {
            FP_GUN_ANIMATION = AttachmentType.builder(FPGunAnimationCapability::new).build();
            event.register(
                    NeoForgeRegistries.ATTACHMENT_TYPES.key(),
                    registry -> {
                        registry.register(modLoc("fp_gun_ani"), FP_GUN_ANIMATION);
                    }
            );
        }



    }

    public static ResourceLocation modLoc(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }
}

package example.init;

import example.block.TestBlock;
import example.block.blockentity.TestBlockEntity;
import example.capability.FPGunAnimationCapability;
import example.entity.Zti;
import example.item.DeagleItem;
import example.item.ExampleArmorItem;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

//@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class ExampleModRegister {
    /**
     * 注册名用 example，方便 build 时排除
     */
    public static final String MOD_ID = "example";

    public static Block TEST_BLOCK;
    public static BlockEntityType<TestBlockEntity> TEST_BLOCK_ENTITY_TYPE;
    public static EntityType<Zti> ZTI_ENTITY_TYPE;
    public static BlockItem TEST_BLOCK_ITEM;
    public static Item DEAGLE_ITEM;
    public static AttachmentType<FPGunAnimationCapability> FP_GUN_ANIMATION;

    public static ExampleArmorItem DEFENDER_ARMOR_HELMET;
    public static ExampleArmorItem DEFENDER_ARMOR_CHESTPLATE;
    public static ExampleArmorItem DEFENDER_ARMOR_LEGGINGS;
    public static ExampleArmorItem DEFENDER_ARMOR_BOOTS;

    public static SpawnEggItem ZTI_SPAWN_EGG;
    public static ItemGroup TEST_TAB;

    //@SubscribeEvent // on the mod event bus
    public static void register(RegisterEvent event) {
        var registry = event.getRegistry();

        if (event.getRegistryKey().equals(NeoForgeRegistries.ATTACHMENT_TYPES.key())) {
            FP_GUN_ANIMATION = AttachmentType.builder(FPGunAnimationCapability::new).build();
            event.register(
                    NeoForgeRegistries.ATTACHMENT_TYPES.key(),
                    r -> r.register(modLoc("fp_gun_ani"), FP_GUN_ANIMATION)
            );
        }

        if (BuiltInRegistries.BLOCK.equals(registry)) {
            TEST_BLOCK = new TestBlock();
            event.register(BuiltInRegistries.BLOCK.key(), modLoc("test_block"), () -> TEST_BLOCK);
        }

        if (BuiltInRegistries.BLOCK_ENTITY_TYPE.equals(registry)) {
            TEST_BLOCK_ENTITY_TYPE = BlockEntityType.Builder.of(TestBlockEntity::new, TEST_BLOCK).build(null);
            event.register(BuiltInRegistries.BLOCK_ENTITY_TYPE.key(), modLoc("test_block_entity_type"), () -> TEST_BLOCK_ENTITY_TYPE);
        }

        if (BuiltInRegistries.ENTITY_TYPE.equals(registry)) {
            ZTI_ENTITY_TYPE = EntityType.Builder.of(Zti::new, MobCategory.MONSTER)
                    .sized(2.2F, 2.5F)
                    .build(modLoc("zti").toString());
            event.register(BuiltInRegistries.ENTITY_TYPE.key(), modLoc("zti"), () -> ZTI_ENTITY_TYPE);
        }

        if (BuiltInRegistries.ITEM.equals(registry)) {
            TEST_BLOCK_ITEM = new BlockItem(TEST_BLOCK, new BlockItem.Properties());
            DEAGLE_ITEM = new DeagleItem();
            DEFENDER_ARMOR_HELMET = new ExampleArmorItem(ArmorItem.Type.HELMET);
            DEFENDER_ARMOR_CHESTPLATE = new ExampleArmorItem(ArmorItem.Type.CHESTPLATE);
            DEFENDER_ARMOR_LEGGINGS = new ExampleArmorItem(ArmorItem.Type.LEGGINGS);
            DEFENDER_ARMOR_BOOTS = new ExampleArmorItem(ArmorItem.Type.BOOTS);
            ZTI_SPAWN_EGG = new DeferredSpawnEggItem(() -> ZTI_ENTITY_TYPE, 0x61554D, 0xD8B076, new Item.Properties());
            event.register(BuiltInRegistries.ITEM.key(), modLoc("test_block_item"), () -> TEST_BLOCK_ITEM);
            event.register(BuiltInRegistries.ITEM.key(), modLoc("deagle"), () -> DEAGLE_ITEM);

            event.register(BuiltInRegistries.ITEM.key(), modLoc("defender_helmet"), () -> DEFENDER_ARMOR_HELMET);
            event.register(BuiltInRegistries.ITEM.key(), modLoc("defender_chestplate"), () -> DEFENDER_ARMOR_CHESTPLATE);
            event.register(BuiltInRegistries.ITEM.key(), modLoc("defender_leggings"), () -> DEFENDER_ARMOR_LEGGINGS);
            event.register(BuiltInRegistries.ITEM.key(), modLoc("defender_boots"), () -> DEFENDER_ARMOR_BOOTS);

            event.register(BuiltInRegistries.ITEM.key(), modLoc("zti_spawn_egg"), () -> ZTI_SPAWN_EGG);
        }

        if (Registries.CREATIVE_MODE_TAB.equals(event.getRegistryKey())) {
            TEST_TAB = CreativeModeTab.builder().title(Text.translatable("item_group.example.name"))
                    .icon(() -> DEAGLE_ITEM.getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(TEST_BLOCK_ITEM);
                        output.accept(DEAGLE_ITEM);
                        output.accept(DEFENDER_ARMOR_HELMET);
                        output.accept(DEFENDER_ARMOR_CHESTPLATE);
                        output.accept(DEFENDER_ARMOR_LEGGINGS);
                        output.accept(DEFENDER_ARMOR_BOOTS);
                        output.accept(ZTI_SPAWN_EGG);
                    }).build();
            event.register(Registries.CREATIVE_MODE_TAB, modLoc("test_tab"), () -> TEST_TAB);
        }
    }

    //@SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        if (ZTI_ENTITY_TYPE != null) {
            event.put(ZTI_ENTITY_TYPE, Zti.createAttributes().build());
        }
    }

    public static Identifier modLoc(String name) {
        return Identifier.of(MOD_ID, name);
    }
}

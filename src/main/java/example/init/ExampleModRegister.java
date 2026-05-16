package example.init;

import example.block.TestBlock;
import example.block.blockentity.TestBlockEntity;
import example.entity.Zti;
import example.item.DeagleItem;
import example.item.ExampleArmorItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class ExampleModRegister {
    /**
     * 注册名用 example，方便 build 时排除
     */
    public static final String MOD_ID = "example";

    public static Block TEST_BLOCK;
    public static BlockEntityType<TestBlockEntity> TEST_BLOCK_ENTITY_TYPE;
    public static EntityType<Zti> ZTI_ENTITY_TYPE;
    public static BlockItem TEST_BLOCK_ITEM;
    public static Item DEAGLE_ITEM;

    public static ExampleArmorItem DEFENDER_ARMOR_HELMET;
    public static ExampleArmorItem DEFENDER_ARMOR_CHESTPLATE;
    public static ExampleArmorItem DEFENDER_ARMOR_LEGGINGS;
    public static ExampleArmorItem DEFENDER_ARMOR_BOOTS;

    public static SpawnEggItem ZTI_SPAWN_EGG;
    public static ItemGroup TEST_TAB;

    private static boolean registered;

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;

        TEST_BLOCK = Registry.register(Registries.BLOCK, modLoc("test_block"), new TestBlock());
        TEST_BLOCK_ENTITY_TYPE = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                modLoc("test_block_entity_type"),
                BlockEntityType.Builder.create(TestBlockEntity::new, TEST_BLOCK).build(null)
        );
        ZTI_ENTITY_TYPE = Registry.register(
                Registries.ENTITY_TYPE,
                modLoc("zti"),
                EntityType.Builder.create(Zti::new, SpawnGroup.MONSTER)
                        .dimensions(2.2F, 2.5F)
                        .build(modLoc("zti").toString())
        );

        TEST_BLOCK_ITEM = Registry.register(Registries.ITEM, modLoc("test_block_item"), new BlockItem(TEST_BLOCK, new Item.Settings()));
        DEAGLE_ITEM = Registry.register(Registries.ITEM, modLoc("deagle"), new DeagleItem());
        DEFENDER_ARMOR_HELMET = Registry.register(Registries.ITEM, modLoc("defender_helmet"), new ExampleArmorItem(ArmorItem.Type.HELMET));
        DEFENDER_ARMOR_CHESTPLATE = Registry.register(Registries.ITEM, modLoc("defender_chestplate"), new ExampleArmorItem(ArmorItem.Type.CHESTPLATE));
        DEFENDER_ARMOR_LEGGINGS = Registry.register(Registries.ITEM, modLoc("defender_leggings"), new ExampleArmorItem(ArmorItem.Type.LEGGINGS));
        DEFENDER_ARMOR_BOOTS = Registry.register(Registries.ITEM, modLoc("defender_boots"), new ExampleArmorItem(ArmorItem.Type.BOOTS));
        ZTI_SPAWN_EGG = Registry.register(Registries.ITEM, modLoc("zti_spawn_egg"), new SpawnEggItem(ZTI_ENTITY_TYPE, 0x61554D, 0xD8B076, new Item.Settings()));

        TEST_TAB = Registry.register(
                Registries.ITEM_GROUP,
                modLoc("test_tab"),
                FabricItemGroup.builder()
                        .displayName(Text.translatable("item_group.example.name"))
                        .icon(() -> new ItemStack(DEAGLE_ITEM))
                        .entries((context, entries) -> {
                            entries.add(TEST_BLOCK_ITEM);
                            entries.add(DEAGLE_ITEM);
                            entries.add(DEFENDER_ARMOR_HELMET);
                            entries.add(DEFENDER_ARMOR_CHESTPLATE);
                            entries.add(DEFENDER_ARMOR_LEGGINGS);
                            entries.add(DEFENDER_ARMOR_BOOTS);
                            entries.add(ZTI_SPAWN_EGG);
                        })
                        .build()
        );

        FabricDefaultAttributeRegistry.register(ZTI_ENTITY_TYPE, Zti.createAttributes());
    }

    public static Identifier modLoc(String name) {
        return Identifier.of(MOD_ID, name);
    }

    private ExampleModRegister() {}
}

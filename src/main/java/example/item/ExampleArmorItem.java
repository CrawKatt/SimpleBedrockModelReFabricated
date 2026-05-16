package example.item;

import com.github.mcmodderanchor.simplebedrockmodel.v1.client.renderer.GeoArmorRenderer;
import example.init.ExampleModRegister;
import example.resource.InnerResourceLoader;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

//@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class ExampleArmorItem extends ArmorItem {

    public ExampleArmorItem(ArmorItem.Type type) {
        super(ArmorMaterials.DIAMOND, type, new Item.Settings().maxCount(1));
    }

    //@SubscribeEvent
    public static void initializeClient(RegisterClientExtensionsEvent event) {
        for (var item : List.of(ExampleModRegister.DEFENDER_ARMOR_BOOTS, ExampleModRegister.DEFENDER_ARMOR_CHESTPLATE, ExampleModRegister.DEFENDER_ARMOR_HELMET, ExampleModRegister.DEFENDER_ARMOR_LEGGINGS)) {
            event.registerItem(new IClientItemExtensions() {
                private GeoArmorRenderer renderer;

                @Override
                public @NotNull BipedEntityModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, BipedEntityModel<?> original) {
                    if (this.renderer == null) {
                        this.renderer = new GeoArmorRenderer(
                                InnerResourceLoader.DEFENDER_MODEL,
                                Identifier.of("example", "textures/armor/defender.png")
                        );
                    }

                    this.renderer.preparePose(livingEntity, itemStack, equipmentSlot, original);

                    return this.renderer;
                }
            }, item);
        }
    }
}

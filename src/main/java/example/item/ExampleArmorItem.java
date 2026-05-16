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
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ExampleArmorItem extends ArmorItem {

    public ExampleArmorItem(ArmorItem.Type type) {
        super(ArmorMaterials.DIAMOND, type, new Item.Settings().maxCount(1));
    }

    public static void initializeClient(RegisterClientExtensionsEvent event) {
        Identifier texture = Identifier.of("example", "textures/armor/defender.png");
        for (var item : List.of(ExampleModRegister.DEFENDER_ARMOR_BOOTS, ExampleModRegister.DEFENDER_ARMOR_CHESTPLATE, ExampleModRegister.DEFENDER_ARMOR_HELMET, ExampleModRegister.DEFENDER_ARMOR_LEGGINGS)) {
            event.registerItem(new IClientItemExtensions() {
                private GeoArmorRenderer renderer;

                @Override
                public @NotNull BipedEntityModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, BipedEntityModel<?> original) {
                    if (this.renderer == null && InnerResourceLoader.DEFENDER_MODEL != null) {
                        this.renderer = new GeoArmorRenderer(InnerResourceLoader.DEFENDER_MODEL, texture);
                    }
                    if (this.renderer == null) {
                        return original;
                    }

                    this.renderer.preparePose(livingEntity, itemStack, equipmentSlot, original);
                    return this.renderer;
                }
            }, item);
        }
    }
}

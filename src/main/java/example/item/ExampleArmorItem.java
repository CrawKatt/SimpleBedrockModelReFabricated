package example.item;

import com.github.mcmodderanchor.simplebedrockmodel.v1.client.renderer.GeoArmorRenderer;
import com.github.mcmodderanchor.simplebedrockmodel.v1.registry.GeoArmorRendererRegistry;
import example.init.ExampleModRegister;
import example.resource.InnerResourceLoader;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.function.Supplier;

public class ExampleArmorItem extends ArmorItem {

    public ExampleArmorItem(ArmorItem.Type type) {
        super(ArmorMaterials.DIAMOND, type, new Item.Settings().maxCount(1));
    }

    public static void registerClient() {
        Identifier texture = Identifier.of("example", "textures/armor/defender.png");
        Supplier<GeoArmorRenderer> rendererSupplier = () -> {
            if (InnerResourceLoader.DEFENDER_MODEL == null) {
                return null;
            }
            return new GeoArmorRenderer(InnerResourceLoader.DEFENDER_MODEL, texture);
        };

        for (var item : List.of(ExampleModRegister.DEFENDER_ARMOR_BOOTS, ExampleModRegister.DEFENDER_ARMOR_CHESTPLATE, ExampleModRegister.DEFENDER_ARMOR_HELMET, ExampleModRegister.DEFENDER_ARMOR_LEGGINGS)) {
            GeoArmorRendererRegistry.register(item, rendererSupplier);
            ArmorRenderer.register((matrices, vertexConsumers, stack, entity, slot, light, contextModel) -> {
                GeoArmorRenderer renderer = rendererSupplier.get();
                if (renderer == null) {
                    return;
                }
                renderer.preparePose(entity, stack, slot, contextModel);
                renderer.render(matrices, vertexConsumers.getBuffer(renderer.getRenderLayer(renderer.getTexture())), light, OverlayTexture.DEFAULT_UV, 0xFFFFFFFF);
            }, item);
        }
    }
}

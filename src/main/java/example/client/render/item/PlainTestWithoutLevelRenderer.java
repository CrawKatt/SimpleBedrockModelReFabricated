package example.client.render.item;

import com.github.mcmodderanchor.simplebedrockmodel.v1.client.render.backend.BedrockRenderDispatcher;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockModel;
import com.github.mcmodderanchor.simplebedrockmodel.v1.event.RegisterBedrockModelReloadListenerEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import example.init.ExampleModRegister;
import example.resource.KnownResources;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.ParametersAreNonnullByDefault;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class PlainTestWithoutLevelRenderer extends BlockEntityWithoutLevelRenderer {
    private static final Material material = new Material(TextureAtlas.LOCATION_BLOCKS, KnownResources.PLAIN_TEST.withPrefix("item/"));

    private static BedrockModel model;

    public PlainTestWithoutLevelRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }
    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModelReloadListenerRegister2 {
        @SubscribeEvent
        public static void onModelReloadListenerRegister(RegisterBedrockModelReloadListenerEvent event) {
            event.register(map -> {
                model = map.get(KnownResources.PLAIN_TEST);
            });
        }
    }

    @SubscribeEvent
    public static void onFirstPersonRender(RenderHandEvent event) {
        if (event.getItemStack().getItem() == ExampleModRegister.PLAIN_TEST_ITEM && event.getHand() == InteractionHand.MAIN_HAND) {

            PoseStack poseStack = event.getPoseStack();

            poseStack.pushPose();
            {
                poseStack.translate(0.125, -0.5, -1.03125);
                BedrockRenderDispatcher.render(model, poseStack, event.getMultiBufferSource(),
                        material, RenderType::entityCutout, event.getPackedLight(), OverlayTexture.NO_OVERLAY);
            }
            poseStack.popPose();
        }
    }

    @ParametersAreNonnullByDefault
    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext ctx, PoseStack poseStack, MultiBufferSource bufferSource,
                             int light, int overlay) {
        if (ctx.firstPerson()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);
        BedrockRenderDispatcher.render(model, poseStack, bufferSource,
                material, RenderType::entityCutout, light, overlay);
        poseStack.popPose();
    }
}

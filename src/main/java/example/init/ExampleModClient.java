package example.init;

import com.github.mcmodderanchor.simplebedrockmodel.v1.client.event.SimpleBedrockModelEvents;
import example.animation.DeagleAnimationGraph;
import example.animation.TestBlockAnimationContext;
import example.animation.ZtiAnimationContext;
import example.client.event.ClientTicker;
import example.client.render.blockentity.TestBlockEntityRenderer;
import example.client.render.entity.ZtiRenderer;
import example.client.render.item.DeagleWithoutLevelRenderer;
import example.item.ExampleArmorItem;
import example.resource.InnerResourceLoader;
import example.resource.KnownResources;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

public class ExampleModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SimpleBedrockModelEvents.REGISTER_CLIENT_ANIMATIONS.register(KnownResources::onAnimationRegister);
        SimpleBedrockModelEvents.REGISTER_CLIENT_MODELS.register(KnownResources::onModelRegister);
        SimpleBedrockModelEvents.REGISTER_CLIENT_ANIMATIONS.register(InnerResourceLoader::onAnimationRegister);
        SimpleBedrockModelEvents.REGISTER_CLIENT_MODELS.register(InnerResourceLoader::onModelRegister);
        SimpleBedrockModelEvents.REGISTER_CLIENT_MODEL_RELOAD_LISTENERS.register(InnerResourceLoader::onModelLoaded);
        SimpleBedrockModelEvents.REGISTER_CLIENT_MODEL_RELOAD_LISTENERS.register(DeagleAnimationGraph::onRegisterModelReloadListener);
        SimpleBedrockModelEvents.REGISTER_CLIENT_ANIMATION_RELOAD_LISTENERS.register(DeagleAnimationGraph::onRegisterAnimationReloadListener);
        SimpleBedrockModelEvents.REGISTER_CLIENT_ANIMATION_RELOAD_LISTENERS.register(TestBlockAnimationContext::onAnimationReloadListenerRegister);
        SimpleBedrockModelEvents.REGISTER_CLIENT_ANIMATION_RELOAD_LISTENERS.register(ZtiAnimationContext::onAnimationReloadListenerRegister);
        SimpleBedrockModelEvents.REGISTER_CLIENT_MODEL_RELOAD_LISTENERS.register(DeagleWithoutLevelRenderer.ModelReloadListenerRegister::onModelReloadListenerRegister);
        SimpleBedrockModelEvents.RENDER_HAND.register(DeagleWithoutLevelRenderer::onFirstPersonRender);

        EntityRendererRegistry.register(ExampleModRegister.ZTI_ENTITY_TYPE, ZtiRenderer::new);
        BlockEntityRendererFactories.register(ExampleModRegister.TEST_BLOCK_ENTITY_TYPE, TestBlockEntityRenderer::new);
        DeagleWithoutLevelRenderer deagleRenderer = new DeagleWithoutLevelRenderer();
        BuiltinItemRendererRegistry.INSTANCE.register(ExampleModRegister.DEAGLE_ITEM, deagleRenderer::render);

        ExampleArmorItem.initializeClient(new RegisterClientExtensionsEvent());
        ClientTicker.register();
    }
}

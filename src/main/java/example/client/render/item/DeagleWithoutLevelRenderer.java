package example.client.render.item;

import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockBone;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockModel;
import com.github.mcmodderanchor.simplebedrockmodel.v1.event.RegisterBedrockModelReloadListenerEvent;
import com.mojang.blaze3d.systems.RenderSystem;
import example.animation.GunAnimationGraph;
import example.capability.FPGunAnimationCapability;
import example.init.ExampleModRegister;
import example.resource.KnownResources;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

//@EventBusSubscriber(value = Dist.CLIENT)
public class DeagleWithoutLevelRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
    private static final SpriteIdentifier material = new SpriteIdentifier(net.minecraft.screen.PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Identifier.of(KnownResources.DEAGLE.getNamespace(), "item/" + KnownResources.DEAGLE.getPath()));

    private static BedrockModel model;

    // 暂时只能想到这么丑的办法
    //@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static class ModelReloadListenerRegister {
        //@SubscribeEvent
        public static void onModelReloadListenerRegister(RegisterBedrockModelReloadListenerEvent event) {
            event.register(map -> {
                model = map.get(KnownResources.DEAGLE);
                BedrockBone leftHandBone = model.getBone("lefthand_pos");
                BedrockBone rightHandBone = model.getBone("righthand_pos");
                if (leftHandBone != null) {
                    leftHandBone.visible = false;
                }
                if (rightHandBone != null) {
                    rightHandBone.visible = false;
                }
            });
        }
    }

    public DeagleWithoutLevelRenderer() {
        //super(MinecraftClient.getInstance().getBlockEntityRenderDispatcher(), MinecraftClient.getInstance().getEntityModels());
    }

    public static boolean onFirstPersonRender(Hand hand, ItemStack stack, MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, int packedLight, float partialTick) {
        if (stack.getItem() == ExampleModRegister.DEAGLE_ITEM && hand == Hand.MAIN_HAND && model != null) {
            MinecraftClient mc = MinecraftClient.getInstance();
            // 从 AnimationInstance 中获取 AnimationGraph，然后计算当前帧的 Pose，然后混合并 apply
            if (mc.getCameraEntity() instanceof PlayerEntity player) {
                var cap = FPGunAnimationCapability.get(player);
                GunAnimationGraph animationGraph = cap.getAnimationInstance().getAnimationGraph();
                if (animationGraph != null) {
                    model.applyPose(animationGraph.getPose());
                }
            }
            matrixStack.push();
            {
                // 反转 Bobbing
                if (mc.options.getBobView().getValue() && (mc.getCameraEntity() instanceof PlayerEntity player)) {
                    float f = player.horizontalSpeed - player.prevHorizontalSpeed;
                    float f1 = -(player.horizontalSpeed + f * partialTick);
                    float f2 = MathHelper.lerp(partialTick, player.prevStrideDistance, player.strideDistance);
                    matrixStack.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(Math.abs(MathHelper.cos(f1 * (float)Math.PI - 0.2F) * f2) * 5.0F));
                    matrixStack.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(MathHelper.sin(f1 * (float)Math.PI) * f2 * 3.0F));
                    matrixStack.translate(-MathHelper.sin(f1 * (float)Math.PI) * f2 * 0.5F, Math.abs(MathHelper.cos(f1 * (float)Math.PI) * f2), 0.0F);
                }
                // 这里的 translate 是为了把枪放到合适位置，为了方便直接硬编码了
                matrixStack.translate(0.125, -0.5, -1.03125);
                // 执行渲染
                VertexConsumer buffer = material.getVertexConsumer(vertexConsumers, RenderLayer::getEntityCutout);
                model.renderToBuffer(matrixStack, buffer, packedLight, OverlayTexture.DEFAULT_UV);
                // 渲染手臂
                if (mc.getCameraEntity() instanceof AbstractClientPlayerEntity abstractClientPlayer) {
                    BedrockBone leftHandBone = model.getBone("lefthand_pos");
                    BedrockBone rightHandBone = model.getBone("righthand_pos");
                    RenderSystem.setShaderTexture(0, abstractClientPlayer.getSkinTextures().texture());
                    PlayerEntityRenderer playerRenderer = (PlayerEntityRenderer)mc.getEntityRenderDispatcher().getRenderer(abstractClientPlayer);
                    if (leftHandBone != null) {
                        Matrix4f globalTransform = leftHandBone.getGlobalTransform();
                        matrixStack.push();
                        matrixStack.multiplyPositionMatrix(globalTransform);
                        playerRenderer.renderLeftArm(matrixStack, vertexConsumers, packedLight, abstractClientPlayer);
                        matrixStack.pop();
                    }
                    if (rightHandBone != null) {
                        Matrix4f globalTransform = rightHandBone.getGlobalTransform();
                        matrixStack.push();
                        matrixStack.multiplyPositionMatrix(globalTransform);
                        playerRenderer.renderRightArm(matrixStack, vertexConsumers, packedLight, abstractClientPlayer);
                        matrixStack.pop();
                    }
                }
            }
            matrixStack.pop();
            // 恢复被动画影响的模型
            model.applyPose(model.getBindPose());
            return true;
        }
        return false;
    }

    @Override
    public void render(ItemStack itemStack, ModelTransformationMode ctx, MatrixStack matrixStack, VertexConsumerProvider bufferSource,
                       int light, int overlay) {
        // 第一人称不用这个渲染，而是改为监听 RenderHandEvent 渲染。这里测试实现的比较粗糙，实际生产环境需要斟酌
        if (ctx.isFirstPerson() || model == null) {
            return;
        }
        matrixStack.push();
        matrixStack.translate(0.5, 0, 0.5);
        VertexConsumer buffer = material.getVertexConsumer(bufferSource, RenderLayer::getEntityCutout);
        model.renderToBuffer(matrixStack, buffer, light, overlay);
        matrixStack.pop();
    }

    private static void resetRotation(ModelPart part) {
        part.pitch = 0;
        part.yaw = 0;
        part.roll = 0;
    }
}

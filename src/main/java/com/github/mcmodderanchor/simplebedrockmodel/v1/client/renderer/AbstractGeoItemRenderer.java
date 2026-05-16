package com.github.mcmodderanchor.simplebedrockmodel.v1.client.renderer;

import com.github.mcmodderanchor.simplebedrockmodel.v1.client.animation.IFPAnimationInstance;
import com.github.mcmodderanchor.simplebedrockmodel.v1.client.model.PositionableModel;
import com.github.mcmodderanchor.simplebedrockmodel.v1.client.model.SlotModel;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockModel;
import com.github.mcmodderanchor.simplebedrockmodel.v1.util.RenderDistance;
import com.maydaymemory.mae.basic.YXZRotationView;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3fc;

/**
 * 第一人称基岩模型的抽象 BEWLR，包含了一些基础实现，如摄像机动画的应用、定位组的应用。
 *
 * @param <M> 基岩版模型
 */
public abstract class AbstractGeoItemRenderer<M extends BedrockModel>
        extends BuiltinModelItemRenderer implements IFPGeoItemRenderer {
    public static final String FP_CAMERA_BONE_NAME = "camera";
    private static final SlotModel SLOT_MODEL = new SlotModel();

    public AbstractGeoItemRenderer() {
        super(MinecraftClient.getInstance().getBlockEntityRenderDispatcher(), MinecraftClient.getInstance().getEntityModelLoader());
    }

    @Nullable
    public abstract Pair<M, RenderLayer> getModelAndRenderLayer(ItemStack stack);

    @Nullable
    public abstract Pair<M, RenderLayer> getLodModelAndRenderLayer(ItemStack stack);

    @Nullable
    public abstract Identifier getSlotTexture(ItemStack stack);

    /**
     * 应用摄像机动画对世界的变换（只有旋转生效）
     */
    public void applyLevelCameraAnimation(float[] cameraAngles, ItemStack stack, Quaternionf animateRot, float partialTicks) {
        Quaternionf initialRotation = new Quaternionf().rotateYXZ(-cameraAngles[0], -cameraAngles[1], -cameraAngles[2]);
        YXZRotationView rotationView = new YXZRotationView(initialRotation.mul(animateRot));
        Vector3fc eulerAngle = rotationView.asEulerAngle();
        cameraAngles[0] = -eulerAngle.y();
        cameraAngles[1] = -eulerAngle.x();
        cameraAngles[2] = -eulerAngle.z();
    }

    /**
     * 应用摄像机动画对手持物品的变换（只有旋转生效）
     */
    public void applyItemInHandCameraAnimation(MatrixStack poseStack, ItemStack stack, Quaternionf animateRot, float partialTicks) {
        poseStack.multiply(animateRot);
    }

    /**
     * 渲染模型前调用。默认会应用定位组变换。可以用于施加动画的影响。
     */
    protected void beforeRender(MatrixStack poseStack, ModelTransformationMode ctx, M model, ItemStack stack, float partialTicks) {
        if (ctx == ModelTransformationMode.GROUND) {
            poseStack.translate(0.5, 0.3125, 0.5);
        } else if (!ctx.isFirstPerson()) {
            poseStack.translate(0.5, 0.5, 0.5);
        }
        if (model instanceof PositionableModel positionableBedrockModel) {
            positionableBedrockModel.applyTransform(poseStack, ctx);
        }
    }

    /**
     * 渲染模型后调用。可以做一些清理工作，例如将 bind pose 应用给模型以清除动画影响。默认什么都不会做。
     */
    protected void afterRender(MatrixStack poseStack, ModelTransformationMode ctx, M model, ItemStack stack, VertexConsumerProvider bufferSource,
                               int light, float partialTicks) {
    }

    @Override
    public void renderFirstPerson(ClientPlayerEntity player, ItemStack stack, ModelTransformationMode ctx, MatrixStack poseStack,
                                  VertexConsumerProvider bufferSource, int light, float partialTick) {
        // 默认的左右手位移
        int i = ctx == ModelTransformationMode.FIRST_PERSON_RIGHT_HAND ? 1 : -1; // ItemDisplayContext -> ModelTransformationMode
        poseStack.translate((float) i * 0.5F, -0.75F, -0.75F);
        render(stack, ctx, poseStack, bufferSource, light, OverlayTexture.DEFAULT_UV, partialTick); // NO_OVERLAY -> DEFAULT_UV
    }

    @Override
    public void render(ItemStack stack, ModelTransformationMode ctx, MatrixStack poseStack,
                       VertexConsumerProvider bufferSource, int light, int overlay) {
        if (ctx.isFirstPerson()) {
            return;
        }
        render(stack, ctx, poseStack, bufferSource, light, overlay, MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true));
    }

    protected void render(ItemStack stack, ModelTransformationMode ctx, MatrixStack poseStack,
                          VertexConsumerProvider bufferSource, int light, int overlay, float partialTicks) {
        Pair<M, RenderLayer> modelAndRenderLayer = null;
        // 如果不在高模渲染距离内，则尝试获取低模，如果低模不存在，仍然用高模作为 fallback
        if (!RenderDistance.inRenderHighPolyModelDistance(poseStack, 16) && !ctx.isFirstPerson()) {
            modelAndRenderLayer = getLodModelAndRenderLayer(stack);
            if (modelAndRenderLayer == null) {
                modelAndRenderLayer = getModelAndRenderLayer(stack);
            }
        } else {
            modelAndRenderLayer = getModelAndRenderLayer(stack);
        }
        if (ctx == ModelTransformationMode.GUI || modelAndRenderLayer == null) { // ItemDisplayContext.GUI -> ModelTransformationMode.GUI
            renderSlot(stack, poseStack, bufferSource, light, overlay, modelAndRenderLayer);
            return;
        }
        poseStack.push();
        M model = modelAndRenderLayer.getLeft();
        beforeRender(poseStack, ctx, model, stack, partialTicks);
        RenderLayer renderType = modelAndRenderLayer.getRight();
        model.renderToBuffer(poseStack, bufferSource.getBuffer(renderType), light, overlay);
        afterRender(poseStack, ctx, model, stack, bufferSource, light, partialTicks);
        poseStack.pop();
    }

    public void renderSlot(ItemStack stack, MatrixStack poseStack, VertexConsumerProvider bufferSource, int light, int overlay, Pair<M, RenderLayer> modelAndRenderLayer) { // MultiBufferSource -> VertexConsumerProvider
        Identifier slotTexture = getSlotTexture(stack);
        if (slotTexture != null) {
            poseStack.push();
            poseStack.translate(0.5, 0.5, 0);
            SLOT_MODEL.render(poseStack, bufferSource.getBuffer(RenderLayer.getEntityTranslucent(slotTexture)), light, overlay, 0xFFFFFFFF);
            poseStack.pop();
        } else if (modelAndRenderLayer == null) {
            // 模型和 gui texture 都不存在，渲染 missing texture
            poseStack.push();
            poseStack.translate(0.5, 0.5, 0);
            // MissingTextureAtlasSprite.getLocation() -> MissingSprite.getMissingSpriteId()
            RenderLayer renderType1 = RenderLayer.getEntityTranslucent(MissingSprite.getMissingSpriteId());
            SLOT_MODEL.render(poseStack, bufferSource.getBuffer(renderType1), light, overlay, 0xFFFFFFFF);
            poseStack.pop();
        }
    }

    /**
     * 使用该渲染器的物品会阻止原版的viewBobbing，以便应用自定义的跑步/走路动画。
     * @return 是否阻止原版viewBobbing
     */
    public boolean blockViewBobbing() {
        return true;
    }

    @Nullable
    public IFPAnimationInstance createAnimationInstance(ItemStack stack, Entity entity) {
        return null;
    }

    /**
     * Check if the given ItemStack should be considered the same as the current one.
     * If false is returned, a new IFPAnimationInstance will be created for the new item.
     * @param oldStack current item stack
     * @param newStack the new item stack
     * @return true if the items are considered the same, false otherwise
     */
    public boolean isSameItem(ItemStack oldStack, ItemStack newStack) {
        return ItemStack.areEqual(oldStack, newStack);
    }

    public long getPutAwayDuration(ItemStack stack) {
        return 0;
    }

    public boolean blockOffhandRender() {
        return false;
    }
}

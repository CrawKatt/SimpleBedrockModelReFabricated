package com.github.mcmodderanchor.simplebedrockmodel.v1.client.renderer;

import com.github.mcmodderanchor.simplebedrockmodel.v1.client.model.BedrockArmorModel;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockBone;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// 说是模型，实际上是一个适配器，用来敷衍原版的）
public class GeoArmorRenderer extends BipedEntityModel {
    @Nullable
    protected final BedrockArmorModel model;
    private final Identifier texture;

    @Nullable
    protected LivingEntity livingEntity;
    @Nullable
    protected ItemStack itemStack;
    @Nullable
    protected EquipmentSlot equipmentSlot;
    @Nullable
    protected BipedEntityModel<?> original;

    public GeoArmorRenderer(BedrockArmorModel origin, Identifier texture) {
        super(MinecraftClient.getInstance().getEntityModelLoader().getModelPart(EntityModelLayers.PLAYER_INNER_ARMOR));
        this.model = origin;
        this.texture = texture;
    }

    public void preparePose(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, BipedEntityModel<?> original) {
        if (model == null) {
            return;
        }

        model.applyPose(model.getBindPose());

        copyModelPart(original.head, model.getArmorHead(), 0, 24, 0);
        copyModelPart(original.body, model.getArmorBody(), 0, 24, 0);
        copyModelPart(original.rightArm, model.getArmorRightArm(), 5, 22, 0);
        copyModelPart(original.leftArm, model.getArmorLeftArm(), -5, 22, 0);
        copyModelPart(original.rightLeg, model.getArmorRightLeg(), 1.9f, 12, 0);
        copyModelPart(original.leftLeg, model.getArmorLeftLeg(), -1.9f, 12, 0);
        copyModelPart(original.rightLeg, model.getArmorRightBoot(), 1.9f, 12, 0);
        copyModelPart(original.leftLeg, model.getArmorLeftBoot(), -1.9f, 12, 0);

        setVisibilityBySlot(equipmentSlot);

        this.livingEntity = livingEntity;
        this.itemStack = itemStack;
        this.equipmentSlot = equipmentSlot;
        this.original = original;
    }

    public void copyModelPart(ModelPart part, BedrockBone bone, float initX, float initY, float initZ) {
        if (bone != null) {
            float deltaX = part.pivotX - initX;
            float deltaY = part.pivotY - initY;
            float deltaZ = part.pivotZ - initZ;

            bone.x += deltaX;
            bone.y += deltaY;
            bone.z += deltaZ;

            bone.rotation.rotationZYX(part.roll, part.yaw, part.pitch);

            bone.xScale = -part.xScale;
            bone.yScale = -part.yScale;
            bone.zScale = part.zScale;
            bone.visible = part.visible;
        }
    }

    public void setVisibilityBySlot(EquipmentSlot slot) {
        if (model == null) {
            return;
        }

        setBoneVisible(model.getArmorHead(), slot == EquipmentSlot.HEAD);
        setBoneVisible(model.getArmorBody(), slot == EquipmentSlot.CHEST);
        setBoneVisible(model.getArmorRightArm(), slot == EquipmentSlot.CHEST);
        setBoneVisible(model.getArmorLeftArm(), slot == EquipmentSlot.CHEST);
        setBoneVisible(model.getArmorRightLeg(), slot == EquipmentSlot.LEGS);
        setBoneVisible(model.getArmorLeftLeg(), slot == EquipmentSlot.LEGS);
        setBoneVisible(model.getArmorRightBoot(), slot == EquipmentSlot.FEET);
        setBoneVisible(model.getArmorLeftBoot(), slot == EquipmentSlot.FEET);
    }

    public void setBoneVisible(BedrockBone bone, boolean visible) {
        if (bone != null) {
            bone.visible = visible;
        }
    }

    public void scaleModelForBaby(MatrixStack poseStack, LivingEntity livingEntity, float partialTick, EquipmentSlot slot,
                                  BipedEntityModel<?> original) {
        // En Yarn, AnimalModel define 'child' en lugar de 'young'
        if (!this.child)
            return;

        if (slot == EquipmentSlot.HEAD) {
            float headScale = 1.5f / 2.0f;

            poseStack.scale(headScale, headScale, headScale);
            poseStack.translate(0, 1.0f, 0f);
        } else {
            float bodyScale = 1f / 2.0f;

            poseStack.scale(bodyScale, bodyScale, bodyScale);
            poseStack.translate(0, 24.0f / 16f, 0);
        }
    }

    @Override
    public void render(MatrixStack poseStack, @NotNull VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        if (model == null) {
            afterRender(poseStack, buffer, packedLight, packedOverlay, 0, 0, 0, 0);
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        VertexConsumerProvider bufferSource = mc.getBufferBuilders().getEntityVertexConsumers();
        var vertexConsumer = bufferSource.getBuffer(this.getRenderLayer(this.getTexture()));

        float partialTick = mc.getRenderTickCounter().getTickDelta(true);

        poseStack.push();
        if (this.livingEntity != null && this.equipmentSlot != null && this.original != null) {
            scaleModelForBaby(poseStack, this.livingEntity, partialTick, this.equipmentSlot, this.original);
        }

        var r = ColorHelper.Argb.getRed(color) / 255F;
        var g = ColorHelper.Argb.getGreen(color) / 255F;
        var b = ColorHelper.Argb.getBlue(color) / 255F;
        var a = ColorHelper.Argb.getAlpha(color) / 255F;

        model.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        poseStack.pop();

        afterRender(poseStack, buffer, packedLight, packedOverlay, r, g, b, a);
    }

    public void afterRender(MatrixStack poseStack, VertexConsumer buffer, int light, int overlay,
                            float r, float g, float b, float a) {
        this.livingEntity = null;
        this.itemStack = null;
        this.equipmentSlot = null;
        this.original = null;
    }

    public RenderLayer getRenderLayer(Identifier texture) {
        return RenderLayer.getArmorCutoutNoCull(texture);
    }

    public Identifier getTexture() {
        return this.texture;
    }

    @Nullable
    public BedrockArmorModel getModel() {
        return this.model;
    }
}

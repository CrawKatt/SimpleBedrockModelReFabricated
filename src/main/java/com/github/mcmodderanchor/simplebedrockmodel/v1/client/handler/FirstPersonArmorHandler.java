package com.github.mcmodderanchor.simplebedrockmodel.v1.client.handler;

import com.github.mcmodderanchor.simplebedrockmodel.v1.client.renderer.GeoArmorRenderer;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockBone;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

/**
 * 通用的第一人称盔甲手臂渲染处理器。
 * 监听 RenderArmEvent，在玩家手臂上叠加渲染 Bedrock 盔甲模型的手臂部分。
 */
//@EventBusSubscriber(Dist.CLIENT)
public class FirstPersonArmorHandler {

    private static BipedEntityModel<?> defaultModel;

    private static BipedEntityModel<?> getDefaultModel() {
        if (defaultModel == null) {
            defaultModel = new BipedEntityModel<>(
                    MinecraftClient.getInstance().getEntityModelLoader().getModelPart(EntityModelLayers.PLAYER_INNER_ARMOR)
            );
        }
        return defaultModel;
    }

    //@SubscribeEvent
    public static void onRenderArm(RenderArmEvent event) {
        AbstractClientPlayerEntity player = event.getPlayer();
        Arm arm = event.getArm();

        ItemStack chestStack = player.getEquippedStack(EquipmentSlot.CHEST);
        if (chestStack.isEmpty()) return;

        IClientItemExtensions ext = IClientItemExtensions.of(chestStack.getItem());
        var model = ext.getHumanoidArmorModel(player, chestStack, EquipmentSlot.CHEST, getDefaultModel());
        if (!(model instanceof GeoArmorRenderer geoRenderer)) return;

        BedrockBone armBone = arm == Arm.RIGHT
                ? geoRenderer.getModel().getArmorRightArm()
                : geoRenderer.getModel().getArmorLeftArm();
        if (armBone == null) return;

        RenderLayer renderType = geoRenderer.getRenderLayer(geoRenderer.getTexture());
        VertexConsumer consumer = event.getMultiBufferSource().getBuffer(renderType);

        MatrixStack matrixStack = event.getPoseStack();
        matrixStack.push();

        matrixStack.multiplyPositionMatrix(getGlobalTransform(armBone));

        armBone.render(matrixStack, consumer, event.getPackedLight(), OverlayTexture.DEFAULT_UV);

        matrixStack.pop();
    }

    // 取得骨骼除了自身变换以外的全局变换矩阵
    public static Matrix4f getGlobalTransform(@NotNull BedrockBone targetBone) {
        Matrix4f matrix = new Matrix4f();

        for (BedrockBone bone = targetBone.parent; bone != null; bone = bone.parent) {
            matrix.scaleLocal(bone.xScale, bone.yScale, bone.zScale);
            matrix.rotateLocal(bone.rotation);
            matrix.translateLocal(bone.x / 16.0F, bone.y / 16.0F, bone.z / 16.0F);
        }

        return matrix;
    }
}

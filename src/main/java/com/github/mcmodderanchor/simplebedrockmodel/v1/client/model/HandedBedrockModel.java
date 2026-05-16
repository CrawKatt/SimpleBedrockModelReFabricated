package com.github.mcmodderanchor.simplebedrockmodel.v1.client.model;

import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockBone;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.BedrockModelPOJO;
import com.github.mcmodderanchor.simplebedrockmodel.v1.util.RenderHelper;
import com.github.mcmodderanchor.simplebedrockmodel.v1.util.math.MathUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

/**
 * 带有双臂渲染的基岩模型，用于第一人称视角下的武器等物品的渲染。
 */
public class HandedBedrockModel extends BedrockModelBase {
    private boolean renderHand = true;
    private final BedrockBone leftHandBone;
    private final BedrockBone rightHandBone;

    public HandedBedrockModel(BedrockModelPOJO pojo, @Nullable TransformScale scales) {
        super(pojo, scales);
        leftHandBone = getBone(this.getLeftHandBoneName());
        rightHandBone = getBone(this.getRightHandBoneName());
        if (leftHandBone != null) {
            leftHandBone.visible = false;
        }
        if (rightHandBone != null) {
            rightHandBone.visible = false;
        }
    }

    @NotNull
    public String getLeftHandBoneName() {
        return "lefthand_pos";
    }

    @NotNull
    public String getRightHandBoneName() {
        return "righthand_pos";
    }

    public void renderToBuffer(MatrixStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
        // 渲染枪械
        super.renderToBuffer(poseStack, buffer, packedLight, packedOverlay);
        // 渲染双臂
        if (renderHand) {
            renderHands(poseStack, buffer, packedLight, packedOverlay);
        }
    }

    public void renderHands(MatrixStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
        if (leftHandBone != null) {
            Matrix4f transform = leftHandBone.getGlobalTransform();
            matrixStack.push();
            MathUtil.mulMatrix(matrixStack, transform);
            RenderHelper.renderFirstPersonArm(MinecraftClient.getInstance().player, Arm.LEFT, matrixStack, packedLight);
            matrixStack.pop();
        }
        if (rightHandBone != null) {
            Matrix4f transform = rightHandBone.getGlobalTransform();
            matrixStack.push();
            MathUtil.mulMatrix(matrixStack, transform);
            RenderHelper.renderFirstPersonArm(MinecraftClient.getInstance().player, Arm.RIGHT, matrixStack, packedLight);
            matrixStack.pop();
        }
    }

    public boolean isRenderHand() {
        return renderHand;
    }

    public void setRenderHand(boolean renderHand) {
        this.renderHand = renderHand;
    }
}

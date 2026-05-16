package com.github.mcmodderanchor.simplebedrockmodel.v1.client.handler;

import com.github.mcmodderanchor.simplebedrockmodel.v1.client.renderer.AbstractGeoItemRenderer;
import com.github.mcmodderanchor.simplebedrockmodel.v1.registry.GeoItemRendererRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.*;

@Environment(EnvType.CLIENT)
public class CameraEventHandler {

    // 测试用
    public static Vector3f extractEulerAnglesYXZ(Matrix3fc matrix) {
        Vector3f euler = new Vector3f();

        float r32 = matrix.m21();
        float r12 = matrix.m01();
        float r22 = matrix.m11();
        float r31 = matrix.m20();
        float r33 = matrix.m22();

        final float EPSILON = 1e-4f;

        // Clamp r32 to the range [-1, 1] to avoid NaN from asin due to floating point inaccuracies
        r32 = org.joml.Math.clamp(r32, -1.0f, 1.0f);

        if (org.joml.Math.abs(r32) < 1.0f - EPSILON) {
            euler.x = org.joml.Math.asin(-r32);
            euler.y = org.joml.Math.atan2(r31, r33);
            euler.z = org.joml.Math.atan2(r12, r22);
        } else {
            if (org.joml.Math.abs(r31) < EPSILON && org.joml.Math.abs(r33) < EPSILON &&
                    org.joml.Math.abs(r12) < EPSILON && org.joml.Math.abs(r22) < EPSILON) {
                euler.x = (float) (r32 > 0 ? -org.joml.Math.PI / 2 : org.joml.Math.PI / 2);
                // when x ≈ -90°
                // r21 ≈ -(sin(y)cos(z) + cos(y)sin(z))
                // r11 ≈ cos(y)cos(z) - sin(y)sin(z)
                // when x ≈ 90°
                // r21 ≈ sin(y)cos(z) - cos(y)sin(z)
                // r11 ≈ cos(y)cos(z) + sin(y)sin(z)
                float r21 = matrix.m10();
                float r11 = matrix.m00();
                euler.z = (float) java.lang.Math.atan2(-r21, r11);
                euler.y = 0;
            } else {
                euler.x = (float) java.lang.Math.asin(-r32);
                euler.y = (float) java.lang.Math.atan2(r31, r33);
                euler.z = (float) java.lang.Math.atan2(r12, r22);
            }
        }

        return  euler;
    }

    public static Vector3fc asEulerAngle(Quaternionf quaternion) {
        Matrix3f m = new Matrix3f().set(quaternion);
        return extractEulerAnglesYXZ(m);
    }

    /**
     * 当主手拿着枪械物品的时候，取消应用在它上面的 viewBobbing，以便应用自定义的跑步/走路动画。
     */
    public static boolean shouldCancelItemInHandViewBobbing() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return false;
        }
        var instance = FirstPersonRenderHandler.getActiveAnimationInstance();

        if (instance != null) {
            return GeoItemRendererRegistry.getAbstractGeoRenderer(instance.currentItem())
                    .map(AbstractGeoItemRenderer::blockViewBobbing)
                    .orElse(false);
        }

        return false;
    }

    public static void applyLevelCameraAnimation(float[] cameraAngles, float partialTick) {
        if (!MinecraftClient.getInstance().options.getBobView().getValue()) {
            return;
        }
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return;
        }
        var instance = FirstPersonRenderHandler.getActiveAnimationInstance();

        GeoItemRendererRegistry.getAbstractGeoRenderer(instance != null ? instance.currentItem() : net.minecraft.item.ItemStack.EMPTY)
                .ifPresent(renderer -> renderer.applyLevelCameraAnimation(cameraAngles, instance.currentItem(), instance.getCameraRotation(), partialTick));
    }

    public static void onBeforeRenderHands(MatrixStack poseStack, float partialTick) {
        if (!MinecraftClient.getInstance().options.getBobView().getValue()) {
            return;
        }
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return;
        }
        var instance = FirstPersonRenderHandler.getActiveAnimationInstance();

        if (instance != null) {
            GeoItemRendererRegistry.getAbstractGeoRenderer(instance.currentItem())
                    .ifPresent(renderer -> renderer.applyItemInHandCameraAnimation(poseStack, instance.currentItem(), instance.getCameraRotation(), partialTick));
        }
    }
}

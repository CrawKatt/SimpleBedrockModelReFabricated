package com.github.mcmodderanchor.simplebedrockmodel.v1.client.bedrock.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.renderer.LightTexture;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class BedrockBone {
    private static final Vector3f[] NORMALS = new Vector3f[6];
    private static final int MAX_LIGHT_TEXTURE = LightTexture.pack(15, 15);

    public final ObjectList<BedrockCube> cubes = new ObjectArrayList<>();
    private final ObjectList<BedrockBone> children = new ObjectArrayList<>();
    public BedrockBone parent;
    public int index = -1;
    public float x;
    public float y;
    public float z;
    public Quaternionf rotation = new Quaternionf();
    public float xScale = 1;
    public float yScale = 1;
    public float zScale = 1;
    public boolean visible = true;
    public boolean illuminated = false;
    public boolean mirror;

    static {
        for (int i = 0; i < NORMALS.length; i++) {
            NORMALS[i] = new Vector3f();
        }
    }

    public void render(PoseStack poseStack, VertexConsumer consumer, int lightmap, int overlay) {
        this.render(poseStack, consumer, lightmap, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    public void render(PoseStack poseStack, VertexConsumer consumer, int lightmap, int overlay, float red, float green, float blue, float alpha) {
        int cubePackedLight = illuminated ? MAX_LIGHT_TEXTURE : lightmap;
        if (this.visible) {
            // 缩放过小时，直接退出渲染
            boolean xNearZero = -1E-5F < xScale && xScale < 1E-5F;
            boolean yNearZero = -1E-5F < yScale && yScale < 1E-5F;
            boolean zNearZero = -1E-5F < zScale && zScale < 1E-5F;
            if ((xNearZero && yNearZero) || (xNearZero && zNearZero) || (yNearZero && zNearZero)) {
                return;
            }

            if (!this.cubes.isEmpty() || !this.children.isEmpty()) {
                poseStack.pushPose();
                this.translateAndRotateAndScale(poseStack);
                this.compile(poseStack.last(), consumer, cubePackedLight, overlay, red, green, blue, alpha);

                for (BedrockBone part : this.children) {
                    part.render(poseStack, consumer, cubePackedLight, overlay, red, green, blue, alpha);
                }

                poseStack.popPose();
            }
        }
    }

    public void translateAndRotateAndScale(PoseStack poseStack) {
        poseStack.translate(this.x / 16.0F, this.y / 16.0F, this.z / 16.0F);
        poseStack.last().pose().rotate(rotation);
        poseStack.last().normal().rotate(rotation);
        if (this.xScale != 0.0F || this.yScale != 0.0F || this.zScale != 0.0F) {
            poseStack.last().pose().scale(this.xScale, this.yScale, this.zScale);
            poseStack.last().normal().scale(this.xScale, this.yScale, this.zScale);
        }
    }

    private void compile(PoseStack.Pose pose, VertexConsumer consumer, int lightmap, int overlay, float red, float green, float blue, float alpha) {
        Matrix3f normal = pose.normal();
        NORMALS[0].set(-normal.m10, -normal.m11, -normal.m12);
        NORMALS[1].set(normal.m10, normal.m11, normal.m12);
        NORMALS[2].set(-normal.m20, -normal.m21, -normal.m22);
        NORMALS[3].set(normal.m20, normal.m21, normal.m22);
        NORMALS[4].set(-normal.m00, -normal.m01, -normal.m02);
        NORMALS[5].set(normal.m00, normal.m01, normal.m02);
        for (BedrockCube bedrockCube : this.cubes) {
            bedrockCube.compile(pose, NORMALS, consumer, lightmap, overlay, red, green, blue, alpha);
        }
    }

    public BedrockCube getRandomCube(Random random) {
        return this.cubes.get(random.nextInt(this.cubes.size()));
    }

    public boolean isEmpty() {
        return this.cubes.isEmpty();
    }

    public void addChild(BedrockBone model) {
        this.children.add(model);
    }

    public ObjectList<BedrockBone> getChildren() {
        return children;
    }
}

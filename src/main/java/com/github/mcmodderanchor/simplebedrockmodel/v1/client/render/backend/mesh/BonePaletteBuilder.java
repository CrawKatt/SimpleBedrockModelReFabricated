package com.github.mcmodderanchor.simplebedrockmodel.v1.client.render.backend.mesh;

import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockBone;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockModel;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.ArrayList;

/**
 * 骨骼矩阵调色板构建器。
 * <p>
 * 每帧根据模型当前的骨骼状态，线性计算所有动态骨骼的全局变换矩阵。
 * 使用预先构建的拓扑顺序（父骨骼 index 总是小于子骨骼 index，
 * 或者父骨骼是 root 即 index == -1），避免递归。
 */
public final class BonePaletteBuilder {

    private BonePaletteBuilder() {}

    /**
     * 构建骨骼调色板。
     * <p>
     * 输出数组中，每个骨骼占两个矩阵：
     * <ul>
     *   <li>{@code globalTransforms[i]} - 4x4 全局变换矩阵（含 translate/rotate/scale）</li>
     *   <li>{@code normalTransforms[i]} - 3x3 法线变换矩阵</li>
     * </ul>
     * 同时输出每根骨骼的可见性标记（含父级继承）。
     *
     * @param model            当前模型（骨骼状态已由动画系统 apply）
     * @param mesh             扁平化网格（提供 parentIndices）
     * @param globalTransforms 输出：全局变换矩阵数组，长度 >= boneCount
     * @param normalTransforms 输出：法线变换矩阵数组，长度 >= boneCount
     * @param visibility       输出：可见性数组，长度 >= boneCount
     */
    public static void buildPalette(BedrockModel model, FlattenedModelMesh mesh,
                                    Matrix4f[] globalTransforms, Matrix3f[] normalTransforms,
                                    boolean[] visibility) {
        ArrayList<BedrockBone> boneIndex = model.getBoneIndexes();
        int[] parentIndices = mesh.parentIndices();
        int boneCount = mesh.boneCount();

        for (int i = 0; i < boneCount; i++) {
            BedrockBone bone = boneIndex.get(i);

            // 构建本地变换
            Matrix4f local = globalTransforms[i];
            local.identity();
            local.translate(bone.x / 16.0f, bone.y / 16.0f, bone.z / 16.0f);
            local.rotate(bone.rotation);
            local.scale(bone.xScale, bone.yScale, bone.zScale);

            // 乘以父骨骼的全局变换
            int parentIdx = parentIndices[i];
            if (parentIdx >= 0) {
                // parent global * local = global
                Matrix4f parentGlobal = globalTransforms[parentIdx];
                local.set(new Matrix4f(parentGlobal).mul(local));
            }

            // 构建法线变换（3x3，从 4x4 提取旋转+缩放部分）
            Matrix3f normal = normalTransforms[i];
            normal.set(local);

            // 可见性：自身可见 && 父级可见
            boolean parentVisible = (parentIdx >= 0) ? visibility[parentIdx] : true;
            visibility[i] = bone.visible && parentVisible;
        }
    }

    /**
     * 分配骨骼调色板所需的数组。
     */
    public static Matrix4f[] allocateTransforms(int boneCount) {
        Matrix4f[] arr = new Matrix4f[boneCount];
        for (int i = 0; i < boneCount; i++) {
            arr[i] = new Matrix4f();
        }
        return arr;
    }

    public static Matrix3f[] allocateNormals(int boneCount) {
        Matrix3f[] arr = new Matrix3f[boneCount];
        for (int i = 0; i < boneCount; i++) {
            arr[i] = new Matrix3f();
        }
        return arr;
    }
}

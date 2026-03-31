package com.github.mcmodderanchor.simplebedrockmodel.v1.client.render.backend.mesh;

import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockBone;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockCube;
import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockModel;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

/**
 * 将 {@link BedrockModel} 的骨骼树展平为 {@link FlattenedModelMesh}。
 * <p>
 * 核心规则：
 * <ul>
 *   <li>遇到 {@code bone.index >= 0} 的骨骼：切换当前动态骨骼，重置 helper 累积矩阵</li>
 *   <li>遇到 {@code bone.index == -1} 的 helper bone：把它的局部变换乘进 helper 累积矩阵</li>
 *   <li>收集 cube 顶点时：顶点归属当前动态骨骼 index，坐标/法线先乘 helper 累积矩阵</li>
 * </ul>
 */
public final class FlattenedMeshBuilder {

    // 用于顶点计算的临时向量，避免每次分配
    private static final Vector3f TEMP_POS = new Vector3f();
    private static final Vector3f TEMP_NORMAL = new Vector3f();
    private static final Vector4f TEMP_POS4 = new Vector4f();

    private FlattenedMeshBuilder() {}

    /**
     * 将模型展平为 FlattenedModelMesh。
     *
     * @param model 已完成初始化的 BedrockModel
     * @return 扁平化后的网格数据
     */
    public static FlattenedModelMesh build(BedrockModel model) {
        List<FlattenedVertex> vertices = new ArrayList<>();
        ArrayList<BedrockBone> boneIndex = model.getBoneIndexes();
        int boneCount = boneIndex.size();

        // 构建父索引数组
        int[] parentIndices = new int[boneCount];
        for (int i = 0; i < boneCount; i++) {
            BedrockBone bone = boneIndex.get(i);
            parentIndices[i] = (bone.parent != null) ? bone.parent.index : -1;
        }

        // 从 root 开始遍历
        // root 本身 index == -1，它的子节点才是真正的动态骨骼或 helper
        traverseBone(model.getRoot(), -1, new Matrix4f().identity(), new Matrix3f().identity(),
                false, vertices);

        return new FlattenedModelMesh(vertices, boneCount, parentIndices);
    }

    /**
     * 递归遍历骨骼树，收集所有 cube 的顶点。
     *
     * @param bone               当前骨骼节点
     * @param currentDynamicBone 当前绑定的动态骨骼 index
     * @param helperPose         从最近的动态骨骼到当前节点的 helper 累积变换矩阵
     * @param helperNormal       对应的法线变换矩阵
     * @param illuminated        当前继承链上的 illuminated 状态
     * @param outVertices        输出顶点列表
     */
    private static void traverseBone(BedrockBone bone, int currentDynamicBone,
                                     Matrix4f helperPose, Matrix3f helperNormal,
                                     boolean illuminated, List<FlattenedVertex> outVertices) {
        int activeBone = currentDynamicBone;
        Matrix4f activePose = helperPose;
        Matrix3f activeNormal = helperNormal;
        boolean activeIlluminated = illuminated || bone.illuminated;

        if (bone.index >= 0) {
            // 这是一个动态骨骼，切换绑定目标，重置 helper 累积矩阵
            activeBone = bone.index;
            activePose = new Matrix4f().identity();
            activeNormal = new Matrix3f().identity();
        } else if (currentDynamicBone >= 0) {
            // 这是一个 helper bone，把它的局部变换乘进 helper 累积矩阵
            Matrix4f localTransform = new Matrix4f().identity();
            localTransform.translate(bone.x / 16.0f, bone.y / 16.0f, bone.z / 16.0f);
            localTransform.rotate(bone.rotation);
            localTransform.scale(bone.xScale, bone.yScale, bone.zScale);

            activePose = new Matrix4f(helperPose).mul(localTransform);

            Matrix3f localNormal = new Matrix3f().identity();
            localNormal.rotate(bone.rotation);
            localNormal.scale(bone.xScale, bone.yScale, bone.zScale);
            activeNormal = new Matrix3f(helperNormal).mul(localNormal);
        }

        // 收集该骨骼上的 cube 顶点
        if (activeBone >= 0) {
            collectCubeVertices(bone, activeBone, activePose, activeNormal, activeIlluminated, outVertices);
        }

        // 递归子节点
        for (BedrockBone child : bone.getChildren()) {
            traverseBone(child, activeBone, activePose, activeNormal, activeIlluminated, outVertices);
        }
    }

    /**
     * 收集一个骨骼上所有 cube 的顶点。
     */
    private static void collectCubeVertices(BedrockBone bone, int boneIndex,
                                            Matrix4f helperPose, Matrix3f helperNormal,
                                            boolean fullBright, List<FlattenedVertex> outVertices) {
        for (BedrockCube cube : bone.cubes) {
            // 计算 cube 的 8 个顶点位置（本地空间）
            float x = cube.x();
            float y = cube.y();
            float z = cube.z();
            float w = cube.width();
            float h = cube.height();
            float d = cube.depth();

            // 8 个角点
            float[][] corners = {
                    {x,     y,     z    }, // 0: X1_Y1_Z1
                    {x + w, y,     z    }, // 1: X2_Y1_Z1
                    {x + w, y + h, z    }, // 2: X2_Y2_Z1
                    {x,     y + h, z    }, // 3: X1_Y2_Z1
                    {x,     y,     z + d}, // 4: X1_Y1_Z2
                    {x + w, y,     z + d}, // 5: X2_Y1_Z2
                    {x + w, y + h, z + d}, // 6: X2_Y2_Z2
                    {x,     y + h, z + d}, // 7: X1_Y2_Z2
            };

            // 6 个面的法线（本地空间，未变换）
            float[][] faceNormals = {
                    { 0, -1,  0}, // face 0: bottom (-Y)
                    { 0,  1,  0}, // face 1: top (+Y)
                    { 0,  0, -1}, // face 2: north (-Z)
                    { 0,  0,  1}, // face 3: south (+Z)
                    {-1,  0,  0}, // face 4: west (-X)
                    { 1,  0,  0}, // face 5: east (+X)
            };

            for (int face = 0; face < BedrockCube.NUM_CUBE_FACES; face++) {
                if (cube.isEmptyFace(face)) {
                    continue;
                }

                int[] vertOrder = BedrockCube.VERTEX_ORDER[face];
                float[] normal = faceNormals[face];

                for (int vi = 0; vi < 4; vi++) {
                    float[] corner = corners[vertOrder[vi]];
                    float u = cube.getU(face, vi);
                    float v = cube.getV(face, vi);

                    // 应用 helper 累积变换
                    float px, py, pz;
                    float nx, ny, nz;

                    if (helperPose.equals(IDENTITY_MATRIX)) {
                        px = corner[0];
                        py = corner[1];
                        pz = corner[2];
                        nx = normal[0];
                        ny = normal[1];
                        nz = normal[2];
                    } else {
                        TEMP_POS.set(corner[0], corner[1], corner[2]);
                        TEMP_POS.mulPosition(helperPose);
                        px = TEMP_POS.x;
                        py = TEMP_POS.y;
                        pz = TEMP_POS.z;

                        TEMP_NORMAL.set(normal[0], normal[1], normal[2]);
                        TEMP_NORMAL.mul(helperNormal);
                        nx = TEMP_NORMAL.x;
                        ny = TEMP_NORMAL.y;
                        nz = TEMP_NORMAL.z;
                    }

                    outVertices.add(new FlattenedVertex(px, py, pz, nx, ny, nz, u, v, boneIndex, fullBright));
                }
            }
        }
    }

    // 用于快速判断是否为单位矩阵，避免不必要的矩阵乘法
    private static final Matrix4f IDENTITY_MATRIX = new Matrix4f().identity();

    /**
     * 获取模型的 root bone。
     * 由于 BedrockModel.root 是 protected，这里通过反射或公开方法获取。
     * 我们在 BedrockModel 中需要暴露 root 的访问方式。
     */
    // 注意：需要在 BedrockModel 中添加 getRoot() 方法
}

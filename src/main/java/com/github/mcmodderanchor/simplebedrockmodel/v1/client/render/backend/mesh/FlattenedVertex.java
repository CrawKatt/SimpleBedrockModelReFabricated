package com.github.mcmodderanchor.simplebedrockmodel.v1.client.render.backend.mesh;

/**
 * 扁平化后的单个顶点数据。
 * <p>
 * 保存了 bind pose 下的本地空间顶点信息，以及该顶点所属的动态骨骼索引。
 * helper bone（index == -1）的局部变换已经在构建时烘焙进了顶点坐标和法线。
 */
public final class FlattenedVertex {
    /** 本地空间位置 (相对于所属动态骨骼) */
    public final float posX, posY, posZ;
    /** 本地空间法线 (相对于所属动态骨骼) */
    public final float normalX, normalY, normalZ;
    /** 纹理坐标 */
    public final float u, v;
    /** 所属的动态骨骼索引 (对应 BedrockModel.boneIndex 中的位置) */
    public final int boneIndex;
    /** 是否全亮 (illuminated 继承链的烘焙结果) */
    public final boolean fullBright;

    public FlattenedVertex(float posX, float posY, float posZ,
                           float normalX, float normalY, float normalZ,
                           float u, float v,
                           int boneIndex, boolean fullBright) {
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.normalX = normalX;
        this.normalY = normalY;
        this.normalZ = normalZ;
        this.u = u;
        this.v = v;
        this.boneIndex = boneIndex;
        this.fullBright = fullBright;
    }
}

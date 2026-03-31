package com.github.mcmodderanchor.simplebedrockmodel.v1.client.render.backend.mesh;

import java.util.List;

/**
 * 扁平化后的模型网格数据。
 * <p>
 * 将整个 {@link com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockModel}
 * 的骨骼树展平为一个线性顶点列表，每个顶点记录了所属的动态骨骼索引。
 * <p>
 * 该数据在资源重载时构建一次，后续作为 GPU source SSBO 的数据来源。
 */
public final class FlattenedModelMesh {
    private final FlattenedVertex[] vertices;
    /** 动态骨骼总数 (即 BedrockModel.boneIndex.size()) */
    private final int boneCount;
    /** 每根动态骨骼的父骨骼索引，-1 表示根。按拓扑顺序排列。 */
    private final int[] parentIndices;

    public FlattenedModelMesh(List<FlattenedVertex> vertexList, int boneCount, int[] parentIndices) {
        this.vertices = vertexList.toArray(new FlattenedVertex[0]);
        this.boneCount = boneCount;
        this.parentIndices = parentIndices;
    }

    public FlattenedVertex[] vertices() { return vertices; }
    public int vertexCount() { return vertices.length; }
    public int boneCount() { return boneCount; }
    public int[] parentIndices() { return parentIndices; }

    /**
     * @return quad 数量 (vertexCount / 4)
     */
    public int quadCount() { return vertices.length / 4; }

    public boolean isEmpty() { return vertices.length == 0; }
}

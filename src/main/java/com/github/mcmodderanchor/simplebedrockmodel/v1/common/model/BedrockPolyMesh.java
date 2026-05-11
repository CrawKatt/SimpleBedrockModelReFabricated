package com.github.mcmodderanchor.simplebedrockmodel.v1.common.model;

import com.github.mcmodderanchor.simplebedrockmodel.v1.common.resource.pojo.PolyMeshItem;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class BedrockPolyMesh implements BedrockCube {
    private static final float INV_BLOCK = 1.0f / 16.0f;

    private final Quad[] quads;
    private final float x;
    private final float y;
    private final float z;
    private final float width;
    private final float height;
    private final float depth;

    public BedrockPolyMesh(PolyMeshItem polyMesh, BedrockBone part, float texWidth, float texHeight) {
        List<Quad> bakedQuads = new ArrayList<>();
        float[][] positions = polyMesh.getPositions();
        float[][] normals = polyMesh.getNormals();
        float[][] uvs = polyMesh.getUvs();
        JsonElement polys = polyMesh.getPolys();

        if (positions != null && polys != null && !polys.isJsonNull()) {
            bakePolys(polyMesh, part, texWidth, texHeight, positions, normals, uvs, polys, bakedQuads);
        }

        this.quads = bakedQuads.toArray(Quad[]::new);

        if (this.quads.length == 0) {
            this.x = 0;
            this.y = 0;
            this.z = 0;
            this.width = 0;
            this.height = 0;
            this.depth = 0;
        } else {
            float minX = Float.POSITIVE_INFINITY;
            float minY = Float.POSITIVE_INFINITY;
            float minZ = Float.POSITIVE_INFINITY;
            float maxX = Float.NEGATIVE_INFINITY;
            float maxY = Float.NEGATIVE_INFINITY;
            float maxZ = Float.NEGATIVE_INFINITY;
            for (Quad quad : this.quads) {
                for (Vertex vertex : quad.vertices) {
                    minX = Math.min(minX, vertex.x);
                    minY = Math.min(minY, vertex.y);
                    minZ = Math.min(minZ, vertex.z);
                    maxX = Math.max(maxX, vertex.x);
                    maxY = Math.max(maxY, vertex.y);
                    maxZ = Math.max(maxZ, vertex.z);
                }
            }
            this.x = minX;
            this.y = minY;
            this.z = minZ;
            this.width = maxX - minX;
            this.height = maxY - minY;
            this.depth = maxZ - minZ;
        }
    }

    private void bakePolys(PolyMeshItem polyMesh, BedrockBone part, float texWidth, float texHeight, float[][] positions, float[][] normals,
                           float[][] uvs, JsonElement polys, List<Quad> bakedQuads) {
        if (polys.isJsonPrimitive()) {
            JsonPrimitive primitive = polys.getAsJsonPrimitive();
            if (primitive.isString()) {
                String mode = primitive.getAsString();
                if ("tri_list".equals(mode)) {
                    bakeListMode(polyMesh, part, texWidth, texHeight, positions, normals, uvs, bakedQuads, 3);
                } else if ("quad_list".equals(mode)) {
                    bakeListMode(polyMesh, part, texWidth, texHeight, positions, normals, uvs, bakedQuads, 4);
                }
            }
        } else if (polys.isJsonArray()) {
            for (JsonElement polyElement : polys.getAsJsonArray()) {
                if (polyElement != null && polyElement.isJsonArray()) {
                    bakeIndexedPolygon(polyMesh, part, texWidth, texHeight, positions, normals, uvs, polyElement.getAsJsonArray(), bakedQuads);
                }
            }
        }
    }

    private void bakeListMode(PolyMeshItem polyMesh, BedrockBone part, float texWidth, float texHeight, float[][] positions, float[][] normals,
                              float[][] uvs, List<Quad> bakedQuads, int stride) {
        for (int i = 0; i + stride - 1 < positions.length; i += stride) {
            List<Vertex> polygon = new ArrayList<>(stride);
            for (int j = 0; j < stride; j++) {
                polygon.add(createVertex(polyMesh, part, texWidth, texHeight, positions, normals, uvs, i + j, i + j, i + j));
            }
            bakePolygon(polygon, bakedQuads);
        }
    }

    private void bakeIndexedPolygon(PolyMeshItem polyMesh, BedrockBone part, float texWidth, float texHeight, float[][] positions, float[][] normals,
                                    float[][] uvs, JsonArray poly, List<Quad> bakedQuads) {
        List<Vertex> polygon = new ArrayList<>(poly.size());
        for (JsonElement vertexElement : poly) {
            if (vertexElement == null || !vertexElement.isJsonArray()) {
                continue;
            }
            JsonArray indices = vertexElement.getAsJsonArray();
            if (indices.isEmpty()) {
                continue;
            }
            int positionIndex = getIndex(indices, 0);
            int normalIndex = indices.size() > 1 ? getIndex(indices, 1) : positionIndex;
            int uvIndex = indices.size() > 2 ? getIndex(indices, 2) : positionIndex;
            polygon.add(createVertex(polyMesh, part, texWidth, texHeight, positions, normals, uvs, positionIndex, normalIndex, uvIndex));
        }
        if (polygon.size() > 1 && samePosition(polygon.get(0), polygon.get(polygon.size() - 1))) {
            polygon.remove(polygon.size() - 1);
        }
        bakePolygon(polygon, bakedQuads);
    }

    private int getIndex(JsonArray indices, int index) {
        return indices.get(index).getAsInt();
    }

    private Vertex createVertex(PolyMeshItem polyMesh, BedrockBone part, float texWidth, float texHeight, float[][] positions, float[][] normals,
                                float[][] uvs, int positionIndex, int normalIndex, int uvIndex) {
        float[] position = getArray(positions, positionIndex);
        float x = (-get(position, 0) - part.x) * INV_BLOCK;
        float y = (get(position, 1) - part.y) * INV_BLOCK;
        float z = (get(position, 2) - part.z) * INV_BLOCK;

        float nx = 0;
        float ny = 0;
        float nz = 0;
        if (normals != null && normalIndex >= 0 && normalIndex < normals.length) {
            float[] normal = normals[normalIndex];
            nx = -get(normal, 0);
            ny = get(normal, 1);
            nz = get(normal, 2);
            float length = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (length > 1.0E-6f) {
                nx /= length;
                ny /= length;
                nz /= length;
            }
        }

        float u = 0;
        float v = 0;
        if (uvs != null && uvIndex >= 0 && uvIndex < uvs.length) {
            float[] uv = uvs[uvIndex];
            u = get(uv, 0);
            v = get(uv, 1);
            if (!polyMesh.isNormalizedUvs()) {
                u = texWidth == 0 ? 0 : u / texWidth;
                v = texHeight == 0 ? 0 : v / texHeight;
            }
            v = 1.0f - v;
        }
        return new Vertex(x, y, z, u, v, nx, ny, nz);
    }

    private float[] getArray(float[][] values, int index) {
        if (index < 0 || index >= values.length || values[index] == null) {
            return new float[0];
        }
        return values[index];
    }

    private float get(float[] values, int index) {
        return values != null && index >= 0 && index < values.length ? values[index] : 0;
    }

    private boolean samePosition(Vertex first, Vertex second) {
        return Math.abs(first.x - second.x) < 1.0E-6f
                && Math.abs(first.y - second.y) < 1.0E-6f
                && Math.abs(first.z - second.z) < 1.0E-6f;
    }

    private void bakePolygon(List<Vertex> polygon, List<Quad> bakedQuads) {
        if (polygon.size() < 3) {
            return;
        }
        if (polygon.size() == 4) {
            bakeQuad(polygon.get(0), polygon.get(1), polygon.get(2), polygon.get(3), bakedQuads);
            return;
        }
        for (int i = 1; i + 1 < polygon.size(); i++) {
            bakeTriangle(polygon.get(0), polygon.get(i), polygon.get(i + 1), bakedQuads);
        }
    }

    private void bakeTriangle(Vertex a, Vertex b, Vertex c, List<Quad> bakedQuads) {
        Vector3f normal = computeNormal(a, b, c);
        bakedQuads.add(new Quad(a.withNormal(normal), b.withNormal(normal), c.withNormal(normal), c.withNormal(normal)));
    }

    private void bakeQuad(Vertex a, Vertex b, Vertex c, Vertex d, List<Quad> bakedQuads) {
        Vector3f normal = computeQuadNormal(a, b, c, d);
        bakedQuads.add(new Quad(a.withNormal(normal), b.withNormal(normal), c.withNormal(normal), d.withNormal(normal)));
    }

    private Vector3f computeQuadNormal(Vertex a, Vertex b, Vertex c, Vertex d) {
        Vector3f first = computeNormal(a, b, c);
        Vector3f second = computeNormal(a, c, d);
        Vector3f normal = first.add(second, new Vector3f());
        if (normal.lengthSquared() < 1.0E-12f) {
            return first;
        }
        return normal.normalize();
    }

    private Vector3f computeNormal(Vertex a, Vertex b, Vertex c) {
        Vector3f edge1 = new Vector3f(b.x - a.x, b.y - a.y, b.z - a.z);
        Vector3f edge2 = new Vector3f(c.x - a.x, c.y - a.y, c.z - a.z);
        Vector3f normal = edge1.cross(edge2, new Vector3f());
        if (normal.lengthSquared() < 1.0E-12f) {
            normal.set(0, 1, 0);
        } else {
            normal.normalize();
        }
        return normal;
    }

    @Override
    public void compile(PoseStack.Pose pose, Vector3f[] normals, VertexConsumer consumer, int lightmap, int overlay, float red, float green, float blue, float alpha) {
        Matrix4f positionMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        for (Quad quad : quads) {
            for (Vertex vertex : quad.vertices) {
                emit(consumer, transform(vertex, positionMatrix, normalMatrix), red, green, blue, alpha, overlay, lightmap);
            }
        }
    }

    private TransformedVertex transform(Vertex vertex, Matrix4f positionMatrix, Matrix3f normalMatrix) {
        Vector3f position = new Vector3f(vertex.x, vertex.y, vertex.z).mulPosition(positionMatrix);
        Vector3f normal = new Vector3f(vertex.nx, vertex.ny, vertex.nz).mul(normalMatrix);
        if (normal.lengthSquared() > 1.0E-12f) {
            normal.normalize();
        }
        return new TransformedVertex(position.x, position.y, position.z, vertex.u, vertex.v, normal.x, normal.y, normal.z);
    }

    private void emit(VertexConsumer consumer, TransformedVertex vertex, float red, float green, float blue, float alpha, int overlay, int lightmap) {
        consumer.vertex(vertex.x, vertex.y, vertex.z, red, green, blue, alpha, vertex.u, vertex.v, overlay, lightmap, vertex.nx, vertex.ny, vertex.nz);
    }

    @Override
    public float width() {
        return width;
    }

    @Override
    public float height() {
        return height;
    }

    @Override
    public float depth() {
        return depth;
    }

    @Override
    public float x() {
        return x;
    }

    @Override
    public float y() {
        return y;
    }

    @Override
    public float z() {
        return z;
    }

    @Override
    public boolean isEmptyFace(int face) {
        return true;
    }

    private record Vertex(float x, float y, float z, float u, float v, float nx, float ny, float nz) {
        private Vertex withNormal(Vector3f normal) {
            return new Vertex(x, y, z, u, v, normal.x, normal.y, normal.z);
        }

    }

    private static class Quad {
        private final Vertex[] vertices;

        private Quad(Vertex a, Vertex b, Vertex c, Vertex d) {
            this.vertices = new Vertex[]{a, b, c, d};
        }
    }

    private record TransformedVertex(float x, float y, float z, float u, float v, float nx, float ny, float nz) {
    }
}

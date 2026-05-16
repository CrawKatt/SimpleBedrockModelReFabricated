package com.github.mcmodderanchor.simplebedrockmodel.v1.client.model;

import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;

public interface PositionableModel {
    void applyTransform(MatrixStack poseStack, ModelTransformationMode ctx);
}

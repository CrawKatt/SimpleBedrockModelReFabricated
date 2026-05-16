package example.client.render.blockentity;

import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockModel;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.NotNull;

public abstract class BedrockModelBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    protected abstract BedrockModel getModel();

    protected abstract SpriteIdentifier getMaterial();

    protected abstract RenderLayer getRenderType(Identifier textureLocation);

    @Override
    public void render(@NotNull T blockEntity, float partialTick, @NotNull MatrixStack matrixStack,
                       @NotNull VertexConsumerProvider bufferSource, int packedLight, int packedOverlay) {
        VertexConsumer buffer = getMaterial().getVertexConsumer(bufferSource, this::getRenderType);
        BlockState blockState = blockEntity.getCachedState();

        matrixStack.push();
        matrixStack.translate(0.5, 0, 0.5);
        if (blockState.contains(Properties.HORIZONTAL_FACING)) {
            Direction facing = blockState.get(Properties.HORIZONTAL_FACING);
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-facing.asRotation()));
        }
        getModel().renderToBuffer(matrixStack, buffer, packedLight, packedOverlay);
        matrixStack.pop();
    }
}

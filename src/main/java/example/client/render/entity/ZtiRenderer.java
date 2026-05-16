package example.client.render.entity;

import com.github.mcmodderanchor.simplebedrockmodel.v1.client.model.EntityModel;
import com.github.mcmodderanchor.simplebedrockmodel.v1.resource.BedrockModelResourceSet;
import com.google.common.base.Suppliers;
import com.maydaymemory.mae.basic.ArrayPoseBuilder;
import com.maydaymemory.mae.basic.Pose;
import com.maydaymemory.mae.basic.ZYXBoneTransformFactory;
import com.maydaymemory.mae.blend.EulerAdditiveBlender;
import com.maydaymemory.mae.blend.SimpleEulerAdditiveBlender;
import example.entity.Zti;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ZtiRenderer extends EntityRenderer<Zti> {
    public static final Identifier TEXTURE =  Identifier.of("example", "textures/entity/zti.png");
    public static final Identifier MODEL =  Identifier.of("example", "zti.geo");
    public static final Identifier ANIMATION =  Identifier.of("example", "zti.animation");

    private static final EulerAdditiveBlender BLENDER = new SimpleEulerAdditiveBlender(new ZYXBoneTransformFactory(), ArrayPoseBuilder::new);

    private final Supplier<EntityModel> modelSupplier;

    public ZtiRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 1.0F;
        this.modelSupplier = Suppliers.memoize(() -> (EntityModel) BedrockModelResourceSet.getInstance().getModel(MODEL));
    }

    @Override
    public void render(@NotNull Zti entity, float entityYaw, float partialTick, @NotNull MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, int packedLight) {
        EntityModel model = modelSupplier.get();
        if (model != null) {
            entity.getAnimationInstance().renderTick();
            Pose blendedPose = BLENDER.blend(model.getBindPose(), entity.getAnimationInstance().getStateMachine().getPose());
            model.applyPose(blendedPose);

            matrixStack.push();

            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - MathHelper.lerpAngleDegrees(partialTick, entity.prevBodyYaw, entity.bodyYaw)));

            VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(TEXTURE));
            model.renderToBuffer(matrixStack, consumer, packedLight,
                    OverlayTexture.getUv(0.0F, entity.hurtTime > 0 || entity.deathTime > 0)
            );
            model.applyPose(model.getBindPose());
            matrixStack.pop();
        }
        super.render(entity, entityYaw, partialTick, matrixStack, vertexConsumers, packedLight);
    }

    @Override
    public @NotNull Identifier getTexture(@NotNull Zti entity) {
        return TEXTURE;
    }
}

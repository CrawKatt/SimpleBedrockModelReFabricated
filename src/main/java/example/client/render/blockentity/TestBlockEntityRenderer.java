package example.client.render.blockentity;

import com.github.mcmodderanchor.simplebedrockmodel.v1.common.model.BedrockModel;
import com.github.mcmodderanchor.simplebedrockmodel.v1.resource.BedrockModelResourceSet;
import com.google.common.base.Suppliers;
import com.maydaymemory.mae.basic.ArrayPoseBuilder;
import com.maydaymemory.mae.basic.Pose;
import com.maydaymemory.mae.basic.ZYXBoneTransformFactory;
import com.maydaymemory.mae.blend.EulerAdditiveBlender;
import com.maydaymemory.mae.blend.SimpleEulerAdditiveBlender;
import example.animation.TestBlockAnimationInstance;
import example.block.blockentity.TestBlockEntity;
import example.init.ExampleModRegister;
import example.resource.KnownResources;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class TestBlockEntityRenderer extends BedrockModelBlockEntityRenderer<TestBlockEntity> {
    private static final SpriteIdentifier MATERIAL = new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, ExampleModRegister.modLoc("block/test"));
    private static final EulerAdditiveBlender BLENDER = new SimpleEulerAdditiveBlender(new ZYXBoneTransformFactory(), ArrayPoseBuilder::new);

    private final Supplier<BedrockModel> modelSupplier;

    public TestBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        modelSupplier = Suppliers.memoize(() -> BedrockModelResourceSet.getInstance().getModel(KnownResources.TEST));
    }

    @Override
    protected BedrockModel getModel() {
        return modelSupplier.get();
    }

    @Override
    protected SpriteIdentifier getMaterial() {
        return MATERIAL;
    }

    @Override
    protected RenderLayer getRenderType(Identifier textureLocation) {
        return RenderLayer.getEntityCutout(textureLocation);
    }

    @Override
    public void render(@NotNull TestBlockEntity blockEntity, float partialTick, @NotNull MatrixStack matrixStack,
                       @NotNull VertexConsumerProvider buffer, int packedLight, int packedOverlay) {
        TestBlockAnimationInstance animationInstance = blockEntity.getAnimationInstance();
        animationInstance.renderTick();
        Pose animationPose = animationInstance.getStateMachine().getPose();
        BedrockModel model = modelSupplier.get();
        Pose bindPose = model.getBindPose();
        Pose blended = BLENDER.blend(bindPose, animationPose);
        model.applyPose(blended);
        super.render(blockEntity, partialTick, matrixStack, buffer, packedLight, packedOverlay);
    }
}

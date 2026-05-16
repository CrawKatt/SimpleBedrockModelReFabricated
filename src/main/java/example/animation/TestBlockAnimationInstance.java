package example.animation;

import com.maydaymemory.mae.basic.ArrayPoseBuilder;
import com.maydaymemory.mae.control.misc.RealtimeVelocityEstimatorNode;
import com.maydaymemory.mae.control.statemachine.AnimationStateMachine;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;

/**
 * Animation Instance 一般统合了某个游戏对象的所有动画需要的上下文，并且统一负责同步动画状态
 */
public class TestBlockAnimationInstance {
    private final AnimationStateMachine<TestBlockAnimationContext> stateMachine;

    @Environment(EnvType.CLIENT)
    private RealtimeVelocityEstimatorNode velocityEstimatorNode;

    public TestBlockAnimationInstance(BlockEntity blockEntity) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            velocityEstimatorNode = new RealtimeVelocityEstimatorNode(ArrayPoseBuilder::new, System::nanoTime);
            stateMachine = new AnimationStateMachine<>(TestBlockStateMachineState.INSTANCE, new TestBlockAnimationContext(velocityEstimatorNode, blockEntity), System::nanoTime);
            velocityEstimatorNode.getPoseSlot().connect(stateMachine.getOutputPort());
        } else {
            stateMachine = new AnimationStateMachine<>(TestBlockStateMachineState.INSTANCE, new TestBlockAnimationContext(null, blockEntity), System::nanoTime);
        }
    }

    @Environment(EnvType.CLIENT)
    public void renderTick() {
        velocityEstimatorNode.tick();
        stateMachine.tick();
    }

    public void tick() {
        stateMachine.tick();
    }

    public void triggerTransition() {
        stateMachine.getContext().needTransition = true;
    }

    public AnimationStateMachine<TestBlockAnimationContext> getStateMachine() {
        return stateMachine;
    }

    public NbtCompound getUpdateTag() {
        NbtCompound tag = new NbtCompound();
        tag.put("StateMachineContext", stateMachine.getContext().getUpdateTag());
        return tag;
    }

    public void handleUpdateTag(NbtCompound tag) {
        stateMachine.getContext().handleUpdateTag(tag.getCompound("StateMachineContext"));
    }
}

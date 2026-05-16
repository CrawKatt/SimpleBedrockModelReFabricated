package example.block.blockentity;

import example.animation.TestBlockAnimationInstance;
import example.init.ExampleModRegister;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TestBlockEntity extends BlockEntity {
    private final TestBlockAnimationInstance animationInstance = new TestBlockAnimationInstance(this);

    public TestBlockEntity(BlockPos pos, BlockState state) {
        super(ExampleModRegister.TEST_BLOCK_ENTITY_TYPE, pos, state);
    }

    public TestBlockAnimationInstance getAnimationInstance() {
        return animationInstance;
    }

    public void tick(World pLevel, BlockPos pPos, BlockState pState) {
        animationInstance.tick();
    }

    public void replicateAnimationInstance() {
        this.markDirty();
        if (world != null) {
            BlockState state = world.getBlockState(pos);
            world.updateListeners(pos, state, state, Block.NOTIFY_ALL);
        }
    }

//    @Override
//    public @NotNull CompoundTag getUpdateTag() {
//        CompoundTag tag = super.getUpdateTag();
//        tag.put("AnimationInstance", animationInstance.getUpdateTag());
//        return tag;
//    }
//
//    @Override
//    public void handleUpdateTag(CompoundTag tag) {
//        super.handleUpdateTag(tag);
//        animationInstance.handleUpdateTag(tag.getCompound("AnimationInstance"));
//    }
//
//    @Override
//    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
//        super.onDataPacket(net, pkt);
//        CompoundTag tag = pkt.getTag();
//        if (tag != null) {
//            animationInstance.handleUpdateTag(tag.getCompound("AnimationInstance"));
//        }
//    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }
}

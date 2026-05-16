package example.block;

import com.mojang.serialization.MapCodec;
import example.block.blockentity.TestBlockEntity;
import example.init.ExampleModRegister;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TestBlock extends HorizontalFacingBlock implements BlockEntityProvider {
    public static final MapCodec<TestBlock> CODEC = createCodec(TestBlock::new);
    public TestBlock(AbstractBlock.Settings properties) {
        super(AbstractBlock.Settings.create()
                .mapColor(MapColor.DIRT_BROWN)
                .strength(2.0F)
                .sounds(BlockSoundGroup.WOOD)
                .luminance(s -> 15)
                .nonOpaque()
                .burnable());
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    public TestBlock() {
        super(AbstractBlock.Settings.create()
                .mapColor(MapColor.DIRT_BROWN)
                .strength(2.0F)
                .sounds(BlockSoundGroup.WOOD)
                .luminance(s -> 15)
                .nonOpaque()
                .burnable());
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    private static final BlockEntityTicker<TestBlockEntity> ticker = (level, pos, state, blockEntity) -> {
        blockEntity.tick(level, pos, state);
    };

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext context) {
        return this.getDefaultState().with(FACING, context.getHorizontalPlayerFacing());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    @Nullable
    public BlockEntity createBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new TestBlockEntity(blockPos, blockState);
    }

//    @Override
//    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
//        if (pLevel.isClientSide) {
//            BlockEntity pBlockEntity = pLevel.getBlockEntity(pPos);
//            if (pBlockEntity instanceof TestBlockEntity blockEntity) {
//                pBlockEntity.setChanged();
//            }
//            return InteractionResult.SUCCESS;
//        } else {
//            BlockEntity pBlockEntity = pLevel.getBlockEntity(pPos);
//            if (pBlockEntity instanceof TestBlockEntity blockEntity) {
//                blockEntity.getAnimationInstance().triggerTransition();
//                blockEntity.replicateAnimationInstance();
//            }
//            return InteractionResult.CONSUME;
//        }
//    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull World pLevel,
                                                                   @NotNull BlockState pState,
                                                                   @NotNull BlockEntityType<T> pBlockEntityType) {
        if (pBlockEntityType == ExampleModRegister.TEST_BLOCK_ENTITY_TYPE) {
            return (BlockEntityTicker<T>) ticker;
        } else {
            return null;
        }
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }
}

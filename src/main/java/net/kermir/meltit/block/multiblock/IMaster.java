package net.kermir.meltit.block.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface IMaster {
    private BlockEntity self() {
        return (BlockEntity)this;
    }

    default BlockState getMasterBlock() {
        return self().getBlockState();
    }

    default BlockPos getMasterPos() {
        return self().getBlockPos();
    }

    void notifyChange(BlockPos pos, BlockState state);
}

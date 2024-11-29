package net.kermir.meltit.block.multiblock.module.entity;

import net.kermir.meltit.block.entity.BlockEntityRegistry;
import net.kermir.meltit.block.multiblock.IMaster;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SmelteryBlockEntity extends ServantEntity {
    public SmelteryBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public SmelteryBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.SMELTERY_MODULE.get(), pos, state);
    }

    public static void updateCloseBlocks(Level level, BlockPos pos, BlockState state) {
        for (Direction direction : Direction.values()) {
            BlockEntity blockEntity = level.getBlockEntity(pos.relative(direction, 1));
            if (blockEntity instanceof IMaster master) {
                master.notifyChange(pos, state);
                break;
            } else if (blockEntity instanceof SmelteryBlockEntity module && module.hasMaster()) {
                module.notifyMasterOfChange(pos, state, false);
                break;
            }
        }
    }
}

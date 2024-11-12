package net.kermir.meltit.block.multiblock;

import net.kermir.meltit.MeltIt;
import net.kermir.meltit.block.multiblock.module.SmelteryModuleBlock;
import net.kermir.meltit.util.BlockEntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.kermir.meltit.block.multiblock.controller.entity.SmelteryControllerBlockEntity.FACING;
import static net.kermir.meltit.block.multiblock.module.SmelteryModuleBlock.IN_MULTIBLOCK;

public class SmelteryMultiblock {

    private IMaster controller;
    private BlockPos controllerPos;
    private BlockState controllerState;
    private int limitWidth = 7;
    private int limitDepth = 7;
    private int limitHeight = 9;

    public SmelteryMultiblock(IMaster controller, BlockPos controllerPos, BlockState controllerState) {
        this.controller = controller;
        this.controllerPos = controllerPos;
        this.controllerState = controllerState;
    }

    public void checkStructureIntegrity(Level pLevel) {}

    private List<BlockPos> mbBase;
    private List<List<BlockPos>> rings;

    public boolean structureCheck(Level pLevel) {
        if (pLevel == null) return false;
        BlockPos currentlyCheckedPos = controllerPos.below();
        Direction facing = controllerState.getValue(FACING);
        //straight line to the base of the structure
        while (pLevel.getBlockState(currentlyCheckedPos).getBlock() instanceof IServant servantBlock) {
            //updateMaster(pLevel, currentlyCheckedPos, true);
            currentlyCheckedPos = currentlyCheckedPos.below();
        }
        //first block of the base adjacent to the master
        currentlyCheckedPos = currentlyCheckedPos.relative(facing.getOpposite(), 1);
        //go to left first and get the left part of the ring (if there's any)
        //we don't have a base if this returns false
        //Bro you are setting the value to the block class not the instance you dum
        MeltIt.LOGGER.debug("{}", currentlyCheckedPos);
        while (pLevel.getBlockState(currentlyCheckedPos).getBlock() instanceof IServant servantBlock) {
            //if (servantBlock.optionalSetMaster(this)) return false;
            //updateMaster(pLevel, currentlyCheckedPos, true);
            MeltIt.LOGGER.debug("{}", currentlyCheckedPos);
            //Clockwise is to the left from our pov
            currentlyCheckedPos = currentlyCheckedPos.relative(facing.getClockWise(), 1);
        }

        pLevel.removeBlock(currentlyCheckedPos, false);

        return false;
    }


    /**
     * first it goes to the far left and after there are no further IServants it starts from there
     * going all the way to the left and then goes all the way to the right
     * from right to "top" (back), after that to the left and then back before stopping at the first block
     * @param pLevel the current world
     * @param startingPos where the check should start (preferably at the first base block)
     * @return the BlockPos of an invalid block
     */
    public BlockPos getMBRingBase(Level pLevel, BlockPos startingPos) {
        while (getBlockAt(pLevel, startingPos) instanceof  IServant servant) {

        }

        return null;
    };

    public Block getBlockAt(Level pLevel, BlockPos pPos) {
        return pLevel.getBlockState(pPos).getBlock();
    }


    public void updateMaster(Level level, BlockPos pos, boolean add) {
        BlockState state = level.getBlockState(pos);
        if (state.hasProperty(IN_MULTIBLOCK) && state.getValue(IN_MULTIBLOCK) != add) {
            level.setBlock(pos, state.setValue(IN_MULTIBLOCK, add), Block.UPDATE_CLIENTS);
        }

        BlockEntityHelper.get(IServant.class, level, pos).ifPresent(
                //TODO force set master?
                add ? blockentity -> blockentity.setPossibleMaster(controller) : blockentity -> blockentity.removeMaster(controller));
    }
}

package net.kermir.meltit.block.multiblock;

import net.kermir.meltit.MeltIt;
import net.kermir.meltit.networking.PacketChannel;
import net.kermir.meltit.networking.packet.RenderBoxPacket;
import net.kermir.meltit.util.BlockEntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static net.kermir.meltit.block.multiblock.controller.entity.SmelteryControllerBlockEntity.FACING;
import static net.kermir.meltit.block.multiblock.module.SmelteryModuleBlock.IN_MULTIBLOCK;

public class SmelteryMultiblock {

    private IMaster controller;
    private BlockPos controllerPos;
    private BlockState controllerState;
    private static final int limitWidth = 7;
    private static final int limitDepth = 7;
    private static final int limitHeight = 9;
    private int width = 0;
    private int depth = 0;
    private int height = 0;


    public SmelteryMultiblock(IMaster controller, BlockPos controllerPos, BlockState controllerState) {
        this.controller = controller;
        this.controllerPos = controllerPos;
        this.controllerState = controllerState;
    }

    public void checkStructureIntegrity(Level pLevel) {}

    private List<BlockPos> mbBase;
    private List<List<BlockPos>> rings;

    public boolean structureCheck(Level pLevel) {
        this.width=0;
        this.depth=0;
        this.height=0;

        Direction facing = controllerState.getValue(FACING);
        BlockPos.MutableBlockPos cPos = controllerPos.below().mutable();

        //get to the bottom
        while (pLevel.getBlockEntity(cPos) instanceof IServant servant) {
            MeltIt.LOGGER.debug("to bottom {}", cPos);
            if (servant.hasMaster()) return false;
            cPos.move(Direction.DOWN);
        }

        cPos.move(facing.getOpposite());

        LinkedHashSet<BlockPos> ringBase = getMBRingBase(pLevel, cPos, facing.getOpposite());

        for (BlockPos pos : ringBase ) {
            MeltIt.LOGGER.debug("contians {}", pos);
            if (!pLevel.isClientSide()) PacketChannel.sendToAllClients(new RenderBoxPacket(pos));
        }
        MeltIt.LOGGER.debug("ring base block amount {}", ringBase.size());

        MeltIt.LOGGER.debug("""
                dimensions:\s
                Height: {},
                Width:  {},
                Depth:  {}""", this.height, this.width, this.depth);

        return false;
    }


    /**
     * will not return a list, only checks if they are correct, so don't place functional blocks here, they won't work
     * @param level the current world bruh
     * @param leftCorner  from top view while controller is pointing towards the hotbar
     * @param rightCorner from top view while controller is pointing towards the hotbar
     */
    public boolean checkBase(Level level, BlockPos leftCorner, BlockPos rightCorner) {
        boolean ret = true;

        for (BlockPos pos : BlockPos.betweenClosed(leftCorner, rightCorner)) {
            MeltIt.LOGGER.debug("checked at {}", pos);
            if (!(level.getBlockEntity(pos) instanceof IServant)) {
                ret = false;
                MeltIt.LOGGER.debug("problem at {}", pos);
                break;
            }
        }

        return ret;
    }


    /**
     * first it goes to the far left and after there are no further IServants it starts from there
     * going all the way to the left and then goes all the way to the right
     * from right to "top" (back), after that to the left and then back before stopping at the first block
     * @param pLevel the current world
     * @param startingPos where the check should start (preferably at the first base block)
     * @param pDirection should be opposite of where the controller is looking
     * @return the BlockPos of an invalid block
     */
    //fuck it, collect it in a list
    //when i say left, its from my pov, I mean it when the controller is looking at me
    private LinkedHashSet<BlockPos> getMBRingBase(Level pLevel, BlockPos startingPos, Direction pDirection) {
        BlockPos.MutableBlockPos cPos = startingPos.mutable(); //currently checked pos

        //go left first
        LinkedHashSet<BlockPos> ring = new LinkedHashSet<>();
        while (pLevel.getBlockEntity(cPos) instanceof IServant servant) {
            if (servant.hasMaster()) return null;
            MeltIt.LOGGER.debug("{} not in ring (yet)", cPos);
            cPos.move(pDirection.getCounterClockWise());
        }

        //correction to go back to the right a bit as we overstepped from the while loop
        cPos.move(pDirection.getClockWise());

        //go right, start adding to the ring
        while (pLevel.getBlockEntity(cPos) instanceof IServant servant) {
            if (servant.hasMaster()) return null;
            MeltIt.LOGGER.debug("going right {}", cPos);
            ring.add(cPos.immutable());
            this.width++;
            cPos.move(pDirection.getClockWise());
        }

        //correction to go back to the left a bit
        cPos.move(pDirection.getCounterClockWise());

        //go back (like further away from the controller)
        while (pLevel.getBlockEntity(cPos) instanceof IServant servant) {
            if (servant.hasMaster()) return null;
            MeltIt.LOGGER.debug("going away the controller {}", cPos);
            ring.add(cPos.immutable());
            this.depth++;
            cPos.move(pDirection);
        }

        //correction to come forward
        cPos.move(pDirection.getOpposite());

        //go left
        while (pLevel.getBlockEntity(cPos) instanceof IServant servant) {
            if (servant.hasMaster()) return null;
            MeltIt.LOGGER.debug("going left {}", cPos);
            ring.add(cPos.immutable());
            cPos.move(pDirection.getCounterClockWise());
        }

        //correction to go right
        cPos.move(pDirection.getClockWise());
        //move towards the controller once so the contains check won't bonk it
        //cPos.move(pDirection.getOpposite());

        //come towards the controller
        while (pLevel.getBlockEntity(cPos) instanceof IServant servant) {
            //if (ring.contains(cPos)) break;
            if (servant.hasMaster()) return null;
            MeltIt.LOGGER.debug("going towards the controller {}", cPos);
            ring.add(cPos.immutable());
            cPos.move(pDirection.getOpposite());
        }

        return ring;
    };



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

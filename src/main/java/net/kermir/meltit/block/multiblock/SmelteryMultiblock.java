package net.kermir.meltit.block.multiblock;

import net.kermir.meltit.MeltIt;
import net.kermir.meltit.util.BlockEntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static net.kermir.meltit.block.multiblock.controller.entity.SmelteryControllerBlockEntity.FACING;
import static net.kermir.meltit.block.multiblock.module.SmelteryModuleBlock.IN_MULTIBLOCK;

@SuppressWarnings("FieldCanBeLocal")
public class SmelteryMultiblock {

    private final IMaster controller;
    private final BlockPos controllerPos;
    private final BlockState controllerState;
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

    private List<List<BlockPos>> rings;
    private List<BlockPos> base;
    //Utility

    public int InteriorSize(Level level) {
        structureCheck(level);
        return this.width*this.height*this.depth;
    }


    //Structure
    public boolean structureCheck(Level pLevel) {
        this.base = new ArrayList<>();
        this.rings = new ArrayList<>();
        this.width=0;
        this.depth=0;
        this.height=0;

        Direction facing = controllerState.getValue(FACING);
        BlockPos.MutableBlockPos cPos = controllerPos.below().mutable();

        int minSupposedHeight = 1;
        //get to the bottom
        while (pLevel.getBlockEntity(cPos) instanceof IServant servant) {
            if (pLevel.getBlockEntity(cPos.mutable().move(facing.getOpposite())) instanceof IServant) {
                break;
            }
            if (!servant.isValidMaster(controller)) return false;
            cPos.move(Direction.DOWN);
            minSupposedHeight++;
        }

        cPos.move(facing.getOpposite());

        if (!(pLevel.getBlockEntity(cPos) instanceof IServant)) return false;

        LinkedHashSet<BlockPos> ringBaseSet = getMBRingBase(pLevel, cPos.immutable(), facing.getOpposite());
        if (ringBaseSet == null) return false;
        List<BlockPos> ringBase = ringBaseSet.stream().toList();

        //jesus christ

        if (ringBase.get(0).hashCode() != ringBase.get(ringBase.size()-1).mutable().move(facing).immutable().hashCode()) return false;

        if (!checkBase(pLevel, ringBase.get(0), ringBase.get(this.width-1 + this.depth-1))) return false;

        cPos.set(ringBase.get(0));
        cPos.move(Direction.UP);
        cPos.move(facing);

        while (getMBRing(pLevel, cPos,facing.getOpposite()) != null) {
            cPos.move(Direction.UP);
            this.height++;
            if (this.height == limitHeight) break;
        }

        if (this.height < minSupposedHeight) {
            return false;
        }


        cPos.move(Direction.DOWN);
        cPos.move(facing.getOpposite().getClockWise(), width-1);
        cPos.move(facing.getOpposite(), depth);

        BlockPos interiorBottomLeft = cPos.immutable();

        cPos.move(Direction.DOWN, height-1);
        cPos.move(facing.getOpposite().getCounterClockWise(), width-1);
        cPos.move(facing, depth-1);

        BlockPos interiorTopRight = cPos.immutable();

        if (!checkInterior(pLevel, interiorBottomLeft, interiorTopRight)) return false;

        MeltIt.LOGGER.debug("""
                dimensions:\s
                Height: {},
                Width:  {},
                Depth:  {}""", this.height, this.width, this.depth);

        assignMasters(pLevel);
        return true;
    }

    public void assignMasters(Level level) {
        for (BlockPos pos : base) {
            updateMaster(level, pos, true);
        }

        for (List<BlockPos> posList : rings) {
            for (BlockPos pos : posList) {
                updateMaster(level, pos, true);
            }
        }
    }

    public LinkedHashSet<BlockPos> getMBRing(Level pLevel, BlockPos startingPos, Direction pDirection) {
        LinkedHashSet<BlockPos> ring = new LinkedHashSet<>();

        BlockPos.MutableBlockPos cPos = startingPos.mutable(); //currently checked pos


        for (int i = 0; i < this.width; i++) {
            if (pLevel.getBlockEntity(cPos) instanceof IServant servant) {
                if (!servant.isValidMaster(controller)) return null;
                ring.add(cPos.immutable());
                cPos.move(pDirection.getClockWise());
            } else if (cPos.immutable().hashCode() == controllerPos.hashCode()) {
                ring.add(cPos.immutable());
                cPos.move(pDirection.getClockWise());
            } else {
                return null;
            }
        }

        cPos.move(pDirection);


        for (int i = 0; i < this.depth; i++) {
            if (pLevel.getBlockEntity(cPos) instanceof IServant servant) {
                if (!servant.isValidMaster(controller)) return null;
                ring.add(cPos.immutable());
                cPos.move(pDirection);
            } else {
                return null;
            }
        }

        cPos.move(pDirection.getCounterClockWise());

        for (int i = 0; i < this.width; i++) {
            if (pLevel.getBlockEntity(cPos) instanceof IServant servant) {
                if (!servant.isValidMaster(controller)) return null;
                ring.add(cPos.immutable());
                cPos.move(pDirection.getCounterClockWise());
            } else {
                return null;
            }
        }

        cPos.move(pDirection.getOpposite());

        for (int i = 0; i < this.depth; i++) {
            if (pLevel.getBlockEntity(cPos) instanceof IServant servant) {
                if (!servant.isValidMaster(controller)) {
                    return null;
                }
                ring.add(cPos.immutable());
                cPos.move(pDirection.getOpposite());
            } else {
                return null;
            }
        }

        rings.add(new ArrayList<>(ring));
        return ring;
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
            base.add(pos);
            if (!(level.getBlockEntity(pos) instanceof IServant)) {
                ret = false;
                base.clear();
                break;
            }
        }


        return ret;
    }

    public boolean checkInterior(Level level, BlockPos leftBottomCorner, BlockPos rightTopCorner) {
        boolean ret = true;

        for (BlockPos pos : BlockPos.betweenClosed(leftBottomCorner, rightTopCorner)) {
            if (!(level.getBlockState(pos).is(Blocks.AIR))) {
                ret = false;
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
     * @return A hashset toe ensure it is without duplicates
     */
    //fuck it, collect it in a list
    //when i say left, its from my pov, I mean it when the controller is looking at me
    private LinkedHashSet<BlockPos> getMBRingBase(Level pLevel, BlockPos startingPos, Direction pDirection) {
        BlockPos.MutableBlockPos cPos = startingPos.mutable(); //currently checked pos

        //go left first
        LinkedHashSet<BlockPos> ring = new LinkedHashSet<>();
        while (pLevel.getBlockEntity(cPos) instanceof IServant servant) {
            if (!servant.isValidMaster(controller)) return null;
            if (checkAlmsostAbove(pLevel, cPos, pDirection.getCounterClockWise())) {
                cPos.move(pDirection.getCounterClockWise());
                break;
            }
            //MeltIt.LOGGER.debug("{} not in ring (yet)", cPos);
            cPos.move(pDirection.getCounterClockWise());
        }

        //correction to go back to the right a bit as we overstepped from the while loop
        cPos.move(pDirection.getClockWise());

        //go right, start adding to the ring
        while (pLevel.getBlockEntity(cPos) instanceof IServant servant) {
            if (!servant.isValidMaster(controller)) return null;
            //MeltIt.LOGGER.debug("going right {}", cPos);
            ring.add(cPos.immutable());
            this.width++;
            if (checkAlmsostAbove(pLevel, cPos, pDirection.getClockWise())) {
                cPos.move(pDirection.getClockWise());
                break;
            }
            cPos.move(pDirection.getClockWise());
        }

        if (width > limitWidth) return null;

        //correction to go back to the left a bit
        cPos.move(pDirection.getCounterClockWise());

        //go back (like further away from the controller)
        while (pLevel.getBlockEntity(cPos) instanceof IServant servant) {
            if (!servant.isValidMaster(controller)) return null;
            //MeltIt.LOGGER.debug("going away the controller {}", cPos);
            ring.add(cPos.immutable());
            this.depth++;
            if (checkAlmsostAbove(pLevel, cPos, pDirection)) {
                cPos.move(pDirection);
                break;
            }
            cPos.move(pDirection);
        }

        if (depth > limitDepth) return null;

        //correction to come forward
        cPos.move(pDirection.getOpposite());

        //go left
        while (pLevel.getBlockEntity(cPos) instanceof IServant servant) {
            if (!servant.isValidMaster(controller)) return null;
            //MeltIt.LOGGER.debug("going left {}", cPos);
            ring.add(cPos.immutable());
            if (checkAlmsostAbove(pLevel, cPos, pDirection.getCounterClockWise())) {
                cPos.move(pDirection.getCounterClockWise());
                break;
            }
            cPos.move(pDirection.getCounterClockWise());
        }

        //correction to go right
        cPos.move(pDirection.getClockWise());
        //move towards the controller once so the contains check won't bonk it
        //cPos.move(pDirection.getOpposite());

        //come towards the controller
        while (pLevel.getBlockEntity(cPos) instanceof IServant servant) {
            if (!servant.isValidMaster(controller)) return null;
            //MeltIt.LOGGER.debug("{}",cPos.immutable());
            BlockPos kPos = cPos.mutable().move(pDirection.getOpposite(), 1).move(Direction.UP);
            if (kPos.immutable().hashCode() == controllerPos.hashCode()) break;
            ring.add(cPos.immutable());
            if (checkAlmsostAbove(pLevel, cPos, pDirection.getOpposite())) {
                cPos.move(pDirection.getOpposite());
                break;
            }
            cPos.move(pDirection.getOpposite());
        }


        MeltIt.LOGGER.debug("{}", new ArrayList<>(ring).get(ring.size()-1));
        return ring;
    }

    //check if in the given direction, and above that one is a servant block
    private boolean checkAlmsostAbove(Level level,BlockPos pos, Direction curHeadedDirection) {
        BlockPos kPos = pos.mutable().move(curHeadedDirection, 1).move(Direction.UP);
        return level.getBlockEntity(kPos) instanceof IServant;
    }

    public void updateMaster(Level level, BlockPos pos, boolean add) {
        BlockState state = level.getBlockState(pos);
        if (state.hasProperty(IN_MULTIBLOCK) && state.getValue(IN_MULTIBLOCK) != add) {
            level.setBlock(pos, state.setValue(IN_MULTIBLOCK, add), Block.UPDATE_CLIENTS);
        }

        BlockEntityHelper.get(IServant.class, level, pos).ifPresent(
                add ? blockentity -> blockentity.setPossibleMaster(controller) : blockentity -> blockentity.removeMaster(controller));
    }
}

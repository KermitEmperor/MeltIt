package net.kermir.meltit.block.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeBlockEntity;

@SuppressWarnings("unused")
public interface IServant extends IForgeBlockEntity {
    BlockPos getMasterPos();
    /**
     * @param pos of the changed block
     * @param state of the changed block
     * @param shouldDeleteItself should the block entity of the block remove itself from the level
    * */
    void notifyMasterOfChange(BlockPos pos, BlockState state, boolean shouldDeleteItself);
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isValidMaster(IMaster master);
    boolean isMaster(BlockPos masterPos);
    boolean hasMaster();
    void setPossibleMaster(IMaster master);
    void removeMaster(IMaster master);
}

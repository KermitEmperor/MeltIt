package net.kermir.meltit.block.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeBlockEntity;

public interface IServant extends IForgeBlockEntity {
    BlockPos getMasterPos();
    void notifyMasterOfChange(BlockPos pos, BlockState state);
    boolean isValidMaster(IMaster master);
    void setPossibleMaster(IMaster master);
    void removeMaster(IMaster master);
}

package net.kermir.meltit.block.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeBlockEntity;

@SuppressWarnings("unused")
public interface IServant extends IForgeBlockEntity {
    BlockPos getMasterPos();
    void notifyMasterOfChange(BlockPos pos, BlockState state);
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isValidMaster(IMaster master);
    boolean isMaster(BlockPos masterPos);
    boolean hasMaster();
    void setPossibleMaster(IMaster master);
    void removeMaster(IMaster master);
}

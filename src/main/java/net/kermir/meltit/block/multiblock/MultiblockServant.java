package net.kermir.meltit.block.multiblock;

import net.minecraft.world.level.block.entity.BlockEntity;

public interface MultiblockServant {

    BlockEntity getMaster();

    void setMaster(BlockEntity master);

    default boolean optionalSetMaster(BlockEntity master) {
        if (this.getMaster() == null) {
            this.setMaster(master);
            return true;
        } else {
            return false;
        }
    }
}

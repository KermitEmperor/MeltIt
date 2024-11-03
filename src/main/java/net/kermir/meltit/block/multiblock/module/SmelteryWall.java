package net.kermir.meltit.block.multiblock.module;

import net.kermir.meltit.block.multiblock.MultiblockServant;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SmelteryWall extends Block implements MultiblockServant {
    BlockEntity master;
    public SmelteryWall(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public BlockEntity getMaster() {
        return this.master;
    }

    @Override
    public void setMaster(BlockEntity master) {
        this.master = master;
    }
}

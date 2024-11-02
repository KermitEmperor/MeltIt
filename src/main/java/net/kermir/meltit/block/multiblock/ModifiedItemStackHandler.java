package net.kermir.meltit.block.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraftforge.items.ItemStackHandler;


public class ModifiedItemStackHandler extends ItemStackHandler {
    public ModifiedItemStackHandler(int size, BlockPos blockPos) {
        super(size);
        this.pos = blockPos;
    }

    public BlockPos pos;

}

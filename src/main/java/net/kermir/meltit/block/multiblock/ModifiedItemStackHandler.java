package net.kermir.meltit.block.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModifiedItemStackHandler extends ItemStackHandler {
    public ModifiedItemStackHandler(int size) {
        super(size);
    }

    public ModifiedItemStackHandler(int size, BlockPos blockPos) {
        super(size);
        this.pos = blockPos;
    }

    public BlockPos pos;

}

package net.kermir.meltit.item;

import net.kermir.meltit.block.BlockRegistry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

public class CreativeTab extends CreativeModeTab {

    public CreativeTab(String label) {
        super(label);
    }

    @Override
    public @NotNull ItemStack makeIcon() {
        return new ItemStack(BlockRegistry.SMELTERY_CONTROLLER.get().asItem());
    }

    @Override
    public boolean hasSearchBar() {
        return true;
    }
}

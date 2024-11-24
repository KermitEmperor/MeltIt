package net.kermir.meltit.util;

import net.kermir.meltit.MeltIt;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class ResizeableItemStackHandler extends ItemStackHandler {
    public ResizeableItemStackHandler(int size) {
        super(size);
    }

    public void onSizeChanged(int size) {

    }

    public void onSizeGotSmaller(int size, NonNullList<ItemStack> excessItems) {

    }

    public void onSizeGotBigger(int size) {

    }

    public void onSizeDidNotChange(int size) {

    }


    @Override
    public void setSize(int size) {
        onSizeChanged(size);
        MeltIt.LOGGER.debug("size set to: {}",size);
        /*
        if (level != null && !level.isClientSide()) {
            PacketChannel.sendToAllClients(new CloseSmelteryScreenPacket(worldPosition));
        }*/
        if (size>stacks.size()) {
            List<ItemStack> combined = new ArrayList<>();
            List<ItemStack> previous = stacks;
            List<ItemStack> additional = NonNullList.withSize(size-stacks.size(),ItemStack.EMPTY);
            combined.addAll(previous);
            combined.addAll(additional);

            //what
            stacks = NonNullList.of(ItemStack.EMPTY, combined.toArray(new ItemStack[0]));

            onSizeGotBigger(size);
        } else if (stacks.size()==size) {
            onSizeDidNotChange(size);
        } else if (size<stacks.size()) {
            int differance = stacks.size()-size;
            List<ItemStack> remainderItems = stacks.subList(stacks.size()-differance,stacks.size());
            NonNullList<ItemStack> excess = NonNullList.create();
            excess.addAll(remainderItems);

            stacks = NonNullList.of(ItemStack.EMPTY, stacks.subList(0, stacks.size()-differance).toArray(new ItemStack[0]));

            onSizeGotSmaller(size, excess);
        } else {
            super.setSize(size);
        }
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        if (slot < 0|| slot >= this.stacks.size()) {
            return ItemStack.EMPTY;
        }
        return super.getStackInSlot(slot);
    }
}

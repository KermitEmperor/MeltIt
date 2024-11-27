package net.kermir.meltit.screen.slot;

import net.kermir.meltit.block.multiblock.controller.heat.HeatHandler;
import net.kermir.meltit.util.ResizeableItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SmelterySlot extends SlotItemHandler {
    private HeatHandler heatHandler;

    public SmelterySlot(ResizeableItemStackHandler itemHandler, HeatHandler heatHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
        this.heatHandler = heatHandler;
    }

    public float getProgress() {
        if (heatHandler.validateSlot(getSlotIndex())) {
            return heatHandler.getProgress(getSlotIndex());
        } else return 0F;
    }
}

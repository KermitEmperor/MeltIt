package net.kermir.meltit.screen.slot;

import net.kermir.meltit.block.multiblock.controller.HeatableItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SmelterySlot extends SlotItemHandler {
    public SmelterySlot(HeatableItemStackHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    public float getProgressInSlot() {
        HeatableItemStackHandler handler = (HeatableItemStackHandler) getItemHandler();

        return handler.getProgressInSlot(getSlotIndex());
    }

    public HeatableItemStackHandler.HeatState getHeatStateInSlot() {
        HeatableItemStackHandler handler = (HeatableItemStackHandler) getItemHandler();

        return handler.getHeatStateInSlot(getSlotIndex());
    }
}

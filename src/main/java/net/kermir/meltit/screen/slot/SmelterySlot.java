package net.kermir.meltit.screen.slot;

import net.kermir.meltit.block.multiblock.controller.heat.HeatHandler;
import net.kermir.meltit.block.multiblock.controller.heat.HeatState;
import net.kermir.meltit.item.util.ResizeableItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SmelterySlot extends SlotItemHandler {
    private HeatHandler heatHandler;

    public SmelterySlot(ResizeableItemStackHandler itemHandler, HeatHandler heatHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
        this.heatHandler = heatHandler;
    }

    public float getProgress() {
        return heatHandler.getProgress(getSlotIndex());
    }

    public HeatState getHeatState() {
        if (heatHandler.validateSlot(getSlotIndex())) {
            return heatHandler.getHeatState(getSlotIndex());
        } else return HeatState.UNMELTABLE;
    }
}

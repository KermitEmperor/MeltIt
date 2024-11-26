package net.kermir.meltit.block.multiblock.controller;

import net.kermir.meltit.MeltIt;
import net.kermir.meltit.util.ResizeableItemStackHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public class HeatableItemStackHandler extends ResizeableItemStackHandler {
    private final List<Pair<HeatState, Float>> heatMap;

    public enum HeatState {
        HEATING,
        UNMELTABLE,
        TOO_COLD,
        NO_SPACE
    }

    public HeatableItemStackHandler(int size) {
        super(size);
        this.heatMap = Collections.synchronizedList(new ArrayList<>());
        for (int i = 0; i < size; i++) {
            this.heatMap.add(pair());
        }
    }

    public HeatState getHeatStateInSlot(int slot) {
        return heatMap.get(slot).getA();
    }

    public float getProgressInSlot(int slot) {
        return heatMap.get(slot).getB();
    }

    public void setHeatStateInSlot(int slot, HeatState state, Float progress) {
        heatMap.set(slot, pair(state, progress));
    }

    public void setHeatStateInSlot(int slot, HeatState state) {
        heatMap.set(slot, pair(state, heatMap.get(slot).getB()));
    }

    public void resetHeatStateInSlot(int slot) {
        heatMap.set(slot, pair());
    }

    public void incrementProgress(int slot, float amount) {
        HeatState currentState = heatMap.get(slot).getA();
        float newProgress = heatMap.get(slot).getB() + amount;
        heatMap.set(slot, pair(currentState, Mth.clamp(newProgress, 0F, 1F) ));
    }

    public int getHeatMapSize() {
        return heatMap.size();
    }

    @Override
    public void setSize(int size) {
        super.setSize(size);
        if (size > heatMap.size()) {
            int mapSize = size-heatMap.size();
            for (int i = 0; i < (mapSize); i++) {
                heatMap.add(pair(HeatState.UNMELTABLE));
            }
        } else if (size < heatMap.size()) {
            List<Pair<HeatState, Float>> newMap;
            if (!heatMap.isEmpty()) newMap = heatMap.subList(0, size <= 1 ? 0 : size-1);
            else newMap = new ArrayList<>();
            heatMap.clear();
            Minecraft.getInstance().execute(() -> {
                try {
                    if (!newMap.isEmpty()) heatMap.addAll(newMap);
                } catch (Exception error) {
                    MeltIt.LOGGER.warn(error.toString());
                }
            });
        }
    }


    @Override
    public CompoundTag serializeNBT() {
        ListTag nbtTagList = new ListTag();
        for (int i = 0; i < stacks.size(); i++)
        {
            if (!stacks.get(i).isEmpty())
            {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                stacks.get(i).save(itemTag);

                CompoundTag heatTag = new CompoundTag();
                heatTag.putString("State", heatMap.get(i).getA().toString());
                heatTag.putFloat("Progress", heatMap.get(i).getB());
                itemTag.put("HeatState", heatTag );

                nbtTagList.add(itemTag);
            }
        }
        CompoundTag nbt = new CompoundTag();
        nbt.put("Items", nbtTagList);
        nbt.putInt("Size", stacks.size());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        setSize(nbt.contains("Size", Tag.TAG_INT) ? nbt.getInt("Size") : stacks.size());
        heatMap.clear();
        ListTag tagList = nbt.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++)
        {
            CompoundTag itemTags = tagList.getCompound(i);
            int slot = itemTags.getInt("Slot");

            CompoundTag heatTag = itemTags.getCompound("HeatState");


            if (slot >= 0 && slot < stacks.size())
            {
                stacks.set(slot, ItemStack.of(itemTags));
                heatMap.add(pair(HeatState.valueOf(heatTag.getString("State")), heatTag.getFloat("Progress")));
            }
        }
        onLoad();
    }

    private Pair<HeatState, Float> pair(HeatState state, Float progress) {
        return new Pair<>(state, progress);
    }

    private Pair<HeatState, Float> pair(HeatState state) {
        return new Pair<>(state, 0F);
    }

    private Pair<HeatState, Float> pair() {
        return new Pair<>(HeatState.UNMELTABLE, 0F);
    }
}

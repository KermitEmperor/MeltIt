package net.kermir.meltit.block.multiblock.controller.heat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraftforge.common.util.INBTSerializable;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HeatHandler implements INBTSerializable<CompoundTag> {
    List<Pair<HeatState, Float>> heatMap;

    public void setSize(int newSize) {
        int mapSize = heatMap.size();
        if (mapSize < newSize )  {
            for (int i = 0; i < (newSize-mapSize); i++) {
                heatMap.add(pair());
            }
        } else if (mapSize > newSize) {
            if (newSize == 0) {
                heatMap.clear();
                return;
            }
            heatMap.retainAll(heatMap.subList(0, newSize-1));
        }
    }

    public HeatHandler(int size) {
        this.heatMap = new ArrayList<>();
    }

    public Pair<HeatState, Float> pair() {
        return pair(HeatState.UNMELTABLE);
    }

    public Pair<HeatState, Float> pair(HeatState state) {
        return pair(state, 0F);
    }

    public Pair<HeatState, Float> pair(HeatState sate, float progress) {
        return new Pair<>(sate, progress);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        for (Pair<HeatState, Float> pair : heatMap) {
            CompoundTag stateTag = new CompoundTag();
            stateTag.putString("State", pair.getA().toString());
            stateTag.putFloat("Progress", pair.getB());
            nbt.put(String.valueOf(heatMap.indexOf(pair))  ,stateTag);
        }

        return nbt;
    }

 /*
    @Override
    public CompoundTag serializeNBT()
    {
        ListTag nbtTagList = new ListTag();
        for (int i = 0; i < stacks.size(); i++)
        {
            if (!stacks.get(i).isEmpty())
            {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                stacks.get(i).save(itemTag);
                nbtTagList.add(itemTag);
            }
        }
        CompoundTag nbt = new CompoundTag();
        nbt.put("Items", nbtTagList);
        nbt.putInt("Size", stacks.size());
        return nbt;
    }
    */
    @Override
    public void deserializeNBT(CompoundTag nbt) {
        heatMap.clear();
        for (int i = 0; i < nbt.size(); i++) {
            heatMap.add(pair(HeatState.valueOf(nbt.getString("State")), nbt.getFloat("Progress")));
        }
    }
}

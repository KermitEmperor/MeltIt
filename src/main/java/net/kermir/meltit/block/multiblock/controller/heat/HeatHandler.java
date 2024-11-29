package net.kermir.meltit.block.multiblock.controller.heat;

import net.kermir.meltit.MeltIt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraftforge.common.util.INBTSerializable;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
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

            heatMap.retainAll(heatMap.subList(0, newSize));
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

    public boolean validateSlot(int slot) {
        return (slot < heatMap.size());
    }

    public void incrementProgress(int slot, float amount) {
        if (!validateSlot(slot)) return;
        Pair<HeatState, Float> pair = heatMap.get(slot);

        if (pair.getA().equals(HeatState.MELTING)) {
            heatMap.set(slot, pair(HeatState.MELTING,  Mth.clamp(pair.getB()+amount, 0F, 1F)));
        }
    }

    public void forceProgress(int slot, float amount) {
        if (!validateSlot(slot)) return;
        Pair<HeatState, Float> pair = heatMap.get(slot);

        heatMap.set(slot, pair(HeatState.MELTING, Mth.clamp(pair.getB()+amount, 0F, 1F)));
    }

    public void setProgress(int slot, float amount) {
        setPairData(slot, HeatState.MELTING, amount);
    }

    public void setPairData(int slot, HeatState state, float amount) {
        if (!validateSlot(slot)) return;
        Pair<HeatState, Float> pair = heatMap.get(slot);

        heatMap.set(slot, pair(state, Mth.clamp(amount, 0F, 1F)));
    }

    public float getProgress(int slot) {
        if (validateSlot(slot))
            return heatMap.get(slot).getB();
        else
            return 0F;
    }

    public void setHeatState(int slot, HeatState state) {
        setPairData(slot, state, getProgress(slot));
    }

    public HeatState getHeatState(int slot) {
        if (validateSlot(slot))
            return heatMap.get(slot).getA();
        else
            return HeatState.UNMELTABLE;
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

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        heatMap.clear();
        MeltIt.LOGGER.debug("{}",nbt);
        for (int i = 0; i < nbt.size(); i++) {
            CompoundTag aPair = nbt.getCompound(String.valueOf(i));
            //TODO this
            heatMap.add(pair(HeatState.valueOf(aPair.getString("State").toUpperCase()), aPair.getFloat("Progress")));
        }
    }
}

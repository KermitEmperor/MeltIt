package net.kermir.meltit.block.multiblock.module.entity;

import net.kermir.meltit.block.entity.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class SmelteryFuelTankBlockEntity extends SmelteryBlockEntity {
    private FluidTank fluidTank = new FluidTank(5000);

    public SmelteryFuelTankBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public SmelteryFuelTankBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.SMELTERY_FUEL_TANK_MODULE.get(),pos, state);
    }

    public void consume(int amount) {
        if (!fluidTank.getFluid().isEmpty()) {
            fluidTank.drain(new FluidStack(fluidTank.getFluid(), amount), IFluidHandler.FluidAction.EXECUTE);
        }
    }

    public void fill(int amount) {
        fluidTank.fill(new FluidStack(Fluids.LAVA, amount), IFluidHandler.FluidAction.EXECUTE);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag nbt) {
        fluidTank.writeToNBT(nbt);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        fluidTank.readFromNBT(pTag);
        super.load(pTag);
    }
}

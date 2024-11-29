package net.kermir.meltit.block.multiblock.module.entity;

import net.kermir.meltit.block.entity.BlockEntityRegistry;
import net.kermir.meltit.networking.PacketChannel;
import net.kermir.meltit.networking.packet.UpdateTankContentsPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.CallbackI;

public class SmelteryFuelTankBlockEntity extends SmelteryBlockEntity {
    private final FluidTank fluidTank = new FluidTank(5000) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            //TODO config to restrict what type of fluid fuels are valid
            return super.isFluidValid(stack);
        }
    };

    private LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.empty();

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

    public boolean isEmpty() {
        return fluidTank.isEmpty();
    }

    public Fluid getFluid() {
        return fluidTank.getFluid().getFluid();
    }

    public FluidStack getFluidStack() {
        return fluidTank.getFluid();
    }

    public void setFluidStack(FluidStack stack) {
        fluidTank.setFluid(stack);
    }

    public int getMaxCapacity() {
        return fluidTank.getCapacity();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide())
            PacketChannel.sendToAllClients(new UpdateTankContentsPacket(this.worldPosition, this.getFluidStack()));
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return lazyFluidHandler.cast();
        }
        return super.getCapability(cap);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag nbt) {
        nbt = fluidTank.writeToNBT(nbt);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        fluidTank.readFromNBT(pTag);
        super.load(pTag);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyFluidHandler = LazyOptional.of(() -> fluidTank);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyFluidHandler.invalidate();
    }
}

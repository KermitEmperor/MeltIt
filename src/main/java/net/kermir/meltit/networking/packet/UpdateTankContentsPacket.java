package net.kermir.meltit.networking.packet;

import net.kermir.meltit.block.multiblock.controller.entity.SmelteryControllerBlockEntity;
import net.kermir.meltit.block.multiblock.module.entity.SmelteryFuelTankBlockEntity;
import net.kermir.meltit.screen.SmelteryControllerMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateTankContentsPacket {
    private BlockPos blockPos;
    private FluidStack fluidStack;

    public UpdateTankContentsPacket() {}

    public UpdateTankContentsPacket(BlockPos pos, FluidStack fluidStack) {
        this.blockPos = pos;
        this.fluidStack = fluidStack;
    }

    public UpdateTankContentsPacket(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
        this.fluidStack = buf.readFluidStack();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.blockPos);
        buf.writeFluidStack(this.fluidStack);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        FluidStack fluid = this.fluidStack;
        BlockPos pos = this.blockPos;

        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                SmelteryFuelTankBlockEntity tank = (SmelteryFuelTankBlockEntity) mc.level.getBlockEntity(pos);
                if (tank!=null)
                    tank.setFluidStack(fluid);
            }
        });

        return true;
    }
}

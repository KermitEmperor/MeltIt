package net.kermir.meltit.networking.packet;

import net.kermir.meltit.block.multiblock.controller.entity.SmelteryControllerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class UpdateControllerSizePacket {
    private BlockPos blockPos;
    private int size;

    public UpdateControllerSizePacket() {}

    public UpdateControllerSizePacket(BlockPos blockPos, int size) {
        this.blockPos = blockPos;
        this.size = size;
    }

    public UpdateControllerSizePacket(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
        this.size = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.blockPos);
        buf.writeInt(this.size);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        BlockPos pos = this.blockPos;
        int newSize = this.size;

        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                SmelteryControllerBlockEntity master = (SmelteryControllerBlockEntity) mc.level.getBlockEntity(pos);
                if (master!=null)
                    master.setSize(newSize);
            }

        });

        return true;
    }
}

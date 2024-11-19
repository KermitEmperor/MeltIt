package net.kermir.meltit.networking.packet;

import net.kermir.meltit.render.RenderBox;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RenderBoxPacket{
    private BlockPos blockPos;

    public RenderBoxPacket() {}

    public RenderBoxPacket(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public RenderBoxPacket(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.blockPos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        BlockPos pos = this.blockPos;

        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            RenderBox.addPosition(pos);
        });

        return true;
    }
}

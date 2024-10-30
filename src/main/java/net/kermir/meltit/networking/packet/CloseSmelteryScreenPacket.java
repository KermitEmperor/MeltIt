package net.kermir.meltit.networking.packet;

import net.kermir.meltit.screen.SmelteryControllerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class CloseSmelteryScreenPacket {
    private BlockPos blockPos;

    public CloseSmelteryScreenPacket() {};

    public CloseSmelteryScreenPacket(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public CloseSmelteryScreenPacket(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.blockPos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        BlockPos pos = this.blockPos;

        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof SmelteryControllerScreen) {
                BlockPos targetPos = ((SmelteryControllerScreen) mc.screen).getMenu().blockEntity.getBlockPos();
                if (pos.equals(targetPos)) {
                    mc.setScreen(null);
                }
            }
        });

        return true;
    }
}

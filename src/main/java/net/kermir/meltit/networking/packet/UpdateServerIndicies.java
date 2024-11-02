package net.kermir.meltit.networking.packet;

import net.kermir.meltit.screen.SmelteryControllerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

@SuppressWarnings({"unused"})
public class UpdateServerIndicies {
    private int offset;


    public UpdateServerIndicies() {}

    public UpdateServerIndicies(int offset) {
        this.offset = offset;
    }

    public UpdateServerIndicies(FriendlyByteBuf buf) {
        this.offset = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.offset);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        int off = this.offset;

        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player != null && player.containerMenu instanceof SmelteryControllerMenu menu) {
                menu.reAddingSlots(menu.getPlayerInventory(), off);
            }
        });

        return true;
    }
}

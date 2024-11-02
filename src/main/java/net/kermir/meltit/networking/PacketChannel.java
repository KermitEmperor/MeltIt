package net.kermir.meltit.networking;

import net.kermir.meltit.MeltIt;
import net.kermir.meltit.networking.packet.CloseSmelteryScreenPacket;
import net.kermir.meltit.networking.packet.UpdateServerIndicies;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

@SuppressWarnings({"unused"})
public class PacketChannel {
    private static SimpleChannel INSTANCE;

    private static int packetID = 0;
    private static int id() {
        return packetID++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(MeltIt.MOD_ID, "packetchannel"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(CloseSmelteryScreenPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(CloseSmelteryScreenPacket::encode)
                .decoder(CloseSmelteryScreenPacket::new)
                .consumer(CloseSmelteryScreenPacket::handle)
                .add();

        net.messageBuilder(UpdateServerIndicies.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(UpdateServerIndicies::encode)
                .decoder(UpdateServerIndicies::new)
                .consumer(UpdateServerIndicies::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToAllClients(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }

    public static <MSG> void sendToClient(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}

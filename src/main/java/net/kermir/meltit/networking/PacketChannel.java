package net.kermir.meltit.networking;

import net.kermir.meltit.MeltIt;
import net.kermir.meltit.networking.packet.*;
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

        net.messageBuilder(UpdateServerMenuIndiciesPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(UpdateServerMenuIndiciesPacket::encode)
                .decoder(UpdateServerMenuIndiciesPacket::new)
                .consumer(UpdateServerMenuIndiciesPacket::handle)
                .add();

        net.messageBuilder(RenderBoxPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(RenderBoxPacket::encode)
                .decoder(RenderBoxPacket::new)
                .consumer(RenderBoxPacket::handle)
                .add();

        net.messageBuilder(UpdateControllerSizePacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(UpdateControllerSizePacket::encode)
                .decoder(UpdateControllerSizePacket::new)
                .consumer(UpdateControllerSizePacket::handle)
                .add();

        net.messageBuilder(UpdateTankContentsPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(UpdateTankContentsPacket::encode)
                .decoder(UpdateTankContentsPacket::new)
                .consumer(UpdateTankContentsPacket::handle)
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

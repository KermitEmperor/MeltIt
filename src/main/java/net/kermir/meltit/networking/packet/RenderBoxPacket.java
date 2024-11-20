package net.kermir.meltit.networking.packet;

import net.kermir.meltit.render.RenderBox;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class RenderBoxPacket{
    private BlockPos blockPos;
    private float alpha;
    private float red;
    private float green;
    private float blue;
    private float extraSize;

    public RenderBoxPacket() {}

    public RenderBoxPacket(BlockPos blockPos, float initialAlpha, float red, float green, float blue, float extraSize) {
        this.blockPos = blockPos;
        this.alpha = initialAlpha;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.extraSize = extraSize;
    }

    public RenderBoxPacket(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
        this.alpha = buf.readFloat();
        this.red = buf.readFloat();
        this.green = buf.readFloat();
        this.blue = buf.readFloat();
        this.extraSize = buf.readFloat();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.blockPos);
        buf.writeFloat(this.alpha);
        buf.writeFloat(this.red);
        buf.writeFloat(this.green);
        buf.writeFloat(this.blue);
        buf.writeFloat(this.extraSize);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        BlockPos pos = this.blockPos;
        float pAlpha = this.alpha;
        float pRed = this.red;
        float pGreen = this.green;
        float pBlue = this.blue;
        float inflate = this.extraSize;

        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> RenderBox.addPosition(pos, pAlpha, pRed, pGreen, pBlue, inflate));

        return true;
    }
}

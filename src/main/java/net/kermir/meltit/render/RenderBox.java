package net.kermir.meltit.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.kermir.meltit.MeltIt;
import net.kermir.meltit.networking.PacketChannel;
import net.kermir.meltit.networking.packet.RenderBoxPacket;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = MeltIt.MOD_ID)
public class RenderBox {
    private static final HashMap<BlockPos, ArrayList<Float>> blockPosList = new HashMap<>();


    /**
     * values need to be between 0 and 1
    */
    public static void addPosition(BlockPos pos, float initialAlpha, float red, float green, float blue, float extraSize) {
        ArrayList<Float> initialList = new ArrayList<>();
        initialList.add(initialAlpha);
        initialList.add(red);
        initialList.add(green);
        initialList.add(blue);
        initialList.add(extraSize);
        blockPosList.put(pos, initialList);
    }

    public static void sendToRender(Level level, BlockPos pos, float initialAlpha, float red, float green, float blue, float extraSize) {
        if (!(level.isClientSide()))
            PacketChannel.sendToAllClients(new RenderBoxPacket(
                    pos,
                    initialAlpha,
                    red,
                    green,
                    blue,
                    extraSize
            ));
    }

    public static void sendToRender(Level level, BlockPos pos, float red, float green, float blue, float extraSize) {
        sendToRender(level, pos,1F, red, green, blue, extraSize);
    }

    public static void sendToRender(Level level, BlockPos pos, float red, float green, float blue) {
        sendToRender(level, pos,1F, red, green, blue, 0F);
    }

    public static void sendToRender(Level level, BlockPos pos) {
        sendToRender(level, pos,1F,1F,0.7F, 0F, 0F);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onRenderLevelLast(RenderLevelStageEvent event) {
        if (!(event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY)) return;
        // Get the current pose stack and camera
        PoseStack poseStack = event.getPoseStack();
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        double camX = camera.getPosition().x;
        double camY = camera.getPosition().y;
        double camZ = camera.getPosition().z;

        // Translate to the correct position relative to the camera
        poseStack.pushPose();
        poseStack.translate(-camX, -camY, -camZ);


        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        // Render the box (default line box method)
        for (BlockPos pos : new HashSet<>(blockPosList.keySet())) {
            ArrayList<Float> data = blockPosList.get(pos);
            float alpha = data.get(0);
            float red = data.get(1);
            float green = data.get(2);
            float blue = data.get(3);
            float extraSize = data.get(4);


            AABB box = new AABB(pos).inflate(0.001 + extraSize); // Define the box


            data.set(0, (float) (alpha - 0.003));
            blockPosList.put(pos, data);

            if (alpha <= 0) {
                blockPosList.remove(pos);
            }

            //Render the box
            LevelRenderer.renderLineBox(
                    poseStack,
                    bufferSource.getBuffer(RenderType.LINES),
                    box,
                    red,
                    green,
                    blue,
                    alpha // RGBA: Yellow Box
            );
        }

        //bufferSource.endBatch(RenderType.LINES);

        poseStack.popPose();
    }
}

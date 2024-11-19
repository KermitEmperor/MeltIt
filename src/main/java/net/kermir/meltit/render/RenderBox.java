package net.kermir.meltit.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.kermir.meltit.MeltIt;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = MeltIt.MOD_ID)
public class RenderBox {
    private static HashMap<BlockPos, Float> blockPosList = new HashMap<>();

    public static void addPosition(BlockPos pos) {
        blockPosList.put(pos, 1F);
        MeltIt.LOGGER.debug("{}",pos);
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
            float alpha = blockPosList.get(pos);
            AABB box = new AABB(pos).inflate(0.001); // Define the box

            blockPosList.put(pos, (float) (alpha - 0.003));

            if (alpha <= 0) {
                blockPosList.remove(pos);
            }

            //Render the box
            LevelRenderer.renderLineBox(
                    poseStack,
                    bufferSource.getBuffer(RenderType.LINES),
                    box,
                    1.0f,
                    0.7f,
                    0.0f,
                    alpha // RGBA: Yellow Box
            );
        }

        //bufferSource.endBatch(RenderType.LINES);

        poseStack.popPose();
    }
}

package net.kermir.meltit.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.kermir.meltit.MeltIt;
import net.kermir.meltit.block.multiblock.module.SmelteryFuelTank;
import net.kermir.meltit.block.multiblock.module.entity.SmelteryFuelTankBlockEntity;
import net.kermir.meltit.render.FluidBoxRenderer;
import net.kermir.meltit.render.MeltItRenderTypes;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;

public class FuelTankRenderer implements BlockEntityRenderer<SmelteryFuelTankBlockEntity> {
    /** Distance between the liquid and the edge of the block */
    private static final float FLUID_OFFSET = 0.005f;

    public FuelTankRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SmelteryFuelTankBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        if (pBlockEntity.isEmpty()) return;

        Fluid fluid = pBlockEntity.getFluid();


        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        //TODO Render fuel

        float fullness = (float) pBlockEntity.getFluidStack().getAmount() / pBlockEntity.getMaxCapacity();

        renderFluids(pPoseStack, bufferSource, pBlockEntity.getFluidStack(), pBlockEntity.getBlockPos(), pBlockEntity.getBlockPos(), 1, fullness);

        // Extract color components
    }

    @Override
    public boolean shouldRender(SmelteryFuelTankBlockEntity pBlockEntity, Vec3 pCameraPos) {
        return true;
    }

    public static void renderFluids(PoseStack matrices, MultiBufferSource buffer, FluidStack fluidStack,
                                    BlockPos tankMinPos, BlockPos tankMaxPos, int brightness, float fullness) {
        FluidStack fluid = fluidStack;
        // empty smeltery :(
        if(!fluid.isEmpty()) {
            // determine x and z bounds, constant
            int xd = tankMaxPos.getX() - tankMinPos.getX();
            int zd = tankMaxPos.getZ() - tankMinPos.getZ();
            // somehow people are getting a rendering crash with these being negative, no idea how but easy to catch
            if (xd < 0 || zd < 0) {
                return;
            }
            float[] xBounds = getBlockBounds(xd);
            float[] zBounds = getBlockBounds(zd);

            // rendering time
            VertexConsumer builder = buffer.getBuffer(MeltItRenderTypes.SMELTERY_FLUID);
            float curY = FLUID_OFFSET;

            FluidBoxRenderer.renderLargeFluidBox(matrices, builder, fluid, brightness, xd, xBounds, zd, zBounds, curY, (curY+fullness));
        }
    }

    private static float[] getBlockBounds(int delta) {
        return FluidBoxRenderer.getBlockBounds(delta, FLUID_OFFSET, delta + 1f - FLUID_OFFSET);
    }

}

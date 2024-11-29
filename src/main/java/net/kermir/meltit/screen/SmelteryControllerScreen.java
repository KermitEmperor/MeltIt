package net.kermir.meltit.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.kermir.meltit.MeltIt;
import net.kermir.meltit.block.multiblock.controller.heat.HeatState;
import net.kermir.meltit.networking.PacketChannel;
import net.kermir.meltit.networking.packet.UpdateServerMenuIndiciesPacket;
import net.kermir.meltit.screen.slot.SmelterySlot;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

@SuppressWarnings("unused")
public class SmelteryControllerScreen extends AbstractContainerScreen<SmelteryControllerMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MeltIt.MOD_ID, "textures/gui/smeltery.png");

    public SmelteryControllerScreen(SmelteryControllerMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.size = this.getMenu().getCurrentSize();
        this.isBig = this.size > 24;
        this.scrollbarHeld = false;
        this.scrollbarOffset = 0;
        this.previousIndexOffset = 0;
        this.remainderRows = (Mth.ceil((double) this.size-24)/3);
        this.indexOffset = 0;
    }

    public int size;
    public boolean isBig;
    public boolean scrollbarHeld;
    public int scrollbarOffset;
    public int remainderRows;
    public int previousIndexOffset;
    public int indexOffset;
    private static final int smeltIconImageWidth = 14;
    private static final int smeltIconImageHeight = 14;
    private static final int scrollBarImageWidth = 14;
    private static final int scrollBarImageHeight = 145;
    private static final int scrollButtonImageWidth = 12;
    private static final int scrollButtonImageHeight = 15;
    private static final int smelterySlotImageWidth = 22;
    private static final int smelterySlotImageHeight = 18;
    private static final int blankSlotImageWidth = 22;
    private static final int blankSlotImageHeight = 18;
    private static final int heatStateHeight = 16;
    private static final int heatStateWidth = 3;
    public static final int scrollbarMax = scrollBarImageHeight-scrollButtonImageHeight-3;

    @Override
    protected void renderBg(@NotNull PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1,1,1,1);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        //main gui
        this.blit(pPoseStack, x,y+2,0,0,imageWidth,imageHeight);
        //slider

        //slider background

        if (isBig) {
            this.blit(pPoseStack, x-18, y, 176, 76, scrollBarImageWidth, scrollBarImageHeight);
            //slider button
            if (!scrollbarHeld) {
                this.blit(pPoseStack, x-17, y+1+scrollbarOffset, 152, 166, scrollButtonImageWidth, scrollButtonImageHeight);
            } else {
                this.blit(pPoseStack, x-17, y+1+scrollbarOffset, 164, 166, scrollButtonImageWidth, scrollButtonImageHeight);
            }
        }

        //smelt icon
        if (this.getMenu().blockEntity.isLit())
            this.blit(pPoseStack, x+92, y+3+smeltIconImageHeight,56, 166, smeltIconImageWidth, smeltIconImageHeight );


        //slots
        int currentSlot = 0;
        for (Slot slot_og : this.getMenu().slots) {
            if (currentSlot < 24) {
                if (slot_og instanceof SmelterySlot smelterySlot) {
                    //slot
                    this.blit(pPoseStack, slot_og.x+x-5, slot_og.y+y-1, 0, 166, smelterySlotImageWidth,smelterySlotImageHeight);
                    currentSlot++;

                    //heat

                    if (slot_og.hasItem()) {
                        int xpos = slot_og.x+x-4;
                        int ypos = slot_og.y+y;
                        int UOffset = getuOffset(smelterySlot);

                        drawHeatState(pPoseStack, smelterySlot.getProgress(), xpos, ypos, UOffset, 166, heatStateWidth, heatStateHeight);
                    }
                }
            }
        }

        //fuel
        FluidStack fluidStack = new FluidStack(Fluids.LAVA, 100);
        //36 is the max height of the gauge
        //TODO Amount of Fuel / Max amount of fuel * Height of the gauge
        renderFluid(pPoseStack, fluidStack, x+93, y+54, 12, 36);
    }


    private void renderFluid(PoseStack poseStack, FluidStack fluidStack, int x, int y, int width, int height) {
        Fluid fluid = fluidStack.getFluid();
        Function<Material, TextureAtlasSprite> spriteGetter = ForgeModelBakery.defaultTextureGetter();
        TextureAtlasSprite fluidSprite = fluid != Fluids.EMPTY ? spriteGetter.apply(ForgeHooksClient.getBlockMaterial(fluid.getAttributes().getStillTexture())) : null;

        if (fluidSprite != null) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, fluidSprite.atlas().location());
            RenderSystem.enableBlend();

            // Draw fluid texture
            //innerBlit(pPoseStack.last().pose(), pX, pX + pWidth, pY, pY + pHeight, pBlitOffset, pSprite.getU0(), pSprite.getU1(), pSprite.getV0(), pSprite.getV1());
            //blit(poseStack, x, y, 0, fluidSprite.getWidth(), fluidSprite.getHeight(), fluidSprite);
            //blit(poseStack, x, y, 0, fluidSprite.getU0(), fluidSprite.getU1(), (int)fluidSprite.getV0(), (int)fluidSprite.getV1(), fluidSprite.getHeight(),fluidSprite.getWidth());

            int textureWidth = fluidSprite.getWidth();   // Width of the texture in the atlas
            int textureHeight = fluidSprite.getHeight(); // Height of the texture in the atlas

            float u0 = fluidSprite.getU0();
            float v0 = fluidSprite.getV0();
            float uWidth = fluidSprite.getU1() - fluidSprite.getU0();
            float vHeight = fluidSprite.getV1() - fluidSprite.getV0();

            for (int i = 0; i < width; i += textureWidth) {
                for (int j = 0; j < height; j += textureHeight) {
                    int drawWidth = Math.min(textureWidth, width - i);    // Crop if overflow
                    int drawHeight = Math.min(textureHeight, height - j); // Crop if overflow

                    float u1 = u0 + (drawWidth / (float) textureWidth) * uWidth;
                    float v1 = v0 + (drawHeight / (float) textureHeight) * vHeight;

                    int finalY = y - j;

                    if (textureHeight != drawHeight) {
                        finalY += (textureWidth - drawHeight);
                    }

                    // Render a single tile or cropped part
                    //we switch up v0 and v1 to v1 and v0 so it will draw the fluid from the bottom to up
                    myBlit(poseStack, x + i, finalY, 0, drawWidth, drawHeight, u0, u1, v1, v0);
                }
            }



            RenderSystem.disableBlend();
        }
    }

    public static void myBlit(PoseStack pPoseStack, int pX, int pY, int pBlitOffset, int pWidth, int pHeight, float U0, float U1, float V0, float V1) {
        innerBlit(pPoseStack.last().pose(), pX, pX + pWidth, pY, pY + pHeight, pBlitOffset, U0, U1, V0, V1);
    }


    //cuz fuck you for making it private, now i had to copy it >:(
    private static void innerBlit(Matrix4f pMatrix, int pX1, int pX2, int pY1, int pY2, int pBlitOffset, float pMinU, float pMaxU, float pMinV, float pMaxV) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(pMatrix, (float)pX1, (float)pY2, (float)pBlitOffset).uv(pMinU, pMaxV).endVertex();
        bufferbuilder.vertex(pMatrix, (float)pX2, (float)pY2, (float)pBlitOffset).uv(pMaxU, pMaxV).endVertex();
        bufferbuilder.vertex(pMatrix, (float)pX2, (float)pY1, (float)pBlitOffset).uv(pMaxU, pMinV).endVertex();
        bufferbuilder.vertex(pMatrix, (float)pX1, (float)pY1, (float)pBlitOffset).uv(pMinU, pMinV).endVertex();
        bufferbuilder.end();
        BufferUploader.end(bufferbuilder);
    }

    private static int getuOffset(SmelterySlot smelterySlot) {
        int UOffset = 44;
        HeatState state = smelterySlot.getHeatState();
        //pPoseStack.mulPose(Vector3f.ZN.rotationDegrees(180));
        //blit(pPoseStack, slot_og.x+x-4, slot_og.y+y, 44, 166-16, heatStateWidth,-16);

        if (state.equals(HeatState.TOO_COLD)) UOffset += 3;
        if (state.equals(HeatState.NO_SPACE)) UOffset += 6;
        if (state.equals(HeatState.UNMELTABLE)) UOffset += 9;
        return UOffset;
    }

    private void drawHeatState(PoseStack poseStack, float progress ,int x,int y,int UOffset, int VOffset, int UWidth, int VHeight) {
        int height;
        if (progress > 1) {
            height = VHeight;
        } else if (progress < 0) {
            height = 0;
        } else {
            // add an extra 0.5 so it rounds instead of flooring
            height = (int)(progress * VHeight + 0.5);
        }
        // amount to offset element by for the height
        int deltaY = VHeight - height;
        blit(poseStack, x, y + deltaY, UOffset, VOffset + deltaY, UWidth, height, 256, 256);
    }



    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        this.scrollbarHeld = false;
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        if (isBig) {
            if ((pMouseX >= x-18 && pMouseX <= x-18+scrollBarImageWidth) && (pMouseY >= y+scrollbarOffset && pMouseY <= y+scrollButtonImageHeight+scrollbarOffset)) this.scrollbarHeld = true;
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        this.scrollbarHeld = false;
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (isBig) {
            int y = (height - imageHeight) / 2;
            if (indexOffset != previousIndexOffset) {
                //WARNING if something breaks while using the scrollbar, it probably broke here
                this.menu.reAddingSlots(this.menu.getPlayerInventory(),this.indexOffset*3);
                // HEeeeyyy did you know that Menu exists on server side too?
                // Yeah I too found it out after...
                // 3 GODDAMN HOURS OF DEBUG HELL
                PacketChannel.sendToServer(new UpdateServerMenuIndiciesPacket(this.indexOffset*3));
            }

            this.previousIndexOffset = this.indexOffset;
            if (scrollbarHeld) {
                this.scrollbarOffset = Mth.clamp(Mth.ceil((pMouseY-y)-((double) scrollButtonImageHeight /2)), 0, scrollbarMax);
                this.indexOffset = Math.round((float) scrollbarOffset/((float) scrollbarMax /remainderRows));
            }
        }
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public void render(@NotNull PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pPoseStack);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        renderTooltip(pPoseStack, pMouseX, pMouseY);
    }
}

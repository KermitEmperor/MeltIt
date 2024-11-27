package net.kermir.meltit.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.kermir.meltit.MeltIt;
import net.kermir.meltit.networking.PacketChannel;
import net.kermir.meltit.networking.packet.UpdateServerMenuIndiciesPacket;
import net.kermir.meltit.screen.slot.SmelterySlot;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

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
                        int UOffset = 44;
                        //pPoseStack.mulPose(Vector3f.ZN.rotationDegrees(180));
                        //blit(pPoseStack, slot_og.x+x-4, slot_og.y+y, 44, 166-16, heatStateWidth,-16);

                        drawHeatState(pPoseStack, smelterySlot.getProgress(), xpos, ypos, UOffset, 166, heatStateWidth, heatStateHeight);
                    }
                }
            }
        }
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

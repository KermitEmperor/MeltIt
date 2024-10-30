package net.kermir.meltit.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.kermir.meltit.MeltIt;
import net.kermir.meltit.screen.slot.SmelterySlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.SliderButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class SmelteryControllerScreen extends AbstractContainerScreen<SmelteryControllerMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MeltIt.MOD_ID, "textures/gui/smeltery.png");

    public SmelteryControllerScreen(SmelteryControllerMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1,1,1,1);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        this.blit(pPoseStack, x,y+2,0,0,imageWidth,imageHeight);

        boolean isBig = this.getMenu().getCurrentSize() > 24;


        /*
        if (isBig) {
            this.addRenderableWidget();
        }*/

        int currentSlot = 0;
        for (Slot slot_og : this.getMenu().slots) {
            if (currentSlot < 24) {
                if (slot_og instanceof SmelterySlot) {
                    this.blit(pPoseStack, slot_og.x+x-5, slot_og.y+y-1, 0, 166, 22,18);
                    currentSlot++;
                }
            }
        }


        /*
        for (int i = 1; i <= this.menu.getCurrentSize(); i++) {
            this.blit(pPoseStack, x-22,y+slot_y,0,166,22,18);
            slot_y+=18;
        }*/
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pPoseStack);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        renderTooltip(pPoseStack, pMouseX, pMouseY);
    }
}

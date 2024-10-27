package net.kermir.meltit.screen;

import net.kermir.meltit.MeltIt;
import net.kermir.meltit.block.BlockRegistry;
import net.kermir.meltit.block.multiblock.SmelteryControllerBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraft.world.inventory.Slot;

import java.util.concurrent.atomic.AtomicInteger;

public class SmelteryControllerMenu extends AbstractContainerMenu {
    private final SmelteryControllerBlockEntity blockEntity;
    private final Level level;
    private final Player player;

    public SmelteryControllerMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, inv.player.level.getBlockEntity(extraData.readBlockPos()));
    }

    public SmelteryControllerMenu(int pContainerId, Inventory inv, BlockEntity entity) {
        super(MenuTypeRegistries.SMELTERY_CONTROLLER_MENU.get(), pContainerId);
        checkContainerSize(inv, 4);
        blockEntity = (SmelteryControllerBlockEntity) entity;
        this.player = inv.player;
        this.level = player.level;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(handler -> {
            for (int i = 0; i < handler.getSlots() ;i++) {
                //this pain runs on both client and server and having a mismatch will cause headaches
                //This can get desynced if handler size changes
                this.addSlot(new SlotItemHandler(handler, i, -17, i*18+1));
                MeltIt.LOGGER.debug("{} and {}", i, level.isClientSide());
            }
        });
    }

    public int getCurrentSize() {
        AtomicInteger slotCount = new AtomicInteger();
        this.blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(iItemHandler -> {
            slotCount.set(iItemHandler.getSlots());
        });
        return slotCount.get();
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                pPlayer, BlockRegistry.SMELTERY_CONTROLLER.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 86 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 144));
        }
    }
}

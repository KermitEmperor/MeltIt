package net.kermir.meltit.screen;

import net.kermir.meltit.MeltIt;
import net.kermir.meltit.block.BlockRegistry;
import net.kermir.meltit.block.multiblock.ModifiedItemStackHandler;
import net.kermir.meltit.block.multiblock.SmelteryControllerBlockEntity;
import net.kermir.meltit.screen.slot.SmelterySlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SmelteryControllerMenu extends AbstractContainerMenu {
    public final SmelteryControllerBlockEntity blockEntity;
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
            ModifiedItemStackHandler stackHandler = (ModifiedItemStackHandler) handler;
            boolean isBig = handler.getSlots() > 24;
            int displaceXAmount = isBig ? -24 : 0;
            int row = -1;
            for (int i = 0; i < stackHandler.getSlots() ;i++) {
                //this pain runs on both client and server and having a mismatch will cause headaches
                //This can get desynced if handler size changes
                if (stackHandler.getSlots() > 8) {
                    int modul = i%3;
                    int posmodul = 2 - modul;
                    if (modul == 0) row++;
                    if (i < 24) {
                        this.addSlot(new SmelterySlot(handler, i, -17-(posmodul*22)+displaceXAmount, row*18+1));
                    }
                } else {
                    this.addSlot(new SmelterySlot(handler, i, -17, i*18+1));
                }
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

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        Slot sourceSlot = slots.get(pIndex);
        if (!sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // BE = BlockEntity
        int firstBEslotIndex = 0;
        for (Slot slot : slots) {
            if (slot instanceof SmelterySlot) {
                firstBEslotIndex = slots.indexOf(slot);
                break;
            }
        }

        if (!(sourceSlot instanceof SmelterySlot)) {
            //if (!moveItemStackTo(sourceStack, firstBEslotIndex, firstBEslotIndex+(getCurrentSize()), false)) {
            if (!moveItemStackToBlockEntity(sourceStack)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!moveItemStackToInventory(sourceStack, 0, firstBEslotIndex-1)) {
                return ItemStack.EMPTY;
            }
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        sourceSlot.onTake(pPlayer, sourceStack);
        return copyOfSourceStack;
    }

    protected boolean moveItemStackToBlockEntity(ItemStack stack) {
        AtomicBoolean ret = new AtomicBoolean(false);
        ItemStack singularStack = stack.copy();
        singularStack.setCount(1);

        if (stack.isEmpty()) return false;

        if (stack.isStackable()) {
            this.blockEntity.useItemHandler(handler -> {
                for (int slot_index = 0; slot_index < handler.getSlots(); slot_index++ ) {
                    if (stack.isEmpty()) break;
                    if (handler.getStackInSlot(slot_index).isEmpty()) {
                        blockEntity.safeInsert(slot_index, stack, 1);
                        ret.set(true);
                    }
                }
            });
        }

        return ret.get();
    }
    protected boolean moveItemStackToInventory(ItemStack stack,int startIndex, int endIndex) {
        AtomicBoolean ret = new AtomicBoolean(false);

        MeltIt.LOGGER.debug("I ran1");
        if (stack.isEmpty()) return false;

        MeltIt.LOGGER.debug("I ran2");
        for (int slot_index = startIndex; slot_index <= endIndex; slot_index++) {
            Slot slot = slots.get(slot_index);
            ItemStack slotItem = slot.getItem();
            if (!slotItem.isEmpty() && slotItem.getItem() == stack.getItem() && ItemStack.tagMatches(stack, slotItem) && this.canTakeItemForPickAll(stack, slot)) {
                int l = slotItem.getCount() + stack.getCount();
                int limit = Math.min(stack.getMaxStackSize(), slot.getMaxStackSize(stack));

                if (l <= limit) {
                    stack.setCount(0);
                    slotItem.setCount(l);
                    slot.setChanged();
                    ret.set(true);
                } else if (slotItem.getCount() < limit) {
                    stack.shrink(limit - slotItem.getCount());
                    slotItem.setCount(limit);
                    slot.setChanged();
                    ret.set(true);
                }
            } else if (slotItem.isEmpty()) {
                slot.safeInsert(stack);
                ret.set(true);
            }
        }

        return ret.get();
    }

    @Override
    public void clicked(int pSlotId, int pButton, ClickType pClickType, Player pPlayer) {

        ItemStack mouseItemStack = this.getCarried();
        if (pClickType == ClickType.PICKUP_ALL) {
            this.blockEntity.useItemHandler(handler -> {

                for (int slotIndex = 0; slotIndex < handler.getSlots(); slotIndex++) {
                    if (mouseItemStack.getMaxStackSize() == mouseItemStack.getCount()) break;

                    if (handler.getStackInSlot(slotIndex).is(mouseItemStack.getItem())) {
                        mouseItemStack.grow(1);
                        this.blockEntity.safeTake(slotIndex, 1, 1, pPlayer);
                    }
                }
            });
        }

        super.clicked(pSlotId, pButton, pClickType, pPlayer);
    }

    @Override
    protected boolean moveItemStackTo(ItemStack pStack, int pStartIndex, int pEndIndex, boolean pReverseDirection) {
        return false;
    }
}

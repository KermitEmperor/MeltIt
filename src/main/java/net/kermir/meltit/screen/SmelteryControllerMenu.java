package net.kermir.meltit.screen;

import net.kermir.meltit.MeltIt;
import net.kermir.meltit.block.BlockRegistry;
import net.kermir.meltit.block.multiblock.ModifiedItemStackHandler;
import net.kermir.meltit.block.multiblock.SmelteryControllerBlockEntity;
import net.kermir.meltit.screen.slot.SmelterySlot;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraft.world.inventory.Slot;

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
                    if (i < 24)
                        this.addSlot(new SmelterySlot(handler, i, -17-(posmodul*22)+displaceXAmount, row*18+1));
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
            if (!moveItemStackTo(sourceStack, firstBEslotIndex, firstBEslotIndex+(getCurrentSize()), false)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!moveItemStackTo(sourceStack, 0, firstBEslotIndex-1, false)) {
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

    //Credit to Mantle devs: https://github.com/SlimeKnights/Mantle/blob/1.18.2/src/main/java/slimeknights/mantle/inventory/BaseContainerMenu.java
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean useEndIndex) {
        boolean ret = this.mergeItemStackRefill(stack, startIndex, endIndex, useEndIndex);
        if (!stack.isEmpty() && stack.getCount() > 0) {
            ret |= this.mergeItemStackMove(stack, startIndex, endIndex, useEndIndex);
        }
        return ret;
    }

    protected boolean mergeItemStackRefill(ItemStack stack, int startIndex, int endIndex, boolean useEndIndex) {
        if (stack.getCount() <= 0) {
            return false;
        }

        boolean flag1 = false;
        int k = startIndex;

        if (useEndIndex) {
            k = endIndex - 1;
        }

        Slot slot;
        ItemStack itemstack1;

        if (stack.isStackable()) {
            while (stack.getCount() > 0 && (!useEndIndex && k < endIndex || useEndIndex && k >= startIndex)) {
                slot = this.slots.get(k);
                itemstack1 = slot.getItem();

                if (!itemstack1.isEmpty() && itemstack1.getItem() == stack.getItem() && ItemStack.tagMatches(stack, itemstack1) && this.canTakeItemForPickAll(stack, slot)) {
                    int l = itemstack1.getCount() + stack.getCount();
                    int limit = Math.min(stack.getMaxStackSize(), slot.getMaxStackSize(stack));

                    if (l <= limit) {
                        stack.setCount(0);
                        itemstack1.setCount(l);
                        slot.setChanged();
                        flag1 = true;
                    } else if (itemstack1.getCount() < limit) {
                        stack.shrink(limit - itemstack1.getCount());
                        itemstack1.setCount(limit);
                        slot.setChanged();
                        flag1 = true;
                    }
                }

                if (useEndIndex) {
                    --k;
                } else {
                    ++k;
                }
            }
        }

        return flag1;
    }

    protected boolean mergeItemStackMove(ItemStack stack, int startIndex, int endIndex, boolean useEndIndex) {
        if (stack.getCount() <= 0) {
            return false;
        }

        boolean flag1 = false;
        int k;

        if (useEndIndex) {
            k = endIndex - 1;
        } else {
            k = startIndex;
        }

        while (!useEndIndex && k < endIndex || useEndIndex && k >= startIndex) {
            Slot slot = this.slots.get(k);
            ItemStack itemstack1 = slot.getItem();

            // Forge: Make sure to respect isItemValid in the slot.
            if (itemstack1.isEmpty() && slot.mayPlace(stack) && this.canTakeItemForPickAll(stack, slot)) {
                int limit = slot.getMaxStackSize(stack);
                ItemStack stack2 = stack.copy();

                if (stack2.getCount() > limit) {
                    stack2.setCount(limit);
                    stack.shrink(limit);
                } else {
                    stack.setCount(0);
                }

                slot.set(stack2);
                slot.setChanged();
                flag1 = true;

                if (stack.isEmpty()) {
                    break;
                }
            }

            if (useEndIndex) {
                --k;
            } else {
                ++k;
            }
        }

        return flag1;
    }
}

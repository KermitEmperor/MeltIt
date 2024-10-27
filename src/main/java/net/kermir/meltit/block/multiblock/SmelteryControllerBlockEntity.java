package net.kermir.meltit.block.multiblock;

import net.kermir.meltit.block.BlockEntityRegistry;
import net.kermir.meltit.screen.SmelteryControllerMenu;
import net.kermir.meltit.screen.SmelteryControllerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.EventBus;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class SmelteryControllerBlockEntity extends BlockEntity implements MenuProvider, MultiblockMaster {
    private final ModifiedItemStackHandler itemHandler = new ModifiedItemStackHandler(5, worldPosition) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        protected int getStackLimit(int slot, @NotNull ItemStack stack) {
            return 1;
        }

        @Override
        public void setSize(int size) {
            //pars source entity here?
            if (size>stacks.size()) {
                List<ItemStack> combined = new ArrayList<>();
                List<ItemStack> previous = stacks;
                List<ItemStack> additional = NonNullList.withSize(size-stacks.size(),ItemStack.EMPTY);
                combined.addAll(previous);
                combined.addAll(additional);

                //what
                stacks = NonNullList.of(ItemStack.EMPTY, combined.toArray(new ItemStack[0]));
            } else if (stacks.size()==size) {
                return;
            } else if (stacks.size()>size) {
                int differance = stacks.size()-size;
                List<ItemStack> remainderItems = stacks.subList(stacks.size()-differance,stacks.size());
                NonNullList<ItemStack> excess = NonNullList.create();
                excess.addAll(remainderItems);
                dropExcess(excess);

                stacks = NonNullList.of(ItemStack.EMPTY, stacks.subList(0, stacks.size()-differance).toArray(new ItemStack[0]));

            } else {
                super.setSize(size);
            }
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            if (slot >= this.stacks.size()) {
                kickViewer();
                return ItemStack.EMPTY;
            }
            return super.getStackInSlot(slot);
        }
    };


    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public SmelteryControllerBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityRegistry.SMELTERY_CONTROLLER_BLOCK_ENTITY.get(), pPos, pBlockState);
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, SmelteryControllerBlockEntity pBlockEntity) {
        BlockState blockStateBelow = pLevel.getBlockState(pPos.below());
        if (blockStateBelow.equals(Blocks.REDSTONE_BLOCK.defaultBlockState())) {
            pBlockEntity.itemHandler.setSize(8);
            pLevel.removeBlock(pPos.below(), false);
        }
        if (blockStateBelow.equals(Blocks.COAL_BLOCK.defaultBlockState())) {
            pBlockEntity.itemHandler.setSize(1);
            pLevel.removeBlock(pPos.below(), false);
        }
        if (blockStateBelow.equals(Blocks.GOLD_BLOCK.defaultBlockState())) {
            pBlockEntity.itemHandler.setSize(pBlockEntity.itemHandler.getSlots()+5);
            pLevel.removeBlock(pPos.below(), false);
        }
    }

    @Override
    public @NotNull Component getDisplayName() {
        return new TextComponent("Smeltery Controller");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new SmelteryControllerMenu(pContainerId, pPlayerInventory, this);
    }


    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return lazyItemHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps()  {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        tag.put("inventory", itemHandler.serializeNBT());
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public void dropExcess(NonNullList<ItemStack> items) {
        Containers.dropContents(this.level, this.worldPosition.above(), items);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag nbt = super.getUpdateTag();
        saveAdditional(nbt);
        return nbt;
    }

    public void kickViewer() {
    }
}

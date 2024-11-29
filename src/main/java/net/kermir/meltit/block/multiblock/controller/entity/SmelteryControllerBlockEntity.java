package net.kermir.meltit.block.multiblock.controller.entity;

import net.kermir.meltit.MeltIt;
import net.kermir.meltit.block.entity.BlockEntityRegistry;
import net.kermir.meltit.block.multiblock.IMaster;
import net.kermir.meltit.block.multiblock.SmelteryMultiblock;
import net.kermir.meltit.block.multiblock.controller.heat.HeatHandler;
import net.kermir.meltit.block.multiblock.controller.heat.HeatState;
import net.kermir.meltit.item.util.ResizeableItemStackHandler;
import net.kermir.meltit.networking.PacketChannel;
import net.kermir.meltit.networking.packet.CloseSmelteryScreenPacket;
import net.kermir.meltit.networking.packet.UpdateControllerSizePacket;
import net.kermir.meltit.recipe.types.SmelteryMeltingRecipe;
import net.kermir.meltit.screen.SmelteryControllerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class SmelteryControllerBlockEntity extends BlockEntity implements MenuProvider, IMaster {
    public final HeatHandler heatHandler = new HeatHandler(0);
    public final ResizeableItemStackHandler itemHandler = new ResizeableItemStackHandler(0) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        protected int getStackLimit(int slot, @NotNull ItemStack stack) {
            return 1;
        }

        @Override
        public void onSizeChanged(int size) {
            heatHandler.setSize(size);
            if (level != null && !level.isClientSide()) {
                PacketChannel.sendToAllClients(new CloseSmelteryScreenPacket(worldPosition));
            }
        }

        @Override
        public void onSizeGotSmaller(int size, NonNullList<ItemStack> excessItems) {
            dropExcess(excessItems);
            MeltIt.LOGGER.debug("Excess Items dropped");
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private LazyOptional<HeatHandler> lazyHeatHandler = LazyOptional.empty();

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    private final SmelteryMultiblock multiblock;

    public SmelteryControllerBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityRegistry.SMELTERY_CONTROLLER_BLOCK_ENTITY.get(), pPos, pBlockState);
        this.multiblock = new SmelteryMultiblock(this, pPos, pBlockState);
    }

    @SuppressWarnings("unused")
    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, SmelteryControllerBlockEntity pBlockEntity) {
        AtomicBoolean shouldLit = new AtomicBoolean(false);

        pBlockEntity.useItemHandler(hndlr -> {
            ResizeableItemStackHandler handler = (ResizeableItemStackHandler) hndlr;
            HeatHandler heats = pBlockEntity.heatHandler;


            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);

                if (stack.isEmpty()) {
                    heats.setPairData(i, HeatState.UNMELTABLE, 0F);
                } else {
                    Optional<SmelteryMeltingRecipe> recipeOptional = pLevel.getRecipeManager()
                            .getRecipeFor(SmelteryMeltingRecipe.Type.INSTANCE, new SimpleContainer(stack), pLevel);

                    int finalI = i;

                    recipeOptional.ifPresentOrElse(recipe -> {
                        if (heats.getHeatState(finalI) == HeatState.UNMELTABLE) {
                            heats.setPairData(finalI, HeatState.MELTING, 0F);
                        }
                        if (heats.getProgress(finalI) == 1F) {
                            stack.shrink(1);
                            heats.setPairData(finalI, HeatState.UNMELTABLE,1F);
                        } else {
                            shouldLit.set(true);
                            heats.incrementProgress(finalI,1F / recipe.getSmeltTime());
                        }
                    }, () -> {
                        heats.setPairData(finalI, HeatState.UNMELTABLE,1F);
                    });
                }
            }
        });

        // 1 + 2 + 16
        pLevel.setBlock(pPos, pState.setValue(LIT, shouldLit.get()), 18);
    }

    public boolean isLit() {
        return this.getBlockState().getValue(LIT);
    }

    //etc

    @SuppressWarnings("UnusedReturnValue")
    public ItemStack safeInsert(int slotIndex, ItemStack pStack, int pIncrement) {
        if (!pStack.isEmpty()) {
            ItemStack itemstack = this.itemHandler.getStackInSlot(slotIndex);
            int i = Math.min(Math.min(pIncrement, pStack.getCount()), this.itemHandler.getSlotLimit(slotIndex) - itemstack.getCount());
            if (itemstack.isEmpty()) {
                this.itemHandler.insertItem(slotIndex, pStack.split(i), false);
            } else if (ItemStack.isSameItemSameTags(itemstack, pStack)) {
                pStack.shrink(i);
                itemstack.grow(i);
                this.itemHandler.insertItem(slotIndex, itemstack, false);
            }

            setChanged();
            return pStack;
        } else {
            setChanged();
            return pStack;
        }
    }

    public Optional<ItemStack> tryRemove(int slotIndex,int pCount, int pDecrement, Player pPlayer) {
        if (pDecrement < this.itemHandler.getStackInSlot(slotIndex).getCount()) {
            return Optional.empty();
        } else {
            pCount = Math.min(pCount, pDecrement);
            //ItemStack itemstack = this.remove(pCount);
            ItemStack itemstack = this.itemHandler.getStackInSlot(slotIndex).split(pCount);
            if (itemstack.isEmpty()) {
                return Optional.empty();
            } else {
                if (this.itemHandler.getStackInSlot(slotIndex).isEmpty()) {
                    this.itemHandler.setStackInSlot(slotIndex, ItemStack.EMPTY);
                }

                return Optional.of(itemstack);
            }
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public ItemStack safeTake(int slotIndex , int pCount, int pDecrement, Player pPlayer) {
        Optional<ItemStack> optional = this.tryRemove(slotIndex,pCount, pDecrement, pPlayer);
        optional.ifPresent((p_150655_) -> setChanged());
        return optional.orElse(ItemStack.EMPTY);
    }

    public void useItemHandler(Consumer<IItemHandler> method) {
        this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(method::accept);
    }

    //Base needs

    public void setSize(int size) {
        this.itemHandler.setSize(size);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return new TextComponent("Smeltery Controller");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pPlayerInventory, @NotNull Player pPlayer) {
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
        lazyHeatHandler = LazyOptional.of(() -> heatHandler);
    }

    @Override
    public void invalidateCaps()  {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
        lazyHeatHandler.invalidate();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        tag.put("inventory", itemHandler.serializeNBT());
        tag.put("heatstates", heatHandler.serializeNBT());
        super.saveAdditional(tag);
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        heatHandler.deserializeNBT(nbt.getCompound("heatstates"));
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        if (this.level != null)
            Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public void dropExcess(NonNullList<ItemStack> items) {
        if (this.level == null) return;
        Containers.dropContents(this.level, this.worldPosition.relative(this.getBlockState().getValue(FACING), 1), items);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag nbt = super.getUpdateTag();
        saveAdditional(nbt);
        return nbt;
    }

    public void destroyMultiblock(Level level) {
        multiblock.deassignMasters(level);
    }

    @Override
    public void notifyChange(BlockPos pos, BlockState state) {
        if (level == null) return;

        resizeByStructure(level);
    }

    public void resizeByStructure(Level level) {
        int nextSize = Mth.abs(multiblock.InteriorSize(level));
        this.itemHandler.setSize(nextSize);
        MeltIt.LOGGER.debug("VA");
        if (!level.isClientSide())
            PacketChannel.sendToAllClients(new UpdateControllerSizePacket(this.worldPosition, nextSize));
    }

    public boolean structureCheck() {
        if (level == null) return false;
        resizeByStructure(level);
        return multiblock.structureCheck(level);
    }
}

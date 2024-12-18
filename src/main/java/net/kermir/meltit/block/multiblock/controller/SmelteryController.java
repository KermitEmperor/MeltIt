package net.kermir.meltit.block.multiblock.controller;

import net.kermir.meltit.block.entity.BlockEntityRegistry;
import net.kermir.meltit.block.multiblock.controller.entity.SmelteryControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SmelteryController extends AbstractFurnaceBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public SmelteryController(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, Boolean.FALSE));
    }

    @Override
    public void onRemove(BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof SmelteryControllerBlockEntity) {
                ((SmelteryControllerBlockEntity) blockEntity).drops();
                ((SmelteryControllerBlockEntity) blockEntity).destroyMultiblock(pLevel);
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState pState, Level pLevel, @NotNull BlockPos pPos,
                                          @NotNull Player pPlayer, @NotNull InteractionHand pHand, @NotNull BlockHitResult pHit) {
        if (!pLevel.isClientSide()) {
            BlockEntity entity = pLevel.getBlockEntity(pPos);
            if(entity instanceof SmelteryControllerBlockEntity) {
                if (pPlayer.getItemInHand(pHand).is(Items.STICK)) {

                    if (((SmelteryControllerBlockEntity)entity).structureCheck())
                        pPlayer.displayClientMessage(new TextComponent("Succeeded"), true);
                    else
                        pPlayer.displayClientMessage(new TextComponent("Nope"), true);
                } else {
                    if (((SmelteryControllerBlockEntity)entity).structureCheck())
                        NetworkHooks.openGui(((ServerPlayer)pPlayer), (SmelteryControllerBlockEntity)entity, pPos);
                }
            } else {
                //Communism
                throw new IllegalStateException("Our Container provider is missing!");
            }
        }

        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }

    @Override
    protected void openContainer(@NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull Player pPlayer) {

    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return new SmelteryControllerBlockEntity(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, BlockEntityRegistry.SMELTERY_CONTROLLER_BLOCK_ENTITY.get(),
                SmelteryControllerBlockEntity::tick);
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        ((SmelteryControllerBlockEntity) Objects.requireNonNull(pLevel.getBlockEntity(pPos))).notifyChange(pPos, pState);

        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
    }
}

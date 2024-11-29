package net.kermir.meltit.block.multiblock.module;

import net.kermir.meltit.block.multiblock.controller.entity.SmelteryControllerBlockEntity;
import net.kermir.meltit.block.multiblock.module.entity.SmelteryBlockEntity;
import net.kermir.meltit.block.multiblock.module.entity.SmelteryFuelTankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class SmelteryFuelTank extends SmelteryTransparentWall {
    public SmelteryFuelTank(Properties pProperties, boolean requiresBlockEntity) {
        super(pProperties, requiresBlockEntity);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide()) {
            BlockEntity entity = pLevel.getBlockEntity(pPos);
            if(entity instanceof SmelteryFuelTankBlockEntity tankBlockEntity) {
                tankBlockEntity.fill(1000);
            } else {
                //Communism
                throw new IllegalStateException("Tank is not SmelteryFuelTankBlockEntity");
            }
        }

        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        if (requiresBlockEntity || pState.getValue(IN_MULTIBLOCK)) {
            return new SmelteryFuelTankBlockEntity(pPos, pState);
        }
        return null;
    }
}

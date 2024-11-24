package net.kermir.meltit.block.multiblock.module;

import net.kermir.meltit.MeltIt;
import net.kermir.meltit.block.multiblock.module.entity.SmelteryModuleBlockEntity;
import net.kermir.meltit.util.BlockEntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import org.jetbrains.annotations.Nullable;

public class SmelteryModuleBlock extends Block implements EntityBlock {
    public static final BooleanProperty IN_MULTIBLOCK = BooleanProperty.create("in_multiblock");

    protected final boolean requiresBlockEntity;

    public SmelteryModuleBlock(Properties pProperties, boolean requiresBlockEntity) {
        super(pProperties);
        this.requiresBlockEntity = requiresBlockEntity;
        this.registerDefaultState(this.defaultBlockState().setValue(IN_MULTIBLOCK, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(IN_MULTIBLOCK);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        if (requiresBlockEntity || pState.getValue(IN_MULTIBLOCK)) {
            return new SmelteryModuleBlockEntity(pPos, pState);
        }
        return null;
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (requiresBlockEntity || pState.getValue(IN_MULTIBLOCK)) {
            if (pNewState.is(this)) {
                if (!requiresBlockEntity && !pNewState.getValue(IN_MULTIBLOCK)) {
                    pLevel.removeBlockEntity(pPos);
                }
            } else {
                // block changed, tell the master then ditch the block entity
                BlockEntityHelper.get(SmelteryModuleBlockEntity.class, pLevel, pPos).ifPresent(te -> te.notifyMasterOfChange(pPos, pNewState, true));
            }
        }
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        SmelteryModuleBlockEntity.updateCloseBlocks(pLevel, pPos, pState);
    }

    @Override
    public boolean triggerEvent(BlockState pState, Level pLevel, BlockPos pPos, int pId, int pParam) {
        super.triggerEvent(pState, pLevel, pPos, pId, pParam);
        BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
        return blockEntity != null && blockEntity.triggerEvent(pId, pParam);
    }

    @Nullable
    @Override
    public BlockPathTypes getAiPathNodeType(BlockState state, BlockGetter level, BlockPos pos, @Nullable Mob entity) {
        return state.getValue(IN_MULTIBLOCK) ? BlockPathTypes.DAMAGE_FIRE : BlockPathTypes.OPEN;
    }
}

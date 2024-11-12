package net.kermir.meltit.block.multiblock.module.entity;

import net.kermir.meltit.MeltIt;
import net.kermir.meltit.block.multiblock.IMaster;
import net.kermir.meltit.block.multiblock.IServant;
import net.kermir.meltit.util.BlockEntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;


//Credits to TConstruct and Mantle devs
public class ServantEntity extends BlockEntity implements IServant {
    private static final String MASTER_POS = "masterPos";
    private static final String MASTER_BLOCK = "masterBlock";
    @Nullable
    private BlockPos masterPos;
    @Nullable
    private Block masterBlock;

    public ServantEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public boolean hasMaster() {
        return masterPos != null;
    }

    protected void setMaster(@Nullable BlockPos master, @Nullable Block block) {
        masterPos = master;
        masterBlock = block;
        this.setChangedFast();
    }

    @SuppressWarnings("deprecation")
    public void setChangedFast() {
        if (level != null) {
            if (level.hasChunkAt(worldPosition)) {
                level.getChunkAt(worldPosition).setUnsaved(true);
            }
        }
    }

    protected boolean validateMaster() {
        if (masterPos == null) {
            return false;
        }

        if (level != null) {
            if (level.getBlockState(masterPos).getBlock() == masterBlock) {
                return true;
            }
        }

        setMaster(null,null);
        return false;
    }

    @Override
    public boolean isValidMaster(IMaster master) {
        if (validateMaster()) {
            return master.getMasterPos().equals(this.masterPos);
        }

        return true;
    }

    @Override
    public BlockPos getMasterPos() {
        return masterPos;
    }

    @Override
    public void notifyMasterOfChange(BlockPos pos, BlockState state) {
        if (validateMaster()) {
            if (masterPos == null) return;
            BlockEntityHelper.get(IMaster.class, level, masterPos).ifPresent(blockEntity -> blockEntity.notifyChange(pos, state));
        }
    }


    @Override
    public void setPossibleMaster(IMaster master) {
        BlockPos newMaster = master.getMasterPos();
        MeltIt.LOGGER.info("master setPossible called");
        if (newMaster.equals(this.masterPos)) {
            masterBlock = master.getMasterBlock().getBlock();
            this.setChangedFast();
        } else if (!validateMaster()) {
            MeltIt.LOGGER.info("new master set");
            setMaster(newMaster, master.getMasterBlock().getBlock());
        }

    }

    @Override
    public void removeMaster(IMaster master) {
        if (masterPos != null && masterPos.equals(master.getMasterPos())) {
            setMaster(null,null);
        }
    }

    protected void readMaster(CompoundTag tags) {
        BlockPos masterPos = readOptionalPos(tags, MASTER_POS, this.worldPosition);
        Block masterBlock = null;
        // if the master position is valid, get the master block
        if (masterPos != null && tags.contains(MASTER_BLOCK, Tag.TAG_STRING)) {
            ResourceLocation masterBlockName = ResourceLocation.tryParse(tags.getString(MASTER_BLOCK));
            if (masterBlockName != null && ForgeRegistries.BLOCKS.containsKey(masterBlockName)) {
                masterBlock = ForgeRegistries.BLOCKS.getValue(masterBlockName);
            }
        }
        // if both valid, set
        if (masterBlock != null) {
            this.masterPos = masterPos;
            this.masterBlock = masterBlock;
        }
    }

    protected CompoundTag writeMaster(CompoundTag tags) {
        if (masterPos != null && masterBlock != null) {
            tags.put(MASTER_POS, NbtUtils.writeBlockPos(masterPos.subtract(this.worldPosition)));
            tags.putString(MASTER_BLOCK, Registry.BLOCK.getKey(masterBlock).toString());
        }
        return tags;
    }

    @Nullable
    public static BlockPos readOptionalPos(CompoundTag parent, String key, BlockPos offset) {
        if (parent.contains(key, Tag.TAG_COMPOUND)) {
            return NbtUtils.readBlockPos(parent.getCompound(key)).offset(offset);
        }
        return null;
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        readMaster(pTag);
    }


    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        writeMaster(nbt);
    }

    public boolean isClient() {
        return this.getLevel() != null && this.getLevel().isClientSide;
    }
}

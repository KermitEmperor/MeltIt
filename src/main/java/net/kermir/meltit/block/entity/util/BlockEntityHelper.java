package net.kermir.meltit.block.entity.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class BlockEntityHelper {

    @SuppressWarnings("deprecation")
    public static boolean isBlockLoaded(@Nullable BlockGetter world, BlockPos pos) {
        if (world == null) {
            return false;
        }
        if (world instanceof LevelReader) {
            return ((LevelReader) world).hasChunkAt(pos);
        }
        return true;
    }

    public static <T> Optional<T> get(Class<T> clazz, BlockGetter getter, BlockPos pos) {
        if (!isBlockLoaded(getter, pos)) {
            return Optional.empty();
        }

        BlockEntity blockEntity = getter.getBlockEntity(pos);
        if (blockEntity == null) {
            return Optional.empty();
        }

        if (clazz.isInstance(blockEntity)) {
            return Optional.of(clazz.cast(blockEntity));
        }

        return Optional.empty();
    }


}

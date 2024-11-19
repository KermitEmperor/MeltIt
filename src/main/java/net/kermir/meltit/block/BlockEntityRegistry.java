package net.kermir.meltit.block;

import net.kermir.meltit.MeltIt;
import net.kermir.meltit.block.multiblock.controller.entity.SmelteryControllerBlockEntity;
import net.kermir.meltit.block.multiblock.module.entity.SmelteryModuleBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntityRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, MeltIt.MOD_ID);


    public static final RegistryObject<BlockEntityType<SmelteryControllerBlockEntity>> SMELTERY_CONTROLLER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("smeltery_controller_block_entity", () ->
                    BlockEntityType.Builder.of(SmelteryControllerBlockEntity::new,
                            BlockRegistry.SMELTERY_CONTROLLER.get()).build(null));


    public static final RegistryObject<BlockEntityType<SmelteryModuleBlockEntity>> SMELTERY_MODULE = BLOCK_ENTITIES.register("smeltery_module", ()->
            BlockEntityType.Builder.of(SmelteryModuleBlockEntity::new,
                    BlockRegistry.WALL_BLOCK.get(),
                    BlockRegistry.WALL_GLASS_BLOCK.get()
            ).build(null));



    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}

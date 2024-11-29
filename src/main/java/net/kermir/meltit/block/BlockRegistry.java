package net.kermir.meltit.block;

import net.kermir.meltit.MeltIt;
import net.kermir.meltit.block.multiblock.controller.SmelteryController;
import net.kermir.meltit.block.multiblock.module.SmelteryModuleBlockBase;
import net.kermir.meltit.block.multiblock.module.SmelteryModuleTransparentWall;
import net.kermir.meltit.block.multiblock.module.SmelteryModuleWall;
import net.kermir.meltit.item.ItemRegistry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class BlockRegistry {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MeltIt.MOD_ID);

    //public static final Smelter

    public static final RegistryObject<Block> WALL_BLOCK = registerBlock("wall_block",
            () -> new SmelteryModuleWall(BlockBehaviour.Properties.of(Material.METAL).strength(2f), true), MeltIt.MOD_TAB);

    public static final RegistryObject<Block> WALL_GLASS_BLOCK = registerBlock("wall_glass_block",
            () -> new SmelteryModuleTransparentWall(BlockBehaviour.Properties.of(Material.GLASS).strength(2f).noOcclusion(), true), MeltIt.MOD_TAB);

    public static final RegistryObject<Block> SMELTERY_CONTROLLER = registerBlock("smeltery_controller",
            () -> new SmelteryController(BlockBehaviour.Properties.copy(Blocks.BRICKS)), MeltIt.MOD_TAB);

    private static <T extends Block>RegistryObject<T> registerBlock(String name, Supplier<T> block, CreativeModeTab tab) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn, tab);
        return toReturn;
    }

    private static <T extends Block>RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block, CreativeModeTab tab) {
        return ItemRegistry.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(tab)));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}

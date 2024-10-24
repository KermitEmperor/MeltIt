package net.kermir.meltit;

import com.mojang.logging.LogUtils;
import net.kermir.meltit.block.BlockRegistry;
import net.kermir.meltit.item.CreativeTab;
import net.kermir.meltit.item.ItemRegistry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MeltIt.MOD_ID)
public class MeltIt {
    public static final String MOD_ID = "meltit";
    public static final CreativeModeTab MOD_TAB = new CreativeTab(MOD_ID+"_tab");

    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public MeltIt() {
        // Register the setup method for modloading
        IEventBus ModEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEventBus.addListener(this::setup);

        ItemRegistry.register(ModEventBus);
        BlockRegistry.register(ModEventBus);


        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM MELT IT :D");
    }
}

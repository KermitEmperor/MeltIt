package net.kermir.meltit;
import com.mojang.logging.LogUtils;
import net.kermir.meltit.block.entity.BlockEntityRegistry;
import net.kermir.meltit.block.BlockRegistry;
import net.kermir.meltit.event.EventBusEvents;
import net.kermir.meltit.item.CreativeTab;
import net.kermir.meltit.item.ItemRegistry;
import net.kermir.meltit.networking.PacketChannel;
import net.kermir.meltit.recipe.RecipeRegistry;
import net.kermir.meltit.render.RenderBox;
import net.kermir.meltit.render.blockentity.FuelTankRenderer;
import net.kermir.meltit.screen.MenuTypeRegistries;
import net.kermir.meltit.screen.SmelteryControllerScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MeltIt.MOD_ID)
public class MeltIt {
    public static final String MOD_ID = "meltit";
    public static final CreativeModeTab MOD_TAB = new CreativeTab(MOD_ID+"_tab");

    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public MeltIt() {
        // Register the setup method for modloading
        IEventBus ModEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEventBus.addListener(this::commonSetup);
        ModEventBus.addListener(this::clientSetup);

        ItemRegistry.register(ModEventBus);
        BlockRegistry.register(ModEventBus);
        BlockEntityRegistry.register(ModEventBus);
        MenuTypeRegistries.register(ModEventBus);
        RecipeRegistry.register(ModEventBus);


        // Register ourselves for server and other game events we are interested in
        IEventBus ForgeEventBus = MinecraftForge.EVENT_BUS;
        ForgeEventBus.register(this);
        ForgeEventBus.register(new RenderBox());
        ForgeEventBus.register(new EventBusEvents());
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        MenuScreens.register(MenuTypeRegistries.SMELTERY_CONTROLLER_MENU.get(), SmelteryControllerScreen::new);

        ItemBlockRenderTypes.setRenderLayer(BlockRegistry.WALL_GLASS_BLOCK.get(), RenderType.translucent());

        BlockEntityRenderers.register(BlockEntityRegistry.SMELTERY_FUEL_TANK_MODULE.get(), FuelTankRenderer::new);
        ItemBlockRenderTypes.setRenderLayer(BlockRegistry.FUEL_TANK.get(), RenderType.translucent());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        PacketChannel.register();
        LOGGER.info("HELLO FROM MELT IT :D");
    }
}

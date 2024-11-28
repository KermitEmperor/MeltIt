package net.kermir.meltit.event;

import net.kermir.meltit.MeltIt;
import net.kermir.meltit.recipe.types.SmelteryMeltingRecipe;
import net.minecraft.core.Registry;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MeltIt.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EventBusEvents {

    @SubscribeEvent
    public static void recipeTypesRegister(final RegistryEvent.Register<RecipeSerializer<?>> event) {
        Registry.register(Registry.RECIPE_TYPE, SmelteryMeltingRecipe.Type.ID, SmelteryMeltingRecipe.Type.INSTANCE);
    }
}

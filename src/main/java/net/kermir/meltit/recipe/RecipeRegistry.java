package net.kermir.meltit.recipe;

import net.kermir.meltit.MeltIt;
import net.kermir.meltit.recipe.types.SmelteryMeltingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RecipeRegistry {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MeltIt.MOD_ID);

    public static final RegistryObject<RecipeSerializer<SmelteryMeltingRecipe>> SMELTERY_MELTING_SERIALIZER =
            SERIALIZERS.register("smeltery_melting", () -> SmelteryMeltingRecipe.Serializer.INSTANCE);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}

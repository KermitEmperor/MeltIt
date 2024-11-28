package net.kermir.meltit.recipe.types;

import com.google.gson.JsonObject;
import net.kermir.meltit.MeltIt;
import net.kermir.meltit.recipe.FluidStackJson;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Objects;

public class SmelteryMeltingRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final Ingredient ingredient;
    private final FluidStack output;
    private final int smeltTime;
    private final int requiredTemp;

    public SmelteryMeltingRecipe(ResourceLocation id, Ingredient ingredient, FluidStack output, int smeltTime, int requiredTemp) {
        this.id = id;
        this.ingredient = ingredient;
        this.output = output;
        this.smeltTime = smeltTime;
        this.requiredTemp = requiredTemp;
    }

    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {
        //TODO this, check if temperature is correct aswell later
        return ingredient.test(pContainer.getItem(0));
    }

    @Override
    public ItemStack assemble(SimpleContainer pContainer) {
        return ItemStack.EMPTY;
    }

    public FluidStack fluidAssemble(SimpleContainer pContainer) {
        return output;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return false;
    }

    //WARING: DON'T USE THIS METHOD
    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    public FluidStack getResultOutput() {
        return output.copy();
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public int getSmeltTime() {
        return smeltTime;
    }

    public int getRequiredTemp() {
        return requiredTemp;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<SmelteryMeltingRecipe> {
        private Type() {}
        public static final Type INSTANCE = new Type();
        public static final String ID = "smeltery_melting";
    }

    public static class Serializer implements RecipeSerializer<SmelteryMeltingRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(MeltIt.MOD_ID, "smeltery_melting");

        @Override
        public SmelteryMeltingRecipe fromJson(ResourceLocation id, JsonObject json) {
            Ingredient input = Ingredient.fromJson(json.get("input"));

            FluidStack output = FluidStackJson.from(json, "output");

            int minTemperature;
            if (json.has("temperature"))
                minTemperature = json.get("temperature").getAsInt();
            else minTemperature = Objects.requireNonNull(ForgeRegistries.FLUIDS.getValue(new ResourceLocation("minecraft:lava"))).getAttributes().getTemperature();

            int timeInTicks;
            if (json.has("time"))
                timeInTicks = json.get("time").getAsInt();
            else timeInTicks = 1200; //30 seconds


            return new SmelteryMeltingRecipe(id, input, output, timeInTicks, minTemperature);
        }

        @Override
        public SmelteryMeltingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            Ingredient input = Ingredient.fromNetwork(buf);
            FluidStack output = buf.readFluidStack();

            int timeInTicks = buf.readInt();
            int minTemperature = buf.readInt();


            return new SmelteryMeltingRecipe(id, input, output, timeInTicks, minTemperature);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, SmelteryMeltingRecipe recipe) {
            recipe.getIngredient().toNetwork(buf); // input Ingredient
            buf.writeFluidStack(recipe.getResultOutput()); //output Fluid

            buf.writeInt(recipe.getSmeltTime()); //time
            buf.writeInt(recipe.getRequiredTemp()); //temperature
        }

        @Override
        public RecipeSerializer<?> setRegistryName(ResourceLocation name) {
            return INSTANCE;
        }

        @Nullable
        @Override
        public ResourceLocation getRegistryName() {
            return ID;
        }

        @Override
        public Class<RecipeSerializer<?>> getRegistryType() {
            return Serializer.castClass(RecipeSerializer.class);
        }

        @SuppressWarnings("unchecked") // Need this wrapper, because generics
        private static <G> Class<G> castClass(Class<?> cls) {
            return (Class<G>) cls;
        }
    }
}

package net.kermir.meltit.recipe;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;


public class FluidStackJson {

    public static FluidStack from(JsonObject json, String output) {

        JsonObject outputJson = json.getAsJsonObject(output);
        String fluidname = outputJson.get("fluid").getAsString();
        int amount = outputJson.get("amount").getAsInt();

        Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidname));
        return new FluidStack(fluid, amount);
    }
}

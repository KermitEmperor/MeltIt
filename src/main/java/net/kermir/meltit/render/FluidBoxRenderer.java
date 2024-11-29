package net.kermir.meltit.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

//Credit to slime knights, I have no clue about rendering stuff
public class FluidBoxRenderer {

    public static TextureAtlasSprite getBlockSprite(ResourceLocation sprite) {
        return Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(sprite);
    }

    public static int withBlockLight(int combinedLight, int blockLight) {
        // skylight from the combined plus larger block light between combined and parameter
        // not using methods from LightTexture to reduce number of operations
        return (combinedLight & 0xFFFF0000) | Math.max(blockLight << 4, combinedLight & 0xFFFF);
    }

    public static void renderLargeFluidBox(PoseStack matrices, VertexConsumer builder, FluidStack fluid, int brightness,
                                               int xd, float[] xBounds, int zd, float[] zBounds, float yMin, float yMax) {
        if(yMin >= yMax || fluid.isEmpty()) {
            return;
        }
        // fluid attributes
        FluidAttributes attributes = fluid.getFluid().getAttributes();
        TextureAtlasSprite still = getBlockSprite(attributes.getStillTexture(fluid));
        int color = attributes.getColor(fluid);
        brightness = withBlockLight(brightness, attributes.getLuminosity(fluid));
        boolean upsideDown = attributes.isGaseous(fluid);

        // the liquid can stretch over more blocks than the subtracted height is if yMin's decimal is bigger than yMax's decimal (causing UV over 1)
        // ignoring the decimals prevents this, as yd then equals exactly how many ints are between the two
        // for example, if yMax = 5.1 and yMin = 2.3, 2.8 (which rounds to 2), with the face array becoming 2.3, 3, 4, 5.1
        int yd = (int) (yMax - (int) yMin);
        // except in the rare case of yMax perfectly aligned with the block, causing the top face to render multiple times
        // for example, if yMax = 3 and yMin = 1, the values of the face array become 1, 2, 3, 3 as we then have middle ints
        if (yMax % 1d == 0) yd--;
        float[] yBounds = getBlockBounds(yd, yMin, yMax);

        // render each side
        Matrix4f matrix = matrices.last().pose();
        Vector3f from = new Vector3f();
        Vector3f to = new Vector3f();
        int rotation = upsideDown ? 180 : 0;
        for(int y = 0; y <= yd; y++) {
            for(int z = 0; z <= zd; z++) {
                for(int x = 0; x <= xd; x++) {
                    from.set(xBounds[x], yBounds[y], zBounds[z]);
                    to.set(xBounds[x + 1], yBounds[y + 1], zBounds[z + 1]);
                    if (x == 0)  putTexturedQuad(builder, matrix, still, from, to, Direction.WEST,  color, brightness, rotation, false);
                    if (x == xd) putTexturedQuad(builder, matrix, still, from, to, Direction.EAST,  color, brightness, rotation, false);
                    if (z == 0)  putTexturedQuad(builder, matrix, still, from, to, Direction.NORTH, color, brightness, rotation, false);
                    if (z == zd) putTexturedQuad(builder, matrix, still, from, to, Direction.SOUTH, color, brightness, rotation, false);
                    if (y == yd) putTexturedQuad(builder, matrix, still, from, to, Direction.UP,    color, brightness, rotation, false);
                    if (y == 0) {
                        // increase Y position slightly to prevent z fighting on neighboring fluids
                        from.setY(from.y() + 0.001f);
                        putTexturedQuad(builder, matrix, still,   from, to, Direction.DOWN,  color, brightness, rotation, false);
                    }
                }
            }
        }
    }

    public static float[] getBlockBounds(int delta, float start, float end) {
        float[] bounds = new float[2 + delta];
        bounds[0] = start;
        int offset = (int) start;
        for(int i = 1; i <= delta; i++) bounds[i] = i + offset;
        bounds[delta+1] = end;
        return bounds;
    }

    public static void putTexturedQuad(VertexConsumer renderer, Matrix4f matrix, TextureAtlasSprite sprite, Vector3f from, Vector3f to, Direction face, int color, int brightness, int rotation, boolean flowing) {
        // start with texture coordinates
        float x1 = from.x(), y1 = from.y(), z1 = from.z();
        float x2 = to.x(), y2 = to.y(), z2 = to.z();
        // choose UV based on the directions, some need to negate UV due to the direction
        // note that we use -UV instead of 1-UV as its slightly simpler and the later logic deals with negatives
        float u1, u2, v1, v2;
        switch (face) {
            default -> { // DOWN
                u1 = x1; u2 = x2;
                v1 = z2; v2 = z1;
            }
            case UP -> {
                u1 = x1; u2 = x2;
                v1 = -z1; v2 = -z2;
            }
            case NORTH -> {
                u1 = -x1; u2 = -x2;
                v1 = y1; v2 = y2;
            }
            case SOUTH -> {
                u1 = x2; u2 = x1;
                v1 = y1; v2 = y2;
            }
            case WEST -> {
                u1 = z2; u2 = z1;
                v1 = y1; v2 = y2;
            }
            case EAST -> {
                u1 = -z1; u2 = -z2;
                v1 = y1; v2 = y2;
            }
        }

        // flip V when relevant
        if (rotation == 0 || rotation == 270) {
            float temp = v1;
            v1 = -v2;
            v2 = -temp;
        }
        // flip U when relevant
        if (rotation >= 180) {
            float temp = u1;
            u1 = -u2;
            u2 = -temp;
        }

        // bound UV to be between 0 and 1
        boolean reverse = u1 > u2;
        u1 = boundUV(u1, reverse);
        u2 = boundUV(u2, !reverse);
        reverse = v1 > v2;
        v1 = boundUV(v1, reverse);
        v2 = boundUV(v2, !reverse);

        // if rotating by 90 or 270, swap U and V
        float minU, maxU, minV, maxV;
        double size = flowing ? 8 : 16;
        if ((rotation % 180) == 90) {
            minU = sprite.getU(v1 * size);
            maxU = sprite.getU(v2 * size);
            minV = sprite.getV(u1 * size);
            maxV = sprite.getV(u2 * size);
        } else {
            minU = sprite.getU(u1 * size);
            maxU = sprite.getU(u2 * size);
            minV = sprite.getV(v1 * size);
            maxV = sprite.getV(v2 * size);
        }
        // based on rotation, put coords into place
        float u3, u4, v3, v4;
        switch(rotation) {
            default -> { // 0
                u1 = minU; v1 = maxV;
                u2 = minU; v2 = minV;
                u3 = maxU; v3 = minV;
                u4 = maxU; v4 = maxV;
            }
            case 90 -> {
                u1 = minU; v1 = minV;
                u2 = maxU; v2 = minV;
                u3 = maxU; v3 = maxV;
                u4 = minU; v4 = maxV;
            }
            case 180 -> {
                u1 = maxU; v1 = minV;
                u2 = maxU; v2 = maxV;
                u3 = minU; v3 = maxV;
                u4 = minU; v4 = minV;
            }
            case 270 -> {
                u1 = maxU; v1 = maxV;
                u2 = minU; v2 = maxV;
                u3 = minU; v3 = minV;
                u4 = maxU; v4 = minV;
            }
        }
        // add quads
        int light1 = brightness & 0xFFFF;
        int light2 = brightness >> 0x10 & 0xFFFF;
        int a = color >> 24 & 0xFF;
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        switch (face) {
            case DOWN -> {
                renderer.vertex(matrix, x1, y1, z2).color(r, g, b, a).uv(u1, v1).uv2(light1, light2).endVertex();
                renderer.vertex(matrix, x1, y1, z1).color(r, g, b, a).uv(u2, v2).uv2(light1, light2).endVertex();
                renderer.vertex(matrix, x2, y1, z1).color(r, g, b, a).uv(u3, v3).uv2(light1, light2).endVertex();
                renderer.vertex(matrix, x2, y1, z2).color(r, g, b, a).uv(u4, v4).uv2(light1, light2).endVertex();
            }
            case UP -> {
                renderer.vertex(matrix, x1, y2, z1).color(r, g, b, a).uv(u1, v1).uv2(light1, light2).endVertex();
                renderer.vertex(matrix, x1, y2, z2).color(r, g, b, a).uv(u2, v2).uv2(light1, light2).endVertex();
                renderer.vertex(matrix, x2, y2, z2).color(r, g, b, a).uv(u3, v3).uv2(light1, light2).endVertex();
                renderer.vertex(matrix, x2, y2, z1).color(r, g, b, a).uv(u4, v4).uv2(light1, light2).endVertex();
            }
            case NORTH -> {
                renderer.vertex(matrix, x1, y1, z1).color(r, g, b, a).uv(u1, v1).uv2(light1, light2).endVertex();
                renderer.vertex(matrix, x1, y2, z1).color(r, g, b, a).uv(u2, v2).uv2(light1, light2).endVertex();
                renderer.vertex(matrix, x2, y2, z1).color(r, g, b, a).uv(u3, v3).uv2(light1, light2).endVertex();
                renderer.vertex(matrix, x2, y1, z1).color(r, g, b, a).uv(u4, v4).uv2(light1, light2).endVertex();
            }
            case SOUTH -> {
                renderer.vertex(matrix, x2, y1, z2).color(r, g, b, a).uv(u1, v1).uv2(light1, light2).endVertex();
                renderer.vertex(matrix, x2, y2, z2).color(r, g, b, a).uv(u2, v2).uv2(light1, light2).endVertex();
                renderer.vertex(matrix, x1, y2, z2).color(r, g, b, a).uv(u3, v3).uv2(light1, light2).endVertex();
                renderer.vertex(matrix, x1, y1, z2).color(r, g, b, a).uv(u4, v4).uv2(light1, light2).endVertex();
            }
            case WEST -> {
                renderer.vertex(matrix, x1, y1, z2).color(r, g, b, a).uv(u1, v1).uv2(light1, light2).endVertex();
                renderer.vertex(matrix, x1, y2, z2).color(r, g, b, a).uv(u2, v2).uv2(light1, light2).endVertex();
                renderer.vertex(matrix, x1, y2, z1).color(r, g, b, a).uv(u3, v3).uv2(light1, light2).endVertex();
                renderer.vertex(matrix, x1, y1, z1).color(r, g, b, a).uv(u4, v4).uv2(light1, light2).endVertex();
            }
            case EAST -> {
                renderer.vertex(matrix, x2, y1, z1).color(r, g, b, a).uv(u1, v1).uv2(light1, light2).endVertex();
                renderer.vertex(matrix, x2, y2, z1).color(r, g, b, a).uv(u2, v2).uv2(light1, light2).endVertex();
                renderer.vertex(matrix, x2, y2, z2).color(r, g, b, a).uv(u3, v3).uv2(light1, light2).endVertex();
                renderer.vertex(matrix, x2, y1, z2).color(r, g, b, a).uv(u4, v4).uv2(light1, light2).endVertex();
            }
        }
    }

    private static float boundUV(float value, boolean upper) {
        value = value % 1;
        if (value == 0) {
            // if it lands exactly on the 0 bound, map that to 1 instead for the larger UV
            return upper ? 1 : 0;
        }
        // modulo returns a negative result if the input is negative, so add 1 to account for that
        return value < 0 ? (value + 1) : value;
    }
}

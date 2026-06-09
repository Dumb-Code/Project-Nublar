package net.dumbcode.projectnublar.block.api.fence;

import net.dumbcode.projectnublar.Constants;
import net.minecraft.resources.ResourceLocation;


public enum EnumConnectionType implements ConnectionType {
    LOW_SECURITY(2, 3, 6/16F, 0.75F, 90F, 1/8F, 10),
    HIGH_SECURITY(1, 8, 1/2F, 2F, 0F, 2/8F, 15);
    private final double[] offsets;
    private final int height;
    private final float radius;
    private final float cableWidth;
    private final float rotationOffset;
    private final float halfSize;
    private final int lightLevel;
    private final ResourceLocation registryName;

    EnumConnectionType(int amount, int height, float radius, float cableWidth, float defaultRotation, float halfSize, int lightLevel) {
        this.offsets = new double[amount];
        this.height = height;
        this.radius = radius;
        this.cableWidth = cableWidth / 32F; //cableWidth is actually halfCableWidth, the 16 comes from the texturemap size
        this.rotationOffset = defaultRotation;
        this.halfSize = halfSize;
        this.lightLevel = lightLevel;
        this.registryName = new ResourceLocation(Constants.MODID, "textures/blocks/" + this.name().toLowerCase() + "_electric_fence_pole.png");

        double off = 1D / (amount * 2);
        for (int i = 0; i < amount; i++) {
            this.offsets[i] = (i*2 + 1) * off;
        }
        this.register();
    }

    @Override
    public double[] getOffsets() {
        return offsets;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public float getRadius() {
        return radius;
    }

    @Override
    public float getCableWidth() {
        return cableWidth;
    }

    @Override
    public float getRotationOffset() {
        return rotationOffset;
    }

    @Override
    public float getHalfSize() {
        return halfSize;
    }

    @Override
    public int getLightLevel() {
        return lightLevel;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return registryName;
    }
}

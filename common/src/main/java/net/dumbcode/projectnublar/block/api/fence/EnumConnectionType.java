package net.dumbcode.projectnublar.block.api.fence;

import net.dumbcode.projectnublar.Constants;
import net.minecraft.resources.ResourceLocation;


/**
 * The two built-in fence wire types. Constructor arguments are
 * (wiresPerPole, poleHeight, poleRadius, halfCableWidth, defaultRotationDegrees, halfSize,
 * lightLevel); every value is a frozen behavioral/geometry constant.
 */
public enum EnumConnectionType implements ConnectionType {
    LOW_SECURITY(
            EnumConnectionType.LOW_SECURITY_WIRE_COUNT,
            EnumConnectionType.LOW_SECURITY_POLE_HEIGHT,
            EnumConnectionType.LOW_SECURITY_RADIUS,
            EnumConnectionType.LOW_SECURITY_HALF_CABLE_WIDTH,
            EnumConnectionType.LOW_SECURITY_ROTATION_DEGREES,
            EnumConnectionType.LOW_SECURITY_HALF_SIZE,
            EnumConnectionType.LOW_SECURITY_LIGHT_LEVEL),
    HIGH_SECURITY(
            EnumConnectionType.HIGH_SECURITY_WIRE_COUNT,
            EnumConnectionType.HIGH_SECURITY_POLE_HEIGHT,
            EnumConnectionType.HIGH_SECURITY_RADIUS,
            EnumConnectionType.HIGH_SECURITY_HALF_CABLE_WIDTH,
            EnumConnectionType.HIGH_SECURITY_ROTATION_DEGREES,
            EnumConnectionType.HIGH_SECURITY_HALF_SIZE,
            EnumConnectionType.HIGH_SECURITY_LIGHT_LEVEL);

    private static final int LOW_SECURITY_WIRE_COUNT = 2;
    private static final int LOW_SECURITY_POLE_HEIGHT = 3;
    private static final float LOW_SECURITY_RADIUS = 6 / 16F;
    private static final float LOW_SECURITY_HALF_CABLE_WIDTH = 0.75F;
    private static final float LOW_SECURITY_ROTATION_DEGREES = 90F;
    private static final float LOW_SECURITY_HALF_SIZE = 1 / 8F;
    private static final int LOW_SECURITY_LIGHT_LEVEL = 10;

    private static final int HIGH_SECURITY_WIRE_COUNT = 1;
    private static final int HIGH_SECURITY_POLE_HEIGHT = 8;
    private static final float HIGH_SECURITY_RADIUS = 1 / 2F;
    private static final float HIGH_SECURITY_HALF_CABLE_WIDTH = 2F;
    private static final float HIGH_SECURITY_ROTATION_DEGREES = 0F;
    private static final float HIGH_SECURITY_HALF_SIZE = 2 / 8F;
    private static final int HIGH_SECURITY_LIGHT_LEVEL = 15;
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

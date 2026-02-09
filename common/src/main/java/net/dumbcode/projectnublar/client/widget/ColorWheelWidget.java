package net.dumbcode.projectnublar.client.widget;

import com.mojang.blaze3d.platform.NativeImage;

import net.dumbcode.projectnublar.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;
public class ColorWheelWidget extends AbstractWidget {
    public static final ResourceLocation COLOR_WHEEL = new ResourceLocation(Constants.MODID, "color_wheel");
    public static final ResourceLocation COLOR_WHEEL_BLIP = new ResourceLocation(Constants.MODID, "textures/gui/color_wheel_blip.png");
    private int centerX;
    private int centerY;
    private int colorWheelBlipLocationX;
    private int colorWheelBlipLocationY;
    private float currentHue = 0.0F;
    private float currentSaturation = 0.0F;
    private float brightness = 1.0F;
    private int radius;
    private OnColorChanged onColorChanged;


    public ColorWheelWidget(int pX, int pY, int pWidth, int pHeight, Component pMessage, OnColorChanged onColorChanged) {
        super(pX, pY, pWidth, pHeight, pMessage);
        this.centerX = pX + (pHeight / 2);
        this.centerY = pY + (pHeight / 2);
        this.colorWheelBlipLocationX = centerX;
        this.colorWheelBlipLocationY = centerY;
        this.radius = pHeight / 2;
        this.onColorChanged = onColorChanged;
    }

    public float getBrightness() {
        return brightness;
    }

    public void setBrightness(float currentValue) {
        this.brightness = currentValue;
        Minecraft.getInstance().getTextureManager().register(COLOR_WHEEL, makeColorWheel(currentValue));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int i1, float v) {
        guiGraphics.blit(COLOR_WHEEL, this.getX(), this.getY(), 0, 0, this.height, this.height, this.height, this.height);
        guiGraphics.blit(COLOR_WHEEL_BLIP, this.colorWheelBlipLocationX - 1, this.colorWheelBlipLocationY - 1, 0, 0, 3, 3, 3, 3);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    protected void onDrag(double pMouseX, double pMouseY, double $$2, double $$3) {
        checkColorAtPoint(pMouseX, pMouseY);
    }

    @Override
    public void onClick(double mouseX, double mousey) {
        checkColorAtPoint(mouseX, mousey);
    }


    public boolean checkColorAtPoint(double mouseX, double mouseY) {
        final double radius2 = radius * radius;
        final double PI2 = 2 * Math.PI;
        double dist2;
        int x = (int) mouseX;
        int y = (int) mouseY;
        dist2 = distance2(x, y, centerX, centerY);
        if (dist2 > radius2) {
            return false;
        }
        this.currentHue = (float) (Math.atan2(y - centerY, x - centerX) / PI2);
        this.currentSaturation = (float) Math.sqrt((float) dist2) / (float) radius;
        this.onColorChanged.onColorChanged(currentHue, currentSaturation);
        this.colorWheelBlipLocationX = (int) mouseX;
        this.colorWheelBlipLocationY = (int) mouseY;
        return true;
    }

    public void setFromRGB(int color) {
        Color c = new Color(color);

        float[] hsv = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        this.currentHue = hsv[0];
        this.currentSaturation = hsv[1];
        this.brightness = hsv[2];
        this.colorWheelBlipLocationX = (int) (centerX + radius * currentSaturation * Math.cos(currentHue * 2 * Math.PI));
        this.colorWheelBlipLocationY = (int) (centerY + radius * currentSaturation * Math.sin(currentHue * 2 * Math.PI));
    }

    public void setColor(float hue, float saturation) {
        this.currentHue = hue;
        this.currentSaturation = saturation;
        this.colorWheelBlipLocationX = (int) (centerX + radius * currentSaturation * Math.cos(currentHue * 2 * Math.PI));
        this.colorWheelBlipLocationY = (int) (centerY + radius * currentSaturation * Math.sin(currentHue * 2 * Math.PI));
    }

    public int getCurrentColor(float hue, float sat, float value) {
        return Color.HSBtoRGB(hue, sat, value);
    }

    public static DynamicTexture makeColorWheel(double brightness) {

        int radius = 200;
        final int diameter = radius * 2;
        final double radius2 = radius * radius;
        float hue, sat;
        final double PI2 = 2 * Math.PI;
        double dist2;
        int rgb;
        NativeImage buffer = new NativeImage(diameter, diameter, false);
        for (int x = 0; x < diameter; x++) {
            for (int y = 0; y < diameter; y++) {
                dist2 = distance2(x, y, radius, radius);
                if (dist2 > radius2) {
                    buffer.setPixelRGBA(x, y, 0xFFFFFF);
                    continue;
                }
                hue = (float) (Math.atan2(y - radius, x - radius) / PI2);
                sat = (float) Math.sqrt((float) dist2) / (float) radius;
                rgb = Color.HSBtoRGB(-hue - (1f / 3f), sat, (float)brightness);
                buffer.setPixelRGBA(x, y, rgb);

            }
        }
        return new DynamicTexture(buffer);
    }
    static int distance2(int x1, int y1, int x2, int y2) {
        int a = x2 - x1;
        int b = y2 - y1;
        return a * a + b * b;
    }
    public interface OnColorChanged {
        void onColorChanged(float hue, float saturation);
    }
}

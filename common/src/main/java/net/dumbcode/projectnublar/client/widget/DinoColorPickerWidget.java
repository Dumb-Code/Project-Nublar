package net.dumbcode.projectnublar.client.widget;



import net.dumbcode.projectnublar.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

import java.awt.*;

public class DinoColorPickerWidget<T extends GuiEventListener> extends ParentWidget<T> {
    public static final ResourceLocation COLOR_WHEEL_BACKGROUND = new ResourceLocation(Constants.MODID, "textures/gui/color_wheel_background.png");
    private float currentHue = 0;
    private float currentSaturation = 0;
    private float currentValue = 0.0f;
    private OnColorChanged onColorChanged;
    private ColorWheelWidget colorWheelWidget;
    private VerticalGradientBarWidget valueBarWidget;
    private VanillaColorPickerWidget vanillaColorPickerWidget;
//    private EditBox redEditBox;
//    private EditBox greenEditBox;
//    private EditBox blueEditBox;
//    private EditBox hueEditBox;
//    private EditBox saturationEditBox;
//    private EditBox valueEditBox;


    public DinoColorPickerWidget(T parent, int pX, int pY, int pWidth, int pHeight, Component pMessage, OnColorChanged onColorChanged) {
        super(parent,pX, pY, pWidth, pHeight, pMessage);
        this.onColorChanged = onColorChanged;
    }

    @Override
    public void init(boolean rebuild) {
        this.colorWheelWidget = this.addRenderableWidget(new ColorWheelWidget(this.getX() + calculateWidgetWidth(5), this.getY() + calculateWidgetHeight(5), calculateWidgetWidth(70), calculateWidgetHeight(70), null, (hue, saturation) -> {
            this.currentHue = hue;
            this.currentSaturation = saturation;
            this.onColorChanged.onColorChanged(getCurrentColor(this.currentHue, this.currentSaturation, this.currentValue));
            this.valueBarWidget.setTopColor(getCurrentColor(this.currentHue, this.currentSaturation, 1.0f));
            updateEditBoxes();
        }));
        this.valueBarWidget = this.addRenderableWidget(new VerticalGradientBarWidget(this.getX() + calculateWidgetWidth(85), this.getY() + calculateWidgetHeight(5), calculateWidgetWidth(12), calculateWidgetHeight(70), null, (oldValue, newValue) -> {
            this.currentValue = newValue;
            this.colorWheelWidget.setBrightness(newValue);
            this.onColorChanged.onColorChanged(getCurrentColor(this.currentHue, this.currentSaturation, this.currentValue));
            updateEditBoxes();
        }));
        this.vanillaColorPickerWidget = this.addRenderableWidget(new VanillaColorPickerWidget(this.getX() + calculateWidgetWidth(5), this.getY() + calculateWidgetHeight(77), calculateWidgetWidth(95), Component.literal(""), (color) -> {
            this.setFromRBG(color);
            this.onColorChanged.onColorChanged(color);
        }));
        Font font = Minecraft.getInstance().font;
//        this.redEditBox = this.addRenderableWidget(new EditBox(font, this.getX() + calculateWidgetWidth(97), this.getY() + calculateWidgetHeight(5), calculateWidgetWidth(26), calculateWidgetHeight(10), Component.literal("r")));
//        this.greenEditBox = this.addRenderableWidget(new EditBox(font, this.getX() + calculateWidgetWidth(97), this.getY() + calculateWidgetHeight(20), calculateWidgetWidth(26), calculateWidgetHeight(10), Component.literal("g")));
//        this.blueEditBox = this.addRenderableWidget(new EditBox(font, this.getX() + calculateWidgetWidth(97), this.getY() + calculateWidgetHeight(35), calculateWidgetWidth(26), calculateWidgetHeight(10), Component.literal("b")));
//        this.hueEditBox = this.addRenderableWidget(new EditBox(font, this.getX() + calculateWidgetWidth(97), this.getY() + calculateWidgetHeight(50), calculateWidgetWidth(26), calculateWidgetHeight(10), Component.literal("h")));
//        this.saturationEditBox = this.addRenderableWidget(new EditBox(font, this.getX() + calculateWidgetWidth(97), this.getY() + calculateWidgetHeight(65), calculateWidgetWidth(26), calculateWidgetHeight(10), Component.literal("s")));
//        this.valueEditBox = this.addRenderableWidget(new EditBox(font, this.getX() + calculateWidgetWidth(97), this.getY() + calculateWidgetHeight(80), calculateWidgetWidth(26), calculateWidgetHeight(10), Component.literal("v")));
//        this.redEditBox.setMaxLength(3);
//        this.greenEditBox.setMaxLength(3);
//        this.blueEditBox.setMaxLength(3);
//        this.hueEditBox.setMaxLength(4);
//        this.saturationEditBox.setMaxLength(4);
//        this.valueEditBox.setMaxLength(4);
        int color = getCurrentColor(this.currentHue, this.currentSaturation, this.currentValue);
//        this.redEditBox.setValue(FastColor.ARGB32.red(color) + "");
//        this.greenEditBox.setValue(FastColor.ARGB32.green(color) + "");
//        this.blueEditBox.setValue(FastColor.ARGB32.blue(color) + "");
//        this.hueEditBox.setValue(currentHue + "");
//        this.saturationEditBox.setValue(currentSaturation + "");
//        this.valueEditBox.setValue(currentValue + "");
//        this.redEditBox.setResponder((p_169394_) -> {
//            int red = tryParseInt(p_169394_);
//            int newColor = FastColor.ARGB32.color(255, red, greenEditBox.getValue().isEmpty() ? 0 : Integer.parseInt(greenEditBox.getValue()), blueEditBox.getValue().isEmpty() ? 0 : Integer.parseInt(blueEditBox.getValue()));
//            this.setFromRBG(newColor,false);
//            this.onColorChanged.onColorChanged(newColor);
//        });
//        this.greenEditBox.setResponder((p_169394_) -> {
//            int green = tryParseInt(p_169394_);
//            int newColor = FastColor.ARGB32.color(255, redEditBox.getValue().isEmpty() ? 0 : Integer.parseInt(redEditBox.getValue()), green, blueEditBox.getValue().isEmpty() ? 0 : Integer.parseInt(blueEditBox.getValue()));
//            this.setFromRBG(newColor,false);
//            this.onColorChanged.onColorChanged(newColor);
//        });
//        this.blueEditBox.setResponder((p_169394_) -> {
//            int blue = tryParseInt(p_169394_);
//            int newColor = FastColor.ARGB32.color(255, redEditBox.getValue().isEmpty() ? 0 : Integer.parseInt(redEditBox.getValue()), greenEditBox.getValue().isEmpty() ? 0 : Integer.parseInt(greenEditBox.getValue()), blue);
//            this.setFromRBG(newColor,false);
//            this.onColorChanged.onColorChanged(newColor);
//        });
//        this.hueEditBox.setResponder((p_169394_) -> {
//            float hue = tryParseFloat(p_169394_);
//            this.currentHue = hue;
//            this.setFromRBG(getCurrentColor(hue, currentSaturation, currentValue),false);
//            this.onColorChanged.onColorChanged(getCurrentColor(hue, currentSaturation, currentValue));
//        });
//        this.saturationEditBox.setResponder((p_169394_) -> {
//            float saturation = tryParseFloat(p_169394_);
//            this.currentSaturation = saturation;
//            this.setFromRBG(getCurrentColor(currentHue, saturation, currentValue),false);
//            this.onColorChanged.onColorChanged(getCurrentColor(currentHue, saturation, currentValue));
//        });
//        this.valueEditBox.setResponder((p_169394_) -> {
//            float value = tryParseFloat(p_169394_);
//            this.currentValue = value;
//            this.setFromRBG(getCurrentColor(currentHue, currentSaturation, value),false);
//            this.onColorChanged.onColorChanged(getCurrentColor(currentHue, currentSaturation, value));
//        });

//        this.addRenderableWidget(redEditBox);
//        this.addRenderableWidget(greenEditBox);
//        this.addRenderableWidget(blueEditBox);
//        this.addRenderableWidget(hueEditBox);
//        this.addRenderableWidget(saturationEditBox);
//        this.addRenderableWidget(valueEditBox);
        this.addRenderableWidget(vanillaColorPickerWidget);
        this.addRenderableWidget(colorWheelWidget);
        this.addRenderableWidget(valueBarWidget);
    }

    @Override
    protected void renderBackground(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        pGuiGraphics.pose().translate(0,0,1000);
//        pGuiGraphics.blit(COLOR_WHEEL_BACKGROUND, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
    }

    @Override
    protected void renderForeground(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {

    }

    public int getCurrentColor(float hue, float sat, float value) {
        return Color.HSBtoRGB(hue, sat, value);
    }

    public interface OnColorChanged {
        void onColorChanged(int color);
    }

    public void setFromRBG(int color) {
        setFromRBG(color, true);
    }
    public void setFromRBG(int color, boolean updateEditBoxes) {
        float[] hsv = new float[3];
        Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, hsv);
        this.currentHue = hsv[0];
        this.currentSaturation = hsv[1];
        this.currentValue = hsv[2];
        this.colorWheelWidget.setBrightness(hsv[2]);
        this.colorWheelWidget.setColor(hsv[0], hsv[1]);
        this.valueBarWidget.setValue(hsv[2]);
        this.valueBarWidget.setTopColor(color);
        if(updateEditBoxes){
            updateEditBoxes();
        }
    }
    public void updateEditBoxes(){
//        this.redEditBox.value=(((getCurrentColor(this.currentHue, this.currentSaturation, this.currentValue) >> 16) & 0xFF) + "");
//        this.greenEditBox.value=(((getCurrentColor(this.currentHue, this.currentSaturation, this.currentValue) >> 8) & 0xFF) + "");
//        this.blueEditBox.value=((getCurrentColor(this.currentHue, this.currentSaturation, this.currentValue) & 0xFF) + "");
//        this.hueEditBox.value = (currentHue + "");
//        this.saturationEditBox.value=(currentSaturation + "");
//        this.valueEditBox.value=(currentValue + "");
    }

    public int tryParseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public float tryParseFloat(String value) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public int calculateWidgetWidth(int x) {
        return Mth.floor(getWidth()/130f * x);
    }
    public int calculateWidgetHeight(int y) {
        return Mth.floor(getHeight()/120f * y);
    }
}

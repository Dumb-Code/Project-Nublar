package net.dumbcode.projectnublar.client.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.text.DecimalFormat;

public class NGLSlider extends AbstractSliderButton {
    public static final ResourceLocation SLIDER_LOCATION = new ResourceLocation("textures/gui/slider.png");
    protected Component prefix;
    protected Component suffix;

    protected double minValue;
    protected double maxValue;
    protected double stepSize;

    protected boolean drawString;

    private final DecimalFormat format;
    private OnValueChanged consumer;
    private Validator validator = (slider, oldValue, newValue) -> true;


    public NGLSlider(int x, int y, int width, int height, Component prefix, Component suffix, double minValue, double maxValue, double currentValue, double stepSize, int precision, boolean drawString, OnValueChanged consumer) {
        super(x, y, width, height, Component.empty(), 0D);
        this.prefix = prefix;
        this.suffix = suffix;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.stepSize = Math.abs(stepSize);
        this.value = this.snapToNearest((currentValue - minValue) / (maxValue - minValue));
        this.drawString = drawString;

        if (stepSize == 0D) {
            precision = Math.min(precision, 4);

            StringBuilder builder = new StringBuilder("0");

            if (precision > 0)
                builder.append('.');

            while (precision-- > 0)
                builder.append('0');

            this.format = new DecimalFormat(builder.toString());
        } else if (Mth.equal(this.stepSize, Math.floor(this.stepSize))) {
            this.format = new DecimalFormat("0");
        } else {
            this.format = new DecimalFormat(Double.toString(this.stepSize).replaceAll("\\d", "0"));
        }
        this.consumer = consumer;
        this.updateMessage();
    }

    public NGLSlider(int x, int y, int width, int height, Component prefix, Component suffix, double minValue, double maxValue, double currentValue, boolean drawString, OnValueChanged consumer) {
        this(x, y, width, height, prefix, suffix, minValue, maxValue, currentValue, 1D, 0, drawString, consumer);
    }

    public double getValue() {
        return this.value * (maxValue - minValue) + minValue;
    }

    public long getValueLong() {
        return Math.round(this.getValue());
    }

    public int getValueInt() {
        return (int) this.getValueLong();
    }

    public void setValue(double value) {
        this.setValue(value, false);
    }

    public void setValue(double value, boolean skipValidation) {
        if (skipValidation || validator.validate(this, this.value, value)) {
            this.value = this.snapToNearest((value - this.minValue) / (this.maxValue - this.minValue));
            this.updateMessage();
        }
    }

    public String getValueString() {
        return this.format.format(this.getValue());
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.setValueFromMouse(mouseX);
    }


    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean flag = keyCode == GLFW.GLFW_KEY_LEFT;
        if (flag || keyCode == GLFW.GLFW_KEY_RIGHT) {
            if (this.minValue > this.maxValue)
                flag = !flag;
            float f = flag ? -1F : 1F;
            if (stepSize <= 0D)
                this.setSliderValue(this.value + (f / (this.width - 8)));
            else
                this.setValue(this.getValue() + f * this.stepSize);
        }

        return false;
    }

    @Override
    protected void onDrag(double mouseX, double $$1, double $$2, double $$3) {
        this.setValueFromMouse(mouseX);
    }

    protected void setValueFromMouse(double mouseX) {
        this.setSliderValue((mouseX - (this.getX() + 4)) / (this.width - 8));
    }

    protected void setSliderValue(double value) {
        this.setSliderValue(value, false);
    }

    protected void setSliderValue(double value, boolean skipValidation) {
        if (skipValidation || validator.validate(this, this.value, value)) {
            double oldValue = this.value;
            this.value = this.snapToNearest(value);
            if (!Mth.equal(oldValue, this.value))
                this.applyValue();

            this.updateMessage();
        }
    }

    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    protected double snapToNearest(double value) {
        if (stepSize <= 0D)
            return Mth.clamp(value, 0D, 1D);

        value = Mth.lerp(Mth.clamp(value, 0D, 1D), this.minValue, this.maxValue);

        value = (stepSize * Math.round(value / stepSize));

        if (this.minValue > this.maxValue) {
            value = Mth.clamp(value, this.maxValue, this.minValue);
        } else {
            value = Mth.clamp(value, this.minValue, this.maxValue);
        }

        return Mth.map(value, this.minValue, this.maxValue, 0D, 1D);
    }

    @Override
    protected void updateMessage() {
        if (this.drawString) {
            this.setMessage(Component.literal("").append(prefix).append(this.getValueString()).append(suffix));
        } else {
            this.setMessage(Component.empty());
        }
    }

    @Override
    protected void applyValue() {
        if (this.consumer != null)
            this.consumer.onValueChanged(this, this.getValue());
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        final Minecraft mc = Minecraft.getInstance();
        renderSliderBackground(guiGraphics);

        renderSliderBar(guiGraphics);

        renderScrollingString(guiGraphics, mc.font, 2, (this.active ? 16777215 : 10526880) | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    public void renderSliderBar(GuiGraphics guiGraphics) {
        GuiHelper.blitWithBorder(guiGraphics, SLIDER_LOCATION, this.getX() + (int) (this.value * (double) (this.width - 8)), this.getY(), 0, getHandleTextureY(), 8, this.height, 200, 20, 2, 3, 2, 2);
    }

    public void renderSliderBackground(GuiGraphics guiGraphics) {
        GuiHelper.blitWithBorder(guiGraphics, SLIDER_LOCATION, this.getX(), this.getY(), 0, getTextureY(), this.width, this.height, 200, 20, 2, 3, 2, 2);
    }

    public interface OnValueChanged {
        void onValueChanged(NGLSlider slider, double value);
    }

    public interface Validator {
        boolean validate(NGLSlider slider, double oldValue, double newValue);
    }
}

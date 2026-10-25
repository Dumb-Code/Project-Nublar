package net.dumbcode.projectnublar.gui.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.text.DecimalFormat;

public class NGLSlider extends AbstractSliderButton {
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
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        this.setValueFromMouse(event.x());
    }


    @Override
    public boolean keyPressed(KeyEvent event) {
        boolean flag = event.isLeft();
        if (flag || event.isRight()) {
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
    protected void onDrag(MouseButtonEvent event, double dragX, double dragY) {
        this.setValueFromMouse(event.x());
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
    public void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderSliderBackground(guiGraphics);

        renderSliderBar(guiGraphics);

        // Grey when inactive comes from the inactive message style (AbstractWidget.WithInactiveMessage).
        this.extractScrollingStringOverContents(guiGraphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE), this.getMessage(), TEXT_MARGIN);
    }

    /** Draws the handle; the vanilla handle sprite is nine-sliced, so it stretches to the slider's height. */
    public void renderSliderBar(GuiGraphicsExtractor guiGraphics) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, getHandleSprite(), this.getX() + (int) (this.value * (double) (this.width - 8)), this.getY(), 8, this.height);
    }

    /** Draws the track; the vanilla track sprite is nine-sliced, so it stretches to the slider's size. */
    public void renderSliderBackground(GuiGraphicsExtractor guiGraphics) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, getSprite(), this.getX(), this.getY(), this.width, this.height);
    }

    public interface OnValueChanged {
        void onValueChanged(NGLSlider slider, double value);
    }

    public interface Validator {
        boolean validate(NGLSlider slider, double oldValue, double newValue);
    }
}

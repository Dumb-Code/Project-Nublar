package net.dumbcode.projectnublar.gui.widget;

//COPYRIGHT OF MOJANG.

import net.minecraft.client.InputType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

/**
 * Copy of the vanilla slider. Since 1.20.2 vanilla draws it from GUI sprites instead of
 * {@code textures/gui/slider.png}; the copy follows the 26.2 vanilla class for that and for the
 * grey inactive text, which now comes from {@link AbstractWidget.WithInactiveMessage}.
 */
public abstract class AbstractSliderButton extends AbstractWidget.WithInactiveMessage {
    private static final Identifier SLIDER_SPRITE = Identifier.withDefaultNamespace("widget/slider");
    private static final Identifier HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("widget/slider_highlighted");
    private static final Identifier SLIDER_HANDLE_SPRITE = Identifier.withDefaultNamespace("widget/slider_handle");
    private static final Identifier SLIDER_HANDLE_HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("widget/slider_handle_highlighted");
    protected static final int TEXT_MARGIN = 2;
    private static final int HEIGHT = 20;
    private static final int HANDLE_HALF_WIDTH = 4;
    private static final int HANDLE_WIDTH = 8;
    protected double value;
    private boolean canChangeValue;

    public AbstractSliderButton(int x, int y, int width, int height, Component message, double value) {
        super(x, y, width, height, message);
        this.value = value;
    }

    /** The track sprite: highlighted while focused but not yet changing the value (the old texture row 1). */
    protected Identifier getSprite() {
        return this.isFocused() && !this.canChangeValue ? HIGHLIGHTED_SPRITE : SLIDER_SPRITE;
    }

    /** The handle sprite: highlighted while hovered or changing the value (the old texture row 3). */
    protected Identifier getHandleSprite() {
        return !this.isHovered && !this.canChangeValue ? SLIDER_HANDLE_SPRITE : SLIDER_HANDLE_HIGHLIGHTED_SPRITE;
    }

    protected MutableComponent createNarrationMessage() {
        return Component.translatable("gui.narrate.slider", new Object[]{this.getMessage()});
    }

    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.slider.usage.focused"));
            } else {
                narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.slider.usage.hovered"));
            }
        }

    }

    public void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.getSprite(), this.getX(), this.getY(), this.getWidth(), this.getHeight(), ARGB.white(this.alpha));
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.getHandleSprite(), this.getX() + (int)(this.value * (double)(this.width - HANDLE_WIDTH)), this.getY(), HANDLE_WIDTH, HEIGHT, ARGB.white(this.alpha));
        this.extractScrollingStringOverContents(guiGraphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE), this.getMessage(), TEXT_MARGIN);
    }

    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        this.setValueFromMouse(event.x());
    }

    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused) {
            this.canChangeValue = false;
        } else {
            InputType inputtype = Minecraft.getInstance().getLastInputType();
            if (inputtype == InputType.MOUSE || inputtype == InputType.KEYBOARD_TAB) {
                this.canChangeValue = true;
            }
        }

    }

    public boolean keyPressed(KeyEvent event) {
        if (event.isSelection()) {
            this.canChangeValue = !this.canChangeValue;
            return true;
        } else {
            if (this.canChangeValue) {
                boolean flag = event.isLeft();
                if (flag || event.isRight()) {
                    float f = flag ? -1.0F : 1.0F;
                    this.setValue(this.value + (double)(f / (float)(this.width - HANDLE_WIDTH)));
                    return true;
                }
            }

            return false;
        }
    }

    private void setValueFromMouse(double mouseX) {
        this.setValue((mouseX - (double)(this.getX() + HANDLE_HALF_WIDTH)) / (double)(this.width - HANDLE_WIDTH));
    }

    private void setValue(double value) {
        double d0 = this.value;
        this.value = Mth.clamp(value, (double)0.0F, (double)1.0F);
        if (d0 != this.value) {
            this.applyValue();
        }

        this.updateMessage();
    }

    protected void onDrag(MouseButtonEvent event, double dragX, double dragY) {
        this.setValueFromMouse(event.x());
        super.onDrag(event, dragX, dragY);
    }

    public void playDownSound(SoundManager handler) {
    }

    public void onRelease(MouseButtonEvent event) {
        super.playDownSound(Minecraft.getInstance().getSoundManager());
    }

    protected abstract void updateMessage();

    protected abstract void applyValue();
}

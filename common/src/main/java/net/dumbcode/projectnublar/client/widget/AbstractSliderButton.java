package net.dumbcode.projectnublar.client.widget;

//COPYRIGHT OF MOJANG.

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.InputType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractSliderButton extends AbstractWidget {
    private static final ResourceLocation SLIDER_LOCATION = new ResourceLocation("textures/gui/slider.png");
    protected static final int TEXTURE_WIDTH = 200;
    protected static final int TEXTURE_HEIGHT = 20;
    protected static final int TEXTURE_BORDER_X = 20;
    protected static final int TEXTURE_BORDER_Y = 4;
    protected static final int TEXT_MARGIN = 2;
    private static final int HEIGHT = 20;
    private static final int HANDLE_HALF_WIDTH = 4;
    private static final int HANDLE_WIDTH = 8;
    private static final int BACKGROUND = 0;
    private static final int BACKGROUND_FOCUSED = 1;
    private static final int HANDLE = 2;
    private static final int HANDLE_FOCUSED = 3;
    protected double value;
    private boolean canChangeValue;

    public AbstractSliderButton(int x, int y, int width, int height, Component message, double value) {
        super(x, y, width, height, message);
        this.value = value;
    }

    public int getTextureY() {
        int i = this.isFocused() && !this.canChangeValue ? 1 : 0;
        return i * 20;
    }

    public int getHandleTextureY() {
        int i = !this.isHovered && !this.canChangeValue ? 2 : 3;
        return i * 20;
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

    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        guiGraphics.blitNineSliced(SLIDER_LOCATION, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 200, 20, 0, this.getTextureY());
        guiGraphics.blitNineSliced(SLIDER_LOCATION, this.getX() + (int)(this.value * (double)(this.width - 8)), this.getY(), 8, 20, 20, 4, 200, 20, 0, this.getHandleTextureY());
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = this.active ? 16777215 : 10526880;
        this.renderScrollingString(guiGraphics, minecraft.font, 2, i | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    public void onClick(double mouseX, double mouseY) {
        this.setValueFromMouse(mouseX);
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

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (CommonInputs.selected(keyCode)) {
            this.canChangeValue = !this.canChangeValue;
            return true;
        } else {
            if (this.canChangeValue) {
                boolean flag = keyCode == 263;
                if (flag || keyCode == 262) {
                    float f = flag ? -1.0F : 1.0F;
                    this.setValue(this.value + (double)(f / (float)(this.width - 8)));
                    return true;
                }
            }

            return false;
        }
    }

    private void setValueFromMouse(double mouseX) {
        this.setValue((mouseX - (double)(this.getX() + 4)) / (double)(this.width - 8));
    }

    private void setValue(double value) {
        double d0 = this.value;
        this.value = Mth.clamp(value, (double)0.0F, (double)1.0F);
        if (d0 != this.value) {
            this.applyValue();
        }

        this.updateMessage();
    }

    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        this.setValueFromMouse(mouseX);
        super.onDrag(mouseX, mouseY, dragX, dragY);
    }

    public void playDownSound(SoundManager handler) {
    }

    public void onRelease(double mouseX, double mouseY) {
        super.playDownSound(Minecraft.getInstance().getSoundManager());
    }

    protected abstract void updateMessage();

    protected abstract void applyValue();
}

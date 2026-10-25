package net.dumbcode.projectnublar.gui.widget;

import net.dumbcode.projectnublar.gui.NublarGuiConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class VerticalGradientBarWidget extends AbstractWidget {
    public static final ResourceLocation VALUE_BAR_SIDE = new ResourceLocation(NublarGuiConstants.MODID, "textures/gui/value_bar_side.png");
    public static final ResourceLocation VALUE_BAR_MIDDLE = new ResourceLocation(NublarGuiConstants.MODID, "textures/gui/value_bar_middle.png");
    private int topColor = 0xFFFFFFFF;
    private int bottomColor = 0xFF000000;
    private int barY;
    private float value = 1.0f;
    private OnValueChanged valueChanged;

    public VerticalGradientBarWidget(int pX, int pY, int pWidth, int pHeight, Component pMessage, OnValueChanged valueChanged) {
        super(pX, pY, pWidth, pHeight, pMessage);
        this.barY = pY;
        this.valueChanged = valueChanged;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int i1, float v) {
        guiGraphics.fillGradient(getX(), getY(), getX() + width, getY() + height, topColor, bottomColor);
        guiGraphics.blit(VALUE_BAR_SIDE, getX() - 1, barY-1, 0, 0, 1, 3, 1, 3);
        guiGraphics.blit(VALUE_BAR_MIDDLE, getX(), barY-1, 0, 0, width, 3, width, 3);
        guiGraphics.blit(VALUE_BAR_SIDE, getX() + width, barY-1, 0, 0, 1, 3, 1, 3);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        float oldValue = this.value;
        this.value = 1.0f-(float)(mouseY - getY()) / (float)height;
        this.valueChanged.onValueChanged(oldValue, this.value);
        this.barY = (int)mouseY;
    }

    @Override
    protected void onDrag(double pMouseX, double pMouseY, double pDragX, double pDragY) {
        float oldValue = this.value;
        this.value = 1.0f-Mth.clamp((float)(pMouseY - getY()) / (float)height,0.0f,1.0f);
        this.valueChanged.onValueChanged(oldValue, this.value);
        this.barY = Mth.clamp((int)pMouseY, getY(), getY() + height - 1);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
    public void setValue(float value){
        this.value = value;
        this.barY = (int)(getY() + (height * (1.0-value)));
    }

    public int getTopColor() {
        return topColor;
    }

    public void setTopColor(int topColor) {
        this.topColor = topColor;
    }

    public interface OnValueChanged {
        void onValueChanged(float oldValue, float newValue);
    }
}
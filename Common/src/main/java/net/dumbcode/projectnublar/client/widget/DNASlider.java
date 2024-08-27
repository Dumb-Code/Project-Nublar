package net.dumbcode.projectnublar.client.widget;

import com.nyfaria.nyfsguilib.client.widgets.NGLSlider;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.DNAData;
import net.dumbcode.projectnublar.client.screen.SequencerScreen;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;

public class DNASlider extends NGLSlider {
    boolean selected = false;
    int buttonWidth = 16;
    int buttonHeight = 16;
    int barHeight = 10;
    EntityType<?> entityType;
    DNAData dnaData;
    OnClick consumer = (dnaSlider, selected) -> {
    };

    public DNASlider(int x, int y, int width, int height, Component prefix, Component suffix, double minValue, double maxValue, double currentValue, double stepSize, int precision, boolean drawString, OnValueChanged consumer) {
        super(x, y, width, height, prefix, suffix, minValue, maxValue, currentValue, stepSize, precision, drawString, consumer);
    }

    public DNASlider(int x, int y, int width, int height, Component prefix, Component suffix, double minValue, double maxValue, double currentValue, boolean drawString, OnValueChanged consumer) {
        super(x, y, width, height, prefix, suffix, minValue, maxValue, currentValue, drawString, consumer);
    }

    public EntityType<?> getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType<?> entityType) {
        this.entityType = entityType;
    }

    public DNAData getDnaData() {
        return dnaData;
    }

    public double maxDNA() {
        if(dnaData == null)
            return 0;
        return dnaData.getDnaPercentage();
    }

    public void setDNAData(DNAData dnaData) {
        this.dnaData = dnaData;
    }

    public void setConsumer(OnClick consumer) {
        this.consumer = consumer;
    }
    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        if (mouseX >= getX() && mouseX <= getX() + buttonWidth && mouseY >= getY() && mouseY <= getY() + buttonHeight) {
        } else
            super.onDrag(mouseX, mouseY, dragX, dragY);

    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (mouseX >= getX() && mouseX <= getX() + buttonWidth && mouseY >= getY() && mouseY <= getY() + buttonHeight) {
            selected = !selected;
            consumer.onClick(this, selected);
        } else {
            super.onClick(mouseX, mouseY);
        }
    }



    public void setValueFromMouse(double mouseX) {
        int barStart = this.getX() + buttonWidth;
        int barWidth = this.width - 16;
        this.setSliderValue((mouseX - barStart) / barWidth);
    }


    @Override
    public void renderSliderBar(GuiGraphics guiGraphics) {
        int posX = (this.getX() + buttonWidth - 1) + (int) (this.value * (double) (this.width - buttonWidth - 9));
        SequencerScreen.drawBorder(guiGraphics, posX, this.getY(), 10, height, Constants.BORDER_COLOR, 1);
        guiGraphics.fill(posX + 1, this.getY() + 1, posX + 9, getY() + height - 1, 0xFF193B59);
    }

    protected void renderScrollingString(GuiGraphics pGuiGraphics, Font pFont, int pWidth, int pColor) {
        int i = this.getX() + pWidth + buttonWidth / 2;
        int j = this.getX() + this.getWidth() - pWidth + buttonWidth / 2;
        renderScrollingString(pGuiGraphics, pFont, this.getMessage(), i, this.getY(), j, this.getY() + this.getHeight(), pColor);
    }

    @Override
    public void renderSliderBackground(GuiGraphics guiGraphics) {
        SequencerScreen.drawBorder(guiGraphics, getX(), getY(), buttonWidth, buttonHeight, Constants.BORDER_COLOR, 1);
        guiGraphics.fill(getX() + 1, getY() + 1, getX() + buttonWidth - 1, getY() + buttonHeight - 1, selected ? Constants.BORDER_COLOR : 0xFF193B59);
        SequencerScreen.drawBorder(guiGraphics, getX() + buttonWidth, getY() + 3, width - buttonWidth, barHeight, Constants.BORDER_COLOR, 1);
        guiGraphics.fill(getX() + buttonWidth, getY() + 4, getX() + width - 1, getY() + 2 + barHeight, 0xFF193B59);
    }
    public interface OnClick {
        void onClick(DNASlider widget, boolean selected);
    }
}

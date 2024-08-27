package net.dumbcode.projectnublar.client.widget;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.client.screen.SequencerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class IsolatedDataDisplayWidget extends AbstractButton {

    private String value;
    public boolean selected = false;
    private OnClick onClick;

    public IsolatedDataDisplayWidget(int x, int y, int width, int height, String value, OnClick onClick) {
        super(x, y, width, height, Component.empty());
        this.value = value;
        this.onClick = onClick;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void onPress() {
        this.selected = !selected;
        onClick.onClick(this, selected);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int i1, float v) {
        SequencerScreen.drawBorder(guiGraphics, getX(), getY(), getWidth(), getHeight(), Constants.BORDER_COLOR, 1);
        int color = 0xFF193B59;
        if (selected) {
            color = 0xFF063B6B;
        }
        if (isHovered()) {
            color = 0xFF063B6B;
        }
        guiGraphics.fill(getX() + 1, getY() + 1, getX() + getWidth() - 1, getY() + getHeight() - 1, color);
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, value, getX() + getWidth() / 2, getY() + getHeight() / 2 - 4, -1);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    public interface OnClick {
        void onClick(IsolatedDataDisplayWidget widget, boolean selected);
    }
}

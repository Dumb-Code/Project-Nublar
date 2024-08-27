package net.dumbcode.projectnublar.client.widget;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.client.screen.SequencerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.List;

public class TextScrollBox extends AbstractWidget {
    private List<Component> text;
    private int scroll = 0;
    private boolean isBordered = false;
    private int backgroundColor = -1;
    private int borderThickness = 1;
    private int borderColor = Constants.BORDER_COLOR;

    public TextScrollBox(int x, int y, int width, int height, List<Component> text) {
        super(x, y, width, height, Component.empty());
        this.text = text;
    }

    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
    }

    public void setBorder(boolean isBordered, int color, int thickness) {
        this.isBordered = isBordered;
        this.borderColor = color;
        this.borderThickness = thickness;
    }

    public void setText(List<Component> text) {
        this.text = text;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int i1, float v) {

        if (isBordered) {
            SequencerScreen.drawBorder(guiGraphics, getX(), getY(), getWidth(), getHeight(), borderColor, borderThickness);
        }
        guiGraphics.enableScissor(getX() + (isBordered ? 1 : 0), getY() + (isBordered ? 1 : 0), getX() + getWidth() - (isBordered ? 1 : 0), getY() + getHeight() - (isBordered ? 1 : 0));
        if (backgroundColor != -1) {
            guiGraphics.fill(getX()+1, getY()+1, getX() + getWidth()-1, getY() + getHeight()-1, backgroundColor);
        }
        int y = getY() + 3;
        for (Component component : text) {
            guiGraphics.drawString(Minecraft.getInstance().font, component, getX() + 3, y + scroll, 0xFFFFFFFF);
            y += 10;
        }
        guiGraphics.disableScissor();
    }

    @Override
    public boolean mouseScrolled(double $$0, double $$1, double $$2) {
        int maxScroll = text.size() * -10 + getHeight() - 2;
        scroll = Math.min(0, Math.max(maxScroll, scroll + (int) $$2));
        return super.mouseScrolled($$0, $$1, $$2);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}

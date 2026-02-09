package net.dumbcode.projectnublar.client.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ScrollingButtonListWidget<T extends GuiEventListener> extends ParentWidget<T> {
    public int currentY = 0;
    public int scroll = 0;
    private List<AbstractButton> buttons = new ArrayList<>();

    public ScrollingButtonListWidget(@Nullable T parent, int pX, int pY, int pWidth, int pHeight, Component pMessage) {
        super(parent, pX, pY, pWidth, pHeight, pMessage);
    }

    public void clearButtons() {
        buttons.forEach(this::removeWidget);
        buttons.clear();
        currentY = 0;
    }

    public List<AbstractButton> getButtons(){
        return buttons;
    }
    public int size(){
        return buttons.size();
    }
    public void addButton(AbstractButton button) {
        addButton(button, false);
    }

    public void addButton(AbstractButton button, boolean matchWidth) {
        addButton(button, matchWidth, 2);
    }
    public void addButton(AbstractButton button, boolean matchWidth, int padding) {
        button.setX(this.getX() + padding);
        button.setY(this.getY() + currentY + scroll);
        if (matchWidth)
            button.setWidth(this.width - padding * 2);
        buttons.add(button);
        currentY += button.getHeight() + 1;
        this.addRenderableWidget(button);
    }

    @Override
    public boolean doesScissor() {
        return true;
    }

    @Override
    public void init(boolean rebuild) {

    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        int change = (int) (pDelta * 10);
        if (scroll + change <= 0 && scroll + change >= -((buttons.stream().count() - 10) * 21)) {
            buttons.forEach(button -> button.setY(button.getY() + (int) (pDelta * 10)));
            scroll += (int) (pDelta * 10);
        }
        return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    @Override
    protected void renderBackground(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {

    }

    @Override
    protected void renderForeground(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {

    }
}

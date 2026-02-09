package net.dumbcode.projectnublar.client.widget;


import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class DataDisplayWidget extends ParentWidget<AbstractWidget> {

    public DataDisplayWidget(@Nullable AbstractWidget parent, int pX, int pY, int pWidth, int pHeight, Component pMessage) {
        super(parent, pX, pY, pWidth, pHeight, pMessage);
    }

    @Override
    public void init(boolean rebuild) {

    }

    @Override
    protected void renderBackground(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {

    }

    @Override
    protected void renderForeground(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {

    }
}

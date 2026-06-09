package net.dumbcode.projectnublar.gui.widget;

import net.minecraft.client.gui.GuiGraphics;

public interface TooltipRenderer {
    void renderTooltip(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick);
}

package net.dumbcode.projectnublar.gui.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface TooltipRenderer {
    void renderTooltip(GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick);
}

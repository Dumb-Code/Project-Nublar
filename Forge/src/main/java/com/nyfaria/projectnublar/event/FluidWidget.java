package com.nyfaria.projectnublar.event;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class FluidWidget extends AbstractWidget {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/block/water_flow.png");

    public FluidWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int i1, float v) {
        RenderSystem.setShaderColor(0, 0, 1, 1);
        guiGraphics.blit(TEXTURE, getX() + 1, getY() + 1,width-2,height-2,0,0,32, 1024, 32, 1024);
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}

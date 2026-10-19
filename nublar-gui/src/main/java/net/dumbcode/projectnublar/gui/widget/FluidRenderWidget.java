package net.dumbcode.projectnublar.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class FluidRenderWidget extends AbstractWidget {


    private static final ResourceLocation FLOW_TEXTURE = new ResourceLocation("textures/block/water_flow.png");
    private static final ResourceLocation STILL_TEXTURE = new ResourceLocation("textures/block/water_still.png");

    private ResourceLocation foreground;
    private float progress = 0;
    private int color;
    private boolean flowDown;
    private boolean flow;

    public FluidRenderWidget(int x, int y, int width, int height, ResourceLocation foreground, int color, boolean flow, boolean flowDown) {
        super(x, y, width, height, Component.empty());
        this.foreground = foreground;
        this.color = color;
        this.flow = flow;
        this.flowDown = flowDown;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int i1, float v) {
        RenderSystem.setShaderColor((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F, (color >> 24 & 255) / 255.0F);
        long tick = Minecraft.getInstance().level.getGameTime();
        int offset = (int) (tick % 32) * (flowDown ? -1 : 1);
        guiGraphics.enableScissor(getX() + 1, getY() + 1 + ((height - 1) - (int) ((height - 1) * getProgress())), getX() + width - 1, getY() + height - 1);
        guiGraphics.blit(flow ? FLOW_TEXTURE : STILL_TEXTURE, getX() + 1, getY() + 1, width - 2, height - 2, 0, offset * 32, 32, 1024, 32, 1024);
        guiGraphics.disableScissor();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        if(foreground != null) {
            guiGraphics.blit(foreground, getX(), getY(), 0, 0, width, height, width, height);
        }
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}

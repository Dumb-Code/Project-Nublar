package net.dumbcode.projectnublar.gui.widget;


import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;

import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.Supplier;

public class ProgressWidget extends AbstractWidget {
    private final Identifier background;
    private final Identifier foreground;
    private final boolean horizontal;
    private int color;
    public Supplier<Float> progress;
    private boolean reverse;

    public ProgressWidget(int x, int y, int width, int height, Identifier foreground, boolean horizontal, boolean reverse) {
        this(x, y, width, height, null, foreground, -1, horizontal, reverse);
    }

    public ProgressWidget(int x, int y, int width, int height, Identifier foreground, Supplier<Float> progress, boolean horizontal, boolean reverse) {
        this(x, y, width, height, null, foreground, -1, progress, horizontal, reverse);
    }

    public ProgressWidget(int x, int y, int width, int height, Identifier background, Identifier foreground, int color, boolean horizontal, boolean reverse) {
        this(x, y, width, height, background, foreground, color, () -> 0.0F, horizontal, reverse);
    }

    public ProgressWidget(int x, int y, int width, int height, Identifier background, Identifier foreground, Supplier<Float> progress, boolean horizontal, boolean reverse) {
        this(x, y, width, height, background, foreground, -1, progress, horizontal, reverse);
    }

    public ProgressWidget(int x, int y, int width, int height, Identifier background, Identifier foreground, int color, Supplier<Float> progress, boolean horizontal, boolean reverse) {
        super(x, y, width, height, Component.empty());
        this.background = background;
        this.foreground = foreground;
        this.color = color;
        this.progress = progress;
        this.horizontal = horizontal;
        this.reverse = reverse;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int i, int i1, float v) {
        if (background != null) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, background, getX(), getY(), 0, 0, getWidth(), getHeight(), getWidth(), getHeight());
        }
        // The colour tints the foreground blits only; the default -1 is opaque white, which is no tint.
        if (horizontal) {
            int progressWidth = (int) (getWidth() * progress.get());
            if(!reverse) {
                guiGraphics.enableScissor(getX() + 1, getY() + 1, getX() + 1 + (int) (getWidth() * getProgress().get()), getY() + getHeight() - 1);
            } else {
                guiGraphics.enableScissor(getX() + 1 + ((width - 1) - (int) ((width - 1) * getProgress().get())), getY() + 1, getX() + width - 1, getY() + height - 1);
            }
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, foreground, getX(), getY(), 0, 0, getWidth(), getHeight(), getWidth(), getHeight(), color);
        } else {
            int progressHeight = (int) (getHeight() * progress.get());
            if(!reverse) {
                guiGraphics.enableScissor(getX() + 1, getY() + 1 + ((height - 1) - (int) ((height - 1) * getProgress().get())), getX() + width - 1, getY() + height - 1);
            } else {
                guiGraphics.enableScissor(getX() + 1, getY() + 1, getX() + width - 1, getY() + 1 + ((height - 1) - (int) ((height - 1) * getProgress().get())));
            }
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, foreground, getX(), getY() + getHeight(), 0, getHeight(), getWidth(), getHeight(), getWidth(), getHeight(), color);
        }
        guiGraphics.disableScissor();
    }

    public Supplier<Float> getProgress() {
        return progress;
    }

    public void setProgress(Supplier<Float> progress) {
        this.progress = progress;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}

package net.dumbcode.projectnublar.client.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class ProgressWidget extends AbstractWidget {
    private final ResourceLocation background;
    private final ResourceLocation foreground;
    private final boolean horizontal;
    private int color;
    public Supplier<Float> progress;
    private boolean reverse;

    public ProgressWidget(int x, int y, int width, int height, ResourceLocation foreground, boolean horizontal, boolean reverse) {
        this(x, y, width, height, null, foreground, -1, horizontal, reverse);
    }

    public ProgressWidget(int x, int y, int width, int height, ResourceLocation foreground, Supplier<Float> progress, boolean horizontal, boolean reverse) {
        this(x, y, width, height, null, foreground, -1, progress, horizontal, reverse);
    }

    public ProgressWidget(int x, int y, int width, int height, ResourceLocation background, ResourceLocation foreground, int color, boolean horizontal, boolean reverse) {
        this(x, y, width, height, background, foreground, color, () -> 0.0F, horizontal, reverse);
    }

    public ProgressWidget(int x, int y, int width, int height, ResourceLocation background, ResourceLocation foreground, Supplier<Float> progress, boolean horizontal, boolean reverse) {
        this(x, y, width, height, background, foreground, -1, progress, horizontal, reverse);
    }

    public ProgressWidget(int x, int y, int width, int height, ResourceLocation background, ResourceLocation foreground, int color, Supplier<Float> progress, boolean horizontal, boolean reverse) {
        super(x, y, width, height, Component.empty());
        this.background = background;
        this.foreground = foreground;
        this.color = color;
        this.progress = progress;
        this.horizontal = horizontal;
        this.reverse = reverse;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int i1, float v) {
        if (background != null) {
            guiGraphics.blit(background, getX(), getY(), 0, 0, getWidth(), getHeight(), getWidth(), getHeight());
        }
        if (color != -1) {
            guiGraphics.setColor((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F, (color >> 24 & 255) / 255.0F);
        }
        if (horizontal) {
            int progressWidth = (int) (getWidth() * progress.get());
            if(!reverse) {
                guiGraphics.enableScissor(getX() + 1, getY() + 1, getX() + 1 + (int) (getWidth() * getProgress().get()), getY() + getHeight() - 1);
            } else {
                guiGraphics.enableScissor(getX() + 1 + ((width - 1) - (int) ((width - 1) * getProgress().get())), getY() + 1, getX() + width - 1, getY() + height - 1);
            }
            guiGraphics.blit(foreground, getX(), getY(), 0, 0, getWidth(), getHeight(), getWidth(), getHeight());
        } else {
            int progressHeight = (int) (getHeight() * progress.get());
            if(!reverse) {
                guiGraphics.enableScissor(getX() + 1, getY() + 1 + ((height - 1) - (int) ((height - 1) * getProgress().get())), getX() + width - 1, getY() + height - 1);
            } else {
                guiGraphics.enableScissor(getX() + 1, getY() + 1, getX() + width - 1, getY() + 1 + ((height - 1) - (int) ((height - 1) * getProgress().get())));
            }
            guiGraphics.blit(foreground, getX(), getY() + getHeight(), 0, getHeight(), getWidth(), getHeight(), getWidth(), getHeight());
        }
        if (color != -1) {
            guiGraphics.setColor(1, 1, 1, 1);
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

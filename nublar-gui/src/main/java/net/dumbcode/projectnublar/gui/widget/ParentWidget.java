package net.dumbcode.projectnublar.gui.widget;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class ParentWidget<T extends GuiEventListener> extends AbstractWidget implements ContainerEventHandler {

    private final List<GuiEventListener> children = Lists.newArrayList();
    private final List<NarratableEntry> narratables = Lists.newArrayList();
    public final List<Renderable> renderables = Lists.newArrayList();
    public final List<Renderable> renderableNoScissor = Lists.newArrayList();
    @javax.annotation.Nullable
    private GuiEventListener focused;
    private boolean isDragging;
    public T parent;
    private int scissorsTopYOffset = 0;
    private int scissorsBottomYOffset = 0;
    private int scissorsLeftXOffset = 0;
    private int scissorsRightXOffset = 0;


    public ParentWidget(@Nullable T parent, int pX, int pY, int pWidth, int pHeight, Component pMessage) {
        super(pX, pY, pWidth, pHeight, pMessage);
        this.parent = parent;
        init(false);
    }

    public boolean doesScissor() {
        return false;
    }

    public abstract void init(boolean rebuild);

    protected abstract void renderBackground(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick);

    protected abstract void renderForeground(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick);

    @Override
    protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (doesScissor()) {
//            pGuiGraphics.enableScissor(getX(), getY() + getScissorsTopYOffset(), getX() + this.width, getY() + this.height);
            pGuiGraphics.enableScissor(getX() + getScissorsLeftXOffset(), getY() + getScissorsTopYOffset(), getX() + this.width - getScissorsRightXOffset(), getY() + this.height - getScissorsBottomYOffset());
        }
        this.renderBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        for (Renderable renderable : this.renderables) {
            renderable.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        }
        this.renderForeground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        if (doesScissor())
            pGuiGraphics.disableScissor();
        this.renderAfterScissor(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    public int getScissorsTopYOffset() {
        return scissorsTopYOffset;
    }
    public int getScissorsBottomYOffset() {
        return scissorsBottomYOffset;
    }
    public int getScissorsLeftXOffset() {
        return scissorsLeftXOffset;
    }
    public int getScissorsRightXOffset() {
        return scissorsRightXOffset;
    }

    public void setScissorsTopYOffset(int scissorsTopYOffset) {
        this.scissorsTopYOffset = scissorsTopYOffset;
    }

    public void renderAfterScissor(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        for (Renderable renderable : this.renderableNoScissor) {
            renderable.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        }
        for(GuiEventListener renderable : this.children){
            if(renderable instanceof TooltipRenderer && pMouseX >= getX() && pMouseX <= getX() + width && pMouseY >= getY() && pMouseY <= getY() + height){
                ((TooltipRenderer) renderable).renderTooltip(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            }
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    protected <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableNoScissorWidget(T pWidget) {
        this.renderableNoScissor.add(pWidget);
        return this.addWidget(pWidget);
    }
    protected <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T pWidget) {
        this.renderables.add(pWidget);
        return this.addWidget(pWidget);
    }

    protected <T extends GuiEventListener & NarratableEntry> T addWidget(T pListener) {
        this.children.add(pListener);
        this.narratables.add(pListener);
        return pListener;
    }

    protected <T extends Renderable> T addRenderableOnly(T pRenderable) {
        this.renderables.add(pRenderable);
        return pRenderable;
    }

    protected void removeWidget(GuiEventListener pListener) {
        if (pListener instanceof Renderable) {
            this.renderables.remove((Renderable) pListener);
        }

        if (pListener instanceof NarratableEntry) {
            this.narratables.remove((NarratableEntry) pListener);
        }

        this.children.remove(pListener);
    }

    protected void clearWidgets() {
        this.renderables.clear();
        this.renderableNoScissor.clear();
        this.children.clear();
        this.narratables.clear();
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return this.children;
    }

    @Override
    public final boolean isDragging() {
        return this.isDragging;
    }

    public final void setDragging(boolean pDragging) {
        this.isDragging = pDragging;
    }

    @Nullable
    @Override
    public GuiEventListener getFocused() {
        return focused;
    }

    @Override
    public void setFocused(@javax.annotation.Nullable GuiEventListener pListener) {
        if (this.focused != null) {
            this.focused.setFocused(false);
        }

        if (pListener != null) {
            pListener.setFocused(true);
        }

        this.focused = pListener;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (this.active && this.visible) {
            if(this.isMouseOver(pMouseX, pMouseY)){
                for (GuiEventListener guieventlistener : this.children()) {
                    if (guieventlistener.mouseClicked(pMouseX, pMouseY, pButton)) {
                        this.setFocused(guieventlistener);
                        if (pButton == 0) {
                            this.setDragging(true);
                        }

                        return true;
                    }
                }
            }
        }

        return false;
    }



    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        this.setDragging(false);
        return this.getChildAt(pMouseX, pMouseY).filter((p_94708_) -> {
            return p_94708_.mouseReleased(pMouseX, pMouseY, pButton);
        }).isPresent();
    }

    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        return this.getFocused() != null && this.isDragging() && pButton == 0 ? this.getFocused().mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY) : false;
    }

    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        return this.getChildAt(pMouseX, pMouseY).filter((p_94693_) -> {
            return p_94693_.mouseScrolled(pMouseX, pMouseY, pDelta);
        }).isPresent();
    }

    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        return this.getFocused() != null && this.getFocused().keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        return this.getFocused() != null && this.getFocused().keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    public boolean charTyped(char pCodePoint, int pModifiers) {
        return this.getFocused() != null && this.getFocused().charTyped(pCodePoint, pModifiers);
    }

    public void rebuild() {
        this.children.clear();
        this.narratables.clear();
        this.renderables.clear();
        this.renderableNoScissor.clear();
        this.init(true);
    }
}

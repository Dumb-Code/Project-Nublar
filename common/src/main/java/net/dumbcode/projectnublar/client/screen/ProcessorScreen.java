package net.dumbcode.projectnublar.client.screen;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.gui.widget.FluidRenderWidget;
import net.dumbcode.projectnublar.menutypes.ProcessorMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.player.Inventory;

public class ProcessorScreen extends AbstractContainerScreen<ProcessorMenu> {
    private static final Identifier FOREGROUND = Constants.modLoc( "textures/gui/fluid_overlay.png");
    private static Identifier TEXTURE = Constants.modLoc( "textures/gui/processor.png");
    private FluidRenderWidget fluidWidget;

    public ProcessorScreen(ProcessorMenu processorMenu, Inventory inventory, Component component) {
        super(processorMenu, inventory, component, 176, 222);
        inventoryLabelY = -82;
    }



    @Override
    protected void init() {
        super.init();
        fluidWidget = new FluidRenderWidget(leftPos + 21, topPos + 60, 18,46, FOREGROUND, 0xFF3F76E4, true, false);
        addRenderableWidget(fluidWidget);
    }

    /** The dimmed background (super) and the processor. */
    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int i, int i1, float v) {
        super.extractBackground(guiGraphics, i, i1, v);
        int x = this.leftPos;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,x, y, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    /** The progress arrow and its time tooltip go after the slots and under the carried item, as 1.20.1 drew them after its container pass. */
    @Override
    public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractContents(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,leftPos + 82, topPos + 40, 176, 49, 11,  Mth.ceil(19 *((float)menu.getDataSlot(2) / menu.getDataSlot(3))), 256, 256);
        if(mouseX > leftPos + 80 && mouseX < leftPos + 80 + 15 && mouseY > topPos + 39 && mouseY < topPos + 39 + 22){
            guiGraphics.setTooltipForNextFrame(font, Component.literal(StringUtil.formatTickDuration(menu.getDataSlot(3)-menu.getDataSlot(2), SharedConstants.TICKS_PER_SECOND)), mouseX, mouseY);
        }
        fluidWidget.setTooltip(Tooltip.create(Component.literal(menu.getDataSlot(0) + "/" + menu.getDataSlot(1)+"mB").withStyle(ChatFormatting.GRAY)));
    }

    @Override
    protected void containerTick() {
        float progress = (float) menu.getDataSlot(0) / menu.getDataSlot(1);
        fluidWidget.setProgress(progress);
    }
}

package net.dumbcode.projectnublar.client.screen;

import com.nyfaria.nyfsguilib.client.widgets.FluidRenderWidget;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.menutypes.ProcessorMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.player.Inventory;

public class ProcessorScreen extends AbstractContainerScreen<ProcessorMenu> {
    private static final ResourceLocation FOREGROUND = Constants.modLoc( "textures/gui/fluid_overlay.png");
    private static ResourceLocation TEXTURE = Constants.modLoc( "textures/gui/processor.png");
    private FluidRenderWidget fluidWidget;

    public ProcessorScreen(ProcessorMenu processorMenu, Inventory inventory, Component component) {
        super(processorMenu, inventory, component);
        inventoryLabelY = -82;
        imageHeight = 222;
        imageWidth = 176;
    }



    @Override
    protected void init() {
        super.init();
        fluidWidget = new FluidRenderWidget(leftPos + 21, topPos + 60, 18,46, FOREGROUND, 0xFF3F76E4, true, false);
        addRenderableWidget(fluidWidget);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        guiGraphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        int x = this.leftPos;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE,x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.blit(TEXTURE,leftPos + 82, topPos + 40, 176, 49, 11,  Mth.ceil(19 *((float)menu.getDataSlot(2) / menu.getDataSlot(3))));
        if(mouseX > leftPos + 80 && mouseX < leftPos + 80 + 15 && mouseY > topPos + 39 && mouseY < topPos + 39 + 22){
            guiGraphics.renderTooltip(font, Component.literal(StringUtil.formatTickDuration(menu.getDataSlot(3)-menu.getDataSlot(2))), mouseX, mouseY);
        }
        fluidWidget.setTooltip(Tooltip.create(Component.literal(menu.getDataSlot(0) + "/" + menu.getDataSlot(1)+"mB").withStyle(ChatFormatting.GRAY)));
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void containerTick() {
        float progress = (float) menu.getDataSlot(0) / menu.getDataSlot(1);
        fluidWidget.setProgress(progress);
    }
}

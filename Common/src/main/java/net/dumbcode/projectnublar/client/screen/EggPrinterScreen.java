package net.dumbcode.projectnublar.client.screen;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.menutypes.EggPrinterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class EggPrinterScreen extends AbstractContainerScreen<EggPrinterMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Constants.MODID, "textures/gui/egg_printer.png");
    private static final ResourceLocation BONEMEAL_BAR = new ResourceLocation(Constants.MODID, "textures/gui/bonemeal_bar.png");
    private static final ResourceLocation EGG = new ResourceLocation(Constants.MODID, "textures/gui/egg.png");

    public EggPrinterScreen(EggPrinterMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.height = 230;
        this.width = 176;
        this.imageHeight = 230;
        this.imageWidth = 176;
        this.inventoryLabelY = -1000;
        this.titleLabelY = -1000;
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        pGuiGraphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        int x = this.leftPos;
        int y = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(TEXTURE,x, y, 0, 0, imageWidth, imageHeight,imageWidth,imageHeight);
        float bmProgress = menu.getDataSlot(0) / 30f;
        int bmImageHeight = Mth.floor(54 * bmProgress);
        pGuiGraphics.blit(BONEMEAL_BAR, x + 16, y + 17 + 54 - bmImageHeight, 0, 54-bmImageHeight, 14, bmImageHeight, 14, 54);
        float eggProgress = menu.getDataSlot(2) / (float)menu.getDataSlot(3);
        int eggImageHeight = Mth.floor(66 * eggProgress);
        pGuiGraphics.blit(EGG, x + 65, y + 28 + 66 - eggImageHeight, 0, 66-eggImageHeight, 43, eggImageHeight, 43, 66);

    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }
}

package net.dumbcode.projectnublar.client.screen;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.menutypes.EggPrinterMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class EggPrinterScreen extends AbstractContainerScreen<EggPrinterMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Constants.MODID,"textures/gui/egg_printer.png");
    private static final Identifier BONEMEAL_BAR = Identifier.fromNamespaceAndPath(Constants.MODID,"textures/gui/bonemeal_bar.png");
    private static final Identifier EGG = Identifier.fromNamespaceAndPath(Constants.MODID,"textures/gui/egg.png");

    public EggPrinterScreen(EggPrinterMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, 176, 230);
        this.height = 230;
        this.width = 176;
        this.inventoryLabelY = -1000;
        this.titleLabelY = -1000;
    }

    /** The dimmed background (super), the printer, the bone-meal bar and the egg progress. */
    @Override
    public void extractBackground(GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.extractBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        int x = this.leftPos;
        int y = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,x, y, 0, 0, imageWidth, imageHeight,imageWidth,imageHeight);
        float bmProgress = menu.getDataSlot(0) / 30f;
        int bmImageHeight = Mth.floor(54 * bmProgress);
        pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, BONEMEAL_BAR, x + 16, y + 17 + 54 - bmImageHeight, 0, 54-bmImageHeight, 14, bmImageHeight, 14, 54);
        float eggProgress = menu.getDataSlot(2) / (float)menu.getDataSlot(3);
        int eggImageHeight = Mth.floor(66 * eggProgress);
        pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, EGG, x + 65, y + 28 + 66 - eggImageHeight, 0, 66-eggImageHeight, 43, eggImageHeight, 43, 66);

    }
}

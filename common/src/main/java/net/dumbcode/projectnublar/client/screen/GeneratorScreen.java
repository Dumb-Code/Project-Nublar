package net.dumbcode.projectnublar.client.screen;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.menutypes.GeneratorMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GeneratorScreen extends AbstractContainerScreen<GeneratorMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Constants.MODID,"textures/gui/coal_generator.png");
    public GeneratorScreen(GeneratorMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, 176, 166);
        this.width = 176;
        this.height = 166;
    }

    /** The generator screen has no dimmed background (1.20.1 drew none), so the world stays undimmed behind it. */
    @Override
    public void extractTransparentBackground(GuiGraphicsExtractor pGuiGraphics) {
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.extractBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        int x = this.leftPos;
        int y = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,x, y, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    /** The energy text goes after the slots and under the carried item, as 1.20.1 drew it after its container pass. */
    @Override
    public void extractContents(GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.extractContents(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        pGuiGraphics.centeredText(Minecraft.getInstance().font, menu.getData(0)+ "/" + menu.getData(1) + " FE", this.width / 2, this.topPos +64, 0xFFFFFFFF);
    }
}

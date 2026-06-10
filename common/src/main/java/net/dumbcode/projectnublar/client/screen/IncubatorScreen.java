package net.dumbcode.projectnublar.client.screen;

import commonnetwork.api.Network;
import java.util.List;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.menutypes.IncubatorMenu;
import net.dumbcode.projectnublar.network.c2s.UpdateIncubatorSlotPacket;
import net.dumbcode.projectnublar.registry.ItemInit;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

/**
 * Screen for the incubator. Eggs live in freely-positionable slots: clicking or closing the
 * screen sends {@link UpdateIncubatorSlotPacket} with the menu slot index (the block
 * entity subtracts 1 - frozen off-by-design handshake, see {@code IncubatorBlockEntity}).
 *
 * <p>TODO(DEAD): an "incubator_bed" shader overlay was started and abandoned here (removed
 * commented-out code); the bed is drawn with a plain texture blit instead.
 */
public class IncubatorScreen extends AbstractContainerScreen<IncubatorMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Constants.MODID, "textures/gui/incubator.png");

    public static final int BED_WIDTH = 158;
    public static final int BED_HEIGHT = 115;

    public IncubatorScreen(IncubatorMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 176;
        this.imageHeight = 222;
        this.titleLabelY = -100;
        this.inventoryLabelY = -100;
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        pGuiGraphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        int x = this.leftPos;
        int y = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 334, 222);
        pGuiGraphics.blit(TEXTURE, x + 9, y + 9, imageWidth, 0, BED_WIDTH, BED_HEIGHT, 334, 222);
        int plantmatterMax = this.menu.getData().get(1);
        int plantmatter = this.menu.getData().get(0);
        int progress = Mth.floor(((plantmatter / (float) plantmatterMax)) * 63);
        pGuiGraphics.fill(x + 28, y + 121, x + 28 + progress, y + 121 + 4, 0xFFA9E245);
    }

    @Override
    protected void slotClicked(Slot pSlot, int pSlotId, int pMouseButton, ClickType pType) {
        super.slotClicked(pSlot, pSlotId, pMouseButton, pType);
        if (pSlot instanceof IncubatorMenu.IncubatorSlot) {
            if (pType == ClickType.PICKUP) {
                int x = pSlot.x;
                int y = pSlot.y;
                if (pSlot.hasItem()) {
                    x = 0;
                    y = -100;
                }
                // pSlotId is the menu slot index; the block entity subtracts 1
                Network.getNetworkHandler().sendToServer(new UpdateIncubatorSlotPacket(menu.getPos(), pSlotId, x, y));
            }
        }
    }

    @Override
    protected boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
        boolean hover = super.isHovering(pX, pY, pWidth, pHeight, pMouseX, pMouseY);
        if (menu.getCarried().is(ItemInit.UNINCUBATED_EGG.get())) {
            if (!hover && pMouseX > this.leftPos + 17 && pMouseX < this.leftPos + 17 + 150 && pMouseY > this.topPos + 17 && pMouseY < this.topPos + 17 + 88) {
                List<Slot> slots = this.menu.slots;
                Slot slot = slots.stream().filter(s -> !s.hasItem() && s instanceof IncubatorMenu.IncubatorSlot).findFirst().orElse(null);
                if (slot != null) {
                    slot.x = Mth.floor(pMouseX - leftPos - 8);
                    slot.y = Mth.floor(pMouseY - topPos - 8);
                }
            }
        }
        return hover;
    }

    @Override
    public void onClose() {
        super.onClose();
        menu.slots.forEach(
                slot -> {
                    if (slot instanceof IncubatorMenu.IncubatorSlot)
                        Network.getNetworkHandler().sendToServer(new UpdateIncubatorSlotPacket(menu.getPos(), menu.slots.indexOf(slot), slot.x, slot.y));
                }
        );
    }
}

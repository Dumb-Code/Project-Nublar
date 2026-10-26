package net.dumbcode.projectnublar.client.screen;

import java.util.List;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.block.entity.IncubatorBlockEntity;
import net.dumbcode.projectnublar.menutypes.IncubatorMenu;
import net.dumbcode.projectnublar.network.c2s.UpdateIncubatorSlotPacket;
import net.dumbcode.projectnublar.platform.Services;
import net.dumbcode.projectnublar.registry.ItemInit;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
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
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Constants.MODID,"textures/gui/incubator.png");

    public IncubatorScreen(IncubatorMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, 176, 222);
        this.titleLabelY = -100;
        this.inventoryLabelY = -100;
    }

    /** Gives the menu its egg and inventory slots from the client level's incubator ({@link IncubatorMenu}). */
    @Override
    protected void init() {
        super.init();
        menu.addEggAndInventorySlots((IncubatorBlockEntity) this.minecraft.level.getBlockEntity(menu.pos()));
    }

    /** The dimmed background (super), the incubator, the egg bed and the plant-matter bar. */
    @Override
    public void extractBackground(GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.extractBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        int x = this.leftPos;
        int y = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 334, 222);
        pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + IncubatorMenu.BED_OFFSET, y + IncubatorMenu.BED_OFFSET, imageWidth, 0, IncubatorMenu.BED_WIDTH, IncubatorMenu.BED_HEIGHT, 334, 222);
        int plantmatterMax = this.menu.getData().get(1);
        int plantmatter = this.menu.getData().get(0);
        int progress = Mth.floor(((plantmatter / (float) plantmatterMax)) * 63);
        pGuiGraphics.fill(x + 28, y + 121, x + 28 + progress, y + 121 + 4, 0xFFA9E245);
    }

    @Override
    protected void slotClicked(Slot pSlot, int pSlotId, int pMouseButton, ContainerInput pType) {
        super.slotClicked(pSlot, pSlotId, pMouseButton, pType);
        if (pSlot instanceof IncubatorMenu.IncubatorSlot) {
            if (pType == ContainerInput.PICKUP) {
                int x = pSlot.x;
                int y = pSlot.y;
                if (pSlot.hasItem()) {
                    x = 0;
                    y = -100;
                }
                // pSlotId is the menu slot index; the block entity subtracts 1
                Services.NETWORK.sendToServer(new UpdateIncubatorSlotPacket(menu.pos(), pSlotId, x, y));
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
        // Send the slot positions before super.onClose(): it sends the container close packet, the
        // server then closes the menu, and the slot packet's open-menu check would refuse them all.
        menu.slots.forEach(
                slot -> {
                    if (slot instanceof IncubatorMenu.IncubatorSlot)
                        Services.NETWORK.sendToServer(new UpdateIncubatorSlotPacket(menu.pos(), menu.slots.indexOf(slot), slot.x, slot.y));
                }
        );
        super.onClose();
    }
}

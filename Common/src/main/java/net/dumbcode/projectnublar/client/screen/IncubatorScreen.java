package net.dumbcode.projectnublar.client.screen;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import commonnetwork.api.Network;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.init.ItemInit;
import net.dumbcode.projectnublar.menutypes.IncubatorMenu;
import net.dumbcode.projectnublar.network.c2s.UpdateIncubatorSlotPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

import java.io.IOException;
import java.util.List;

public class IncubatorScreen extends AbstractContainerScreen<IncubatorMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Constants.MODID, "textures/gui/incubator.png");

    private static ShaderInstance shaderManager;
    private static final int TEXTURE_WIDTH = 334;
    private static final int TEXTURE_HEIGHT = 222;

    private static final int OVERLAY_START_X = 9;
    private static final int OVERLAY_START_Y = 9;

    public static final int BED_WIDTH = 158;
    public static final int BED_HEIGHT = 115;

    public IncubatorScreen(IncubatorMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 176;
        this.imageHeight = 222;
        this.titleLabelY = -100;
        this.inventoryLabelY = -100;
//        if (shaderManager == null) {
//            try {
//                shaderManager = new ShaderInstance(Minecraft.getInstance().getResourceManager(), Constants.MODID + ":incubator_bed", DefaultVertexFormat.BLIT_SCREEN);
//            } catch (IOException e) {
//                Constants.LOG.debug(e.getMessage());
//            }
//        }
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
//
//        float progress = this.menu.getData().get(0) / (float)this.menu.getData().get(1);
//
//        RenderSystem.enableBlend();
////        RenderSystem.enableAlphaTest();
//        shaderManager.safeGetUniform("progress").set(progress);
//        shaderManager.safeGetUniform("seed").set(this.menu.getPos().asLong());
//        shaderManager.apply();
//
//        int left = this.leftPos + OVERLAY_START_X;
//        int top = this.topPos + OVERLAY_START_Y;
//        int right = left + BED_WIDTH;
//        int bottom = top + BED_HEIGHT;
//
////        RenderSystem.setShaderTexture(0, new ResourceLocation("textures/block/stone.png"));
//        BufferBuilder buff = Tesselator.getInstance().getBuilder();
//        buff.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
//        buff.vertex(left, top, 0).uv(0, 0).endVertex();
//        buff.vertex(left, bottom, 0).uv(0, 1).endVertex();
//        buff.vertex(right, bottom, 0).uv(1, 1).endVertex();
//        buff.vertex(right, top, 0).uv(1, 0).endVertex();
//
//        Tesselator.getInstance().end();
//        shaderManager.clear();
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

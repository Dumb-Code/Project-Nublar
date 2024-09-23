package net.dumbcode.projectnublar.client.widget;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.DNAData;
import net.dumbcode.projectnublar.client.screen.SequencerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;

public class EntityWidget extends AbstractButton {
    private EntityType<?> entityType;
    private boolean selected = false;
    private EntityWidget.OnClick onClick;

    public EntityWidget(int x, int y, int width, int height, DNAData data, EntityWidget.OnClick onClick) {
        super(x, y, width, height, data.getFormattedType());
        this.entityType = data.getEntityType();
        this.onClick = onClick;
    }

    public EntityType<?> getEntityType() {
        return entityType;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setEntityType(EntityType<?> entityType) {
        this.entityType = entityType;
        setMessage(entityType.getDescription());
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int x, int y, float partialTicks) {
        SequencerScreen.drawBorder(guiGraphics, getX(), getY(), getWidth(), getHeight(), Constants.BORDER_COLOR, 1);
        if (selected) {
            guiGraphics.fill(getX() + 1, getY() + 1, getX() + getWidth() - 1, getY() + getHeight() - 1, 0xFF063B6B);
        } else {
            guiGraphics.fill(getX() + 1, getY() + 1, getX() + getWidth() - 1, getY() + getHeight() - 1, 0xFF193B59);
        }
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, getMessage(), getX() + getWidth() / 2, getY() + height / 2 - Minecraft.getInstance().font.lineHeight / 2 , 0xFFFFFFFF);
    }

    @Override
    public void onPress() {
        this.selected = !selected;
        onClick.onClick(this, selected);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }


    public interface OnClick {
        void onClick(EntityWidget widget, boolean selected);
    }
}

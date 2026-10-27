package net.dumbcode.projectnublar.client.widget;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.dinosaur.DNAData;
import net.dumbcode.projectnublar.api.dinosaur.DiskStorage;
import net.dumbcode.projectnublar.client.screen.SequencerScreen;
import net.dumbcode.projectnublar.registry.DataComponentInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class SequenceDataDisplayWidget extends AbstractButton {

    private Supplier<ItemStack> stack;
    private String value;
    public boolean selected = false;
    private OnClick onClick;

    public SequenceDataDisplayWidget(int x, int y, int width, int height, Supplier<ItemStack> stack, String value, OnClick onClick) {
        super(x, y, width, height, Component.empty());
        this.stack = stack;
        this.value = value;
        this.onClick = onClick;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        this.selected = !selected;
        onClick.onClick(this, selected);
    }

    /** Keeps the arrow cursor on hover, as on 1.20.1; 26.2's AbstractButton would show a hand (P38). */
    @Override
    protected void handleCursor(GuiGraphicsExtractor graphics) {
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int i, int i1, float v) {
        SequencerScreen.drawBorder(guiGraphics, getX(), getY(), getWidth(), getHeight(), Constants.BORDER_COLOR, 1);
        int color = 0xFF193B59;
        if (selected) {
            color = 0xFF063B6B;
        }
        if (isHovered()) {
            color = 0xFF063B6B;
        }
        guiGraphics.fill(getX() + 1, getY() + 1, getX() + getWidth() - 1, getY() + getHeight() - 1, color);
        if(stack.get() != null && !stack.get().isEmpty() && stack.get().has(DataComponentInit.DISK_STORAGE.get())) {
            // read only: component values are shared between stack copies
            DNAData dnaData = DiskStorage.fromStack(stack.get()).getOrMissing(value);
            guiGraphics.centeredText(Minecraft.getInstance().font, dnaData.getFormattedType().getString() + ": " + dnaData.getFormattedDNANoDescriptor().getString(), getX() + width / 2, getY() + 3, 0xFFFFFFFF);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    public interface OnClick {
        void onClick(SequenceDataDisplayWidget widget, boolean selected);
    }
}

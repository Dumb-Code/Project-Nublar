package net.dumbcode.projectnublar.gui.widget;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;

public class VanillaColorPickerWidget extends AbstractWidget {
    private int cubeHeight;
    private OnColorSelected onColorSelected;
    private Object2ObjectMap<RowColumn,DyeColor> colorMap = new Object2ObjectOpenHashMap<>();
    private static Object2ObjectMap<DyeColor,Integer> HEX_CODS = new Object2ObjectOpenHashMap<>();
    static {
        HEX_CODS.put(DyeColor.WHITE, 0xFFFFFFFF);
        HEX_CODS.put(DyeColor.ORANGE, 0xFFFFAA00);
        HEX_CODS.put(DyeColor.MAGENTA, 0xFFFF00FF);
        HEX_CODS.put(DyeColor.LIGHT_BLUE, 0xFF55FFFF);
        HEX_CODS.put(DyeColor.YELLOW, 0xFFFFFF55);
        HEX_CODS.put(DyeColor.LIME, 0xFF55FF55);
        HEX_CODS.put(DyeColor.PINK, 0xFFFF55FF);
        HEX_CODS.put(DyeColor.GRAY, 0xFF555555);
        HEX_CODS.put(DyeColor.LIGHT_GRAY, 0xFFAAAAAA);
        HEX_CODS.put(DyeColor.CYAN, 0xFF00AAAA);
        HEX_CODS.put(DyeColor.PURPLE, 0xFFAA55FF);
        HEX_CODS.put(DyeColor.BLUE, 0xFF0000AA);
        HEX_CODS.put(DyeColor.BROWN, 0xFFAA5500);
        HEX_CODS.put(DyeColor.GREEN, 0xFF00AA00);
        HEX_CODS.put(DyeColor.RED, 0xFFFF5555);
        HEX_CODS.put(DyeColor.BLACK, 0xFF000000);
    }

    public VanillaColorPickerWidget(int pX, int pY, int width, Component pMessage, OnColorSelected onColorSelected) {
        super(pX, pY, width, (width/8)*2, pMessage);
        this.cubeHeight = width / 8;
        this.onColorSelected = onColorSelected;
        int i = 0;
        for(DyeColor dyeColor : DyeColor.values()){
            int x = i % (DyeColor.values().length / 2);
            int y = i / (DyeColor.values().length / 2);
            colorMap.put(new RowColumn(y, x), dyeColor);
            i++;
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        colorMap.forEach((rowColumn, dyeColor) -> {
            if(mouseX >= getX() + (rowColumn.column * cubeHeight) && mouseX <= getX() + (rowColumn.column * cubeHeight) +cubeHeight && mouseY >= getY() + (rowColumn.row * cubeHeight) && mouseY <= getY() + (rowColumn.row * cubeHeight) + cubeHeight) {
                onColorSelected.onColorSelected(HEX_CODS.get(dyeColor));
            }
        });
    }

    @Override
    protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        for (RowColumn rowColumn : colorMap.keySet()) {
            DyeColor dyeColor = colorMap.get(rowColumn);

            int x = getX() + (rowColumn.column  * cubeHeight);
            int y = getY() + (rowColumn.row * cubeHeight);
            pGuiGraphics.fill(x, y, x + cubeHeight, y + cubeHeight, HEX_CODS.get(dyeColor));
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
    public interface OnColorSelected{
        void onColorSelected(int color);
    }
    record RowColumn(int row, int column){}
}
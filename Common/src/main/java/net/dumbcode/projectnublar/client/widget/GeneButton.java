package net.dumbcode.projectnublar.client.widget;

import net.dumbcode.projectnublar.api.Genes;
import net.dumbcode.projectnublar.client.screen.SequencerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class GeneButton extends AbstractWidget {
    int baseColor;
    public Genes.Gene type;
    boolean selected = false;
    SequencerScreen parent;
    private static final int CHROMOSOME_COLOUR_MIN_R = 0x27;
    private static final int CHROMOSOME_COLOUR_R_RANGE = 0x4D - 0x27;
    private static final int CHROMOSOME_COLOUR_MIN_G = 0x55;
    private static final int CHROMOSOME_COLOUR_G_RANGE = 0x99 - 0x55;
    private static final int CHROMOSOME_COLOUR_MIN_B = 0x7A;
    private static final int CHROMOSOME_COLOUR_B_RANGE = 0xB1 - 0x7A;
    float randomDist = Minecraft.getInstance().level.random.nextFloat();

    public GeneButton(SequencerScreen parent, int pX, int pY, Genes.Gene type) {
        super(pX, pY, 16, 5, Component.empty());
        this.type = type;
        this.parent = parent;
        int colourR = (int) (CHROMOSOME_COLOUR_MIN_R + CHROMOSOME_COLOUR_R_RANGE * randomDist);
        int colourG = (int) (CHROMOSOME_COLOUR_MIN_G + CHROMOSOME_COLOUR_G_RANGE * randomDist);
        int colourB = (int) (CHROMOSOME_COLOUR_MIN_B + CHROMOSOME_COLOUR_B_RANGE * randomDist);
        baseColor = 0xFF000000 | (colourR << 16) | (colourG << 8) | colourB;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public void onClick(double pMouseX, double pMouseY) {
        selected = !selected;
        parent.geneButtons.stream().filter(button->button!=this).forEach(button->button.setSelected(false));
        parent.selectedGene = selected ? type : null;
        if(selected) {
            parent.dinoData.addGeneValue(type, 0);
            parent.slider.setValue(parent.dinoData.getGeneValue(type));
        }
    }

    @Override
    protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (active) {
            if(selected) {
                pGuiGraphics.fill(getX(), getY(), getX() + this.width, getY() + this.height, 0xFFFF0000);
            } else {
                pGuiGraphics.fill(getX(), getY(), getX() + this.width, getY() + this.height, 0xFFFF00FF);
            }
        } else {
            pGuiGraphics.fill(getX(), getY(), getX() + this.width, getY() + this.height, baseColor);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {

    }
}

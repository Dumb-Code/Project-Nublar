package net.dumbcode.projectnublar.client.widget;


import net.dumbcode.projectnublar.api.dinosaur.DinoData;
import net.dumbcode.projectnublar.api.gene.Genes;
import net.dumbcode.projectnublar.gui.widget.NGLSlider;
import net.minecraft.network.chat.Component;

public class GeneSlider extends NGLSlider implements GeneHolder {
    public GeneSlider(int x, int y, int width, int height, Component prefix, Component suffix, double minValue, double maxValue, double currentValue, double stepSize, int precision, boolean drawString, OnValueChanged consumer) {
        super(x, y, width, height, prefix, suffix, minValue, maxValue, currentValue, stepSize, precision, drawString, consumer);
    }

    public GeneSlider(int x, int y, int width, int height, Component prefix, Component suffix, double minValue, double maxValue, double currentValue, boolean drawString, OnValueChanged consumer) {
        super(x, y, width, height, prefix, suffix, minValue, maxValue, currentValue, drawString, consumer);
    }

    @Override
    public void setGene(Genes.Gene gene, DinoData data) {
        this.setValue(data.getGeneValue(gene));
    }

}

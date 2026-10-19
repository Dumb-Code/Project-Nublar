package net.dumbcode.projectnublar.client.widget;





import net.dumbcode.projectnublar.api.dinosaur.DinoData;
import net.dumbcode.projectnublar.api.gene.Genes;
import net.dumbcode.projectnublar.client.CommonClientClass;
import net.dumbcode.projectnublar.client.renderer.layer.DinoLayer;
import net.dumbcode.projectnublar.client.screen.SequencerScreen;
import net.dumbcode.projectnublar.gui.widget.DinoColorPickerWidget;
import net.dumbcode.projectnublar.gui.widget.FilteredSelectionWidget;
import net.dumbcode.projectnublar.gui.widget.ParentWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdvancedColorWidget extends ParentWidget<SequencerScreen> implements GeneHolder{

    FilteredSelectionWidget<String, FilteredSelectionWidget.SelectionEntry<String>> colorWidget;
    private int currentLayer = 0;
    private List<String> entries;
    private DinoColorPickerWidget<AdvancedColorWidget> colorWheelWidget;


    public AdvancedColorWidget(SequencerScreen parent, int pX, int pY, int pWidth, int pHeight, Component pMessage, List<String> entries) {
        super(parent, pX, pY, pWidth, pHeight, pMessage);
        List<String> blah = new ArrayList<>(List.of("base"));
        blah.addAll(entries);
        this.entries = blah;
    }

    public void setEntries(List<String> entries) {
        this.entries = entries;
    }

    @Override
    public void init(boolean rebuild) {
        this.colorWidget = new FilteredSelectionWidget<>(this.getX(), this.getY(), this.getWidth(), Component.empty(), (selection) -> {
            currentLayer = entries.indexOf(selection);
            colorWheelWidget.setFromRBG(parent.dinoData.getLayerColors().get(currentLayer));
            this.parent.getMenu().sendUpdate(parent.dinoData);
        });
        colorWheelWidget = new DinoColorPickerWidget<>(this,this.getX(), this.getY() + 20, 80, 80, Component.empty(), (color) -> {
            parent.dinoData.getLayerColors().set(currentLayer, color);
            this.parent.getMenu().sendUpdate(parent.dinoData);

        });
        this.addRenderableWidget(colorWidget);
        this.addRenderableWidget(colorWheelWidget);
        Collections.reverse(this.renderables);

    }


    @Override
    protected void renderBackground(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {

    }

    @Override
    protected void renderForeground(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {

    }

    @Override
    public void setGene(Genes.Gene gene, DinoData data) {
        List<String> blah = new ArrayList<>(List.of("base"));
        blah.addAll(CommonClientClass.getDinoLayers(data.getBaseDino()).stream().filter(l->l.getBasicLayer()!=-1).map(DinoLayer::getLayerName).toList());
        setEntries(blah);
        colorWheelWidget.setFromRBG(data.getLayerColors().get(0));
        entries.stream().map((s) -> new FilteredSelectionWidget.SelectionEntry<>(s, Component.literal(s))).forEach(colorWidget::addEntry);

    }
}

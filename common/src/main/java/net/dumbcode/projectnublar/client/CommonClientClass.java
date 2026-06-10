package net.dumbcode.projectnublar.client;

import net.dumbcode.projectnublar.api.dinosaur.DinoData;
import net.dumbcode.projectnublar.api.gene.Genes;
import net.dumbcode.projectnublar.client.renderer.layer.DinoLayer;
import net.dumbcode.projectnublar.client.screen.SequencerScreen;
import net.dumbcode.projectnublar.client.widget.AdvancedColorWidget;
import net.dumbcode.projectnublar.client.widget.GeneHolder;
import net.dumbcode.projectnublar.client.widget.GeneSlider;
import net.dumbcode.projectnublar.registry.EntityInit;
import net.dumbcode.projectnublar.registry.GeneInit;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Client-only registries: the per-gene editor widgets used by the sequencer's advanced edit tab,
 * and the per-species texture layer definitions. {@code LAYER_REGISTRY} keys and layer name
 * strings are frozen texture-path contracts
 * ({@code textures/entity/{path}/{male|female}/{layerName}.png}).
 */
public class CommonClientClass {

    private static final Map<Genes.Gene,  BiFunction<SequencerScreen, DinoData, GeneHolder>> GENE_WIDGET = new HashMap<>();
    private static final Map<EntityType<?>, List<DinoLayer>> LAYER_REGISTRY = new HashMap<>();

    public static  BiFunction<SequencerScreen, DinoData,GeneHolder> getGeneWidget(Genes.Gene gene) {
        for(Genes.Gene g : GENE_WIDGET.keySet()) {
            if(g.equals(gene)) {
                return GENE_WIDGET.get(g);
            }
        }
        return null;
    }
    public static void initClient() {
        registerGeneWidgets();
        registerLayerNames();
    }
    public static void registerLayerNames(){

        //Carnivores

        LAYER_REGISTRY.put(EntityInit.TYRANNOSAURUS_REX.get(),List.of(
                new DinoLayer("base", 0),
                new DinoLayer("belly", 2),
                new DinoLayer("back", 1),
                new DinoLayer("pattern", 3),
                new DinoLayer("mouth", -1),
                new DinoLayer("teeth", -1),
                new DinoLayer("eyes", -1),
                new DinoLayer("nostrils", -1),
                new DinoLayer("claws", -1),

                new DinoLayer("feet", 3,dino -> dino.getDinoGender() == 2D),
                new DinoLayer("yellow", 2,dino -> dino.getDinoGender() == 2D)

        ));

        // TODO(DEAD): the remaining velociraptor layers (belly/stripes/mouth/eyes/eyelids/claws)
        // were commented out in the original; only "base" is registered.
        LAYER_REGISTRY.put(EntityInit.VELOCIRAPTOR.get(),List.of(
                new DinoLayer("base",0)
        ));

        LAYER_REGISTRY.put(EntityInit.DILOPHOSAURUS.get(),List.of(
                new DinoLayer("back", 1),
                new DinoLayer("belly", 2),
                new DinoLayer("pattern", 3),
                new DinoLayer("red", 3),
                new DinoLayer("whiteoutline", 3),
                new DinoLayer("mouth", -1),
                new DinoLayer("nostrils", -1),
                new DinoLayer("teeth", -1),
                new DinoLayer("eyes", -1),
                new DinoLayer("frills", -1),
                new DinoLayer("frillsgreen", -1),
                new DinoLayer("frillswhite", -1),
                new DinoLayer("frillsred", -1),
                new DinoLayer("claws", -1)
        ));

        //Herbivores

        // TODO(DEAD): all other triceratops layers were commented out (original note: "WHY NO
        // WORK FOR TRICERATOPS?"); only "base" is registered.
        LAYER_REGISTRY.put(EntityInit.TRICERATOPS.get(),List.of(
                new DinoLayer("base",0)
                ));

        LAYER_REGISTRY.put(EntityInit.BRACHIOSAURUS.get(),List.of(
                new DinoLayer("back",1),
                new DinoLayer("belly",2),
                new DinoLayer("eyes", -1),
                new DinoLayer("mouth", -1),
                new DinoLayer("nostrils",-1),
                new DinoLayer("toes",-1)
        ));
        LAYER_REGISTRY.put(EntityInit.GALLIMIMUS.get(),List.of(
                new DinoLayer("body-darkness",1),
                new DinoLayer("peach",2),
                new DinoLayer("tail-darkness",1),
                new DinoLayer("brown",2),
                new DinoLayer("body-stripes",3),
                new DinoLayer("belly",2),
                new DinoLayer("tail-stripes",2),
                new DinoLayer("claws",-1),
                new DinoLayer("nostrils",-1),
                new DinoLayer("eyes",-1),
                new DinoLayer("eyelids",-1)
        ));
    }
    public static List<DinoLayer> getDinoLayers(EntityType<?> type){
        if(!LAYER_REGISTRY.containsKey(type)){
            return List.of(new DinoLayer("base", 0));
        }
        return LAYER_REGISTRY.get(type);
    }
    // GeneSlider layout/range constants ( shared by all slider genes).
    private static final int GENE_SLIDER_X_OFFSET = 235;
    private static final int GENE_SLIDER_Y_OFFSET = 50;
    private static final int GENE_SLIDER_WIDTH = 100;
    private static final int GENE_SLIDER_HEIGHT = 20;
    private static final int GENE_SLIDER_MIN = -100;
    private static final int GENE_SLIDER_MAX = 100;

    public static void registerGeneWidgets() {
        // These 19 genes all use an identical percentage slider; only COLOR differs.
        List<java.util.function.Supplier<Genes.Gene>> sliderGenes = List.of(
                GeneInit.AGGRESSION,
                GeneInit.DEFENSE,
                GeneInit.EAT_RATE,
                GeneInit.HEALTH,
                GeneInit.HEALTH_REGEN,
                GeneInit.HEAT_RESISTANCE,
                GeneInit.HERD_SIZE,
                GeneInit.PACK_SIZE,
                GeneInit.IMMUNITY,
                GeneInit.INTELLIGENCE,
                GeneInit.JUMP,
                GeneInit.NOCTURNAL,
                GeneInit.FERTILITY,
                GeneInit.SIZE,
                GeneInit.SPEED,
                GeneInit.STOMACH_CAPACITY,
                GeneInit.STRENGTH,
                GeneInit.TAMABILITY,
                GeneInit.UNDERWATER_CAPACITY);
        for (java.util.function.Supplier<Genes.Gene> geneSupplier : sliderGenes) {
            registerGeneSliderWidget(geneSupplier.get());
        }
        registerGeneWidget(GeneInit.COLOR.get() , (screen, dinoData)-> {
            AdvancedColorWidget widget =new AdvancedColorWidget(screen, screen.leftPos() + 235, screen.topPos() + 40, 100, 100, Component.empty(), CommonClientClass.getDinoLayers(dinoData.getBaseDino()).stream().filter(l->l.getBasicLayer()!=-1).map(DinoLayer::getLayerName).toList());
            return widget;
        });
    }

    /** Factory for the formerly copy-pasted, byte-identical percentage-slider registrations. */
    private static void registerGeneSliderWidget(Genes.Gene gene) {
        registerGeneWidget(gene, (screen, dinoData) -> new GeneSlider(
                screen.leftPos() + GENE_SLIDER_X_OFFSET, screen.topPos() + GENE_SLIDER_Y_OFFSET,
                GENE_SLIDER_WIDTH, GENE_SLIDER_HEIGHT,
                Component.empty(), Component.literal("%"),
                GENE_SLIDER_MIN, GENE_SLIDER_MAX, 0, true,
                (slider, value) -> dinoData.setGeneValue(gene, value)));
    }

    private static void registerGeneWidget(Genes.Gene gene, BiFunction<SequencerScreen, DinoData,GeneHolder> widget) {
        GENE_WIDGET.put(gene, widget);
    }

}

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
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
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
    /**
     * Layer lists for male animals of species whose male art is a different set of layers from the female art.
     * A species with no entry here uses its {@link #LAYER_REGISTRY} list for both genders.
     */
    private static final Map<EntityType<?>, List<DinoLayer>> MALE_LAYER_REGISTRY = new HashMap<>();

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

        // The Triceratops sets are the art team's Nublar skins, the female set here and the male set below: base.png
        // (the base pass) is the plain base layer, then the set's other layers in the art set's order, then the twelve
        // horn layers, of which each animal draws one (see withTriceratopsHorns). Colour or fixed (-1) as the art list
        // marks each layer; a colour index follows the layer with the same role in another species (see the comment on
        // each line).
        LAYER_REGISTRY.put(EntityInit.TRICERATOPS.get(),withTriceratopsHorns(
                new DinoLayer("belly",2),        // as every belly
                new DinoLayer("osteoderms",1),   // as back (proposed)
                new DinoLayer("toes",-1),
                new DinoLayer("mouth",-1),
                new DinoLayer("nostrils",-1),
                new DinoLayer("eyes",-1),
                new DinoLayer("eyelids",-1).visibleWhen(Dinosaur::isResting)
        ));
        MALE_LAYER_REGISTRY.put(EntityInit.TRICERATOPS.get(),withTriceratopsHorns(
                new DinoLayer("midtone",1),      // as back (proposed)
                new DinoLayer("darkercolor",1),  // as Gallimimus body-darkness (proposed)
                new DinoLayer("pattern",3),      // as Tyrannosaurus and Dilophosaurus pattern
                new DinoLayer("head",3),         // markings, as pattern (proposed)
                new DinoLayer("brown",2),        // as Gallimimus brown
                new DinoLayer("spots",3),        // markings, as pattern (proposed)
                new DinoLayer("toes",-1),
                new DinoLayer("mouth",-1),
                new DinoLayer("eyesockets",1),   // dark shade, as back (proposed)
                new DinoLayer("nostrils",-1),
                new DinoLayer("eyes",-1),
                new DinoLayer("eyelids",-1).visibleWhen(Dinosaur::isResting)
        ));

        LAYER_REGISTRY.put(EntityInit.BRACHIOSAURUS.get(),List.of(
                new DinoLayer("back",1),
                new DinoLayer("belly",2),
                // Male only: the JW male skin's blue on the male crest and neck.
                new DinoLayer("blue", -1, Dinosaur::isMale),
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
                // Eyelids show only while the animal rests: resting is the one sleep-like state synced to the client.
                new DinoLayer("eyelids",-1).visibleWhen(Dinosaur::isResting)
        ));
        // The male has its own art: base.png (the base pass) and then these layers, bottom to top.
        // Each colour channel mirrors the female layer with the same role (see the comment on each line).
        MALE_LAYER_REGISTRY.put(EntityInit.GALLIMIMUS.get(),List.of(
                new DinoLayer("speckle",1),      // as female body-darkness (proposed)
                new DinoLayer("green",2),        // as female peach (proposed)
                new DinoLayer("belly",2),        // as female belly
                new DinoLayer("tail",1),         // as female tail-darkness (proposed)
                new DinoLayer("pattern",2),      // as female brown (proposed)
                new DinoLayer("body-stripes",3), // as female body-stripes
                new DinoLayer("mouth",-1),
                new DinoLayer("claws",-1),
                new DinoLayer("nostrils",-1),
                new DinoLayer("eyes",-1),
                new DinoLayer("eyelids",-1).visibleWhen(Dinosaur::isResting)
        ));
    }

    /**
     * Returns {@code layers} followed by the twelve Triceratops horn layers, one per horn set and horn colour: the art's
     * Brown ({@code horn<N>}), Grey ({@code horn<N>_grey}) and Black ({@code horn<N>_black}) v1 textures. Each texture
     * paints one horn set, plus the nose horn, beak and frill spikes that every set shares. A horn layer is fixed (no
     * tint) and draws only on the animals whose {@link TriceratopsHorns} pick it is, so each animal shows one horn set
     * in one colour.
     */
    private static List<DinoLayer> withTriceratopsHorns(DinoLayer... layers) {
        List<DinoLayer> all = new ArrayList<>(List.of(layers));
        for (int set = 1; set <= TriceratopsHorns.SETS; set++) {
            for (int colour = 0; colour < TriceratopsHorns.COLOURS.size(); colour++) {
                TriceratopsHorns horns = new TriceratopsHorns(set, colour);
                all.add(new DinoLayer(horns.layerName(), -1)
                        .visibleWhen(dino -> horns.equals(TriceratopsHorns.of(dino.getUUID()))));
            }
        }
        return List.copyOf(all);
    }

    public static List<DinoLayer> getDinoLayers(EntityType<?> type){
        if(!LAYER_REGISTRY.containsKey(type)){
            return List.of(new DinoLayer("base", 0));
        }
        return LAYER_REGISTRY.get(type);
    }
    /**
     * Returns the layer list for one gender of a species. Males use their own list when the species registers one;
     * otherwise both genders share {@link #getDinoLayers(EntityType)}.
     */
    public static List<DinoLayer> getDinoLayers(EntityType<?> type, boolean male){
        if(male && MALE_LAYER_REGISTRY.containsKey(type)){
            return MALE_LAYER_REGISTRY.get(type);
        }
        return getDinoLayers(type);
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

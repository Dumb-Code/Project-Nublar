package net.dumbcode.projectnublar.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.client.renderer.*;
import net.dumbcode.projectnublar.client.renderer.dinosaurs.*;
import net.dumbcode.projectnublar.client.screen.EggPrinterScreen;
import net.dumbcode.projectnublar.client.screen.GeneratorScreen;
import net.dumbcode.projectnublar.client.screen.IncubatorScreen;
import net.dumbcode.projectnublar.client.screen.ProcessorScreen;
import net.dumbcode.projectnublar.client.screen.SequencerScreen;
import net.dumbcode.projectnublar.client.model.DinosaurGeoModel;
import net.dumbcode.projectnublar.entity.dinosaur.DinosaurPart;
import net.dumbcode.projectnublar.registry.BlockInit;
import net.dumbcode.projectnublar.registry.EntityInit;
import net.dumbcode.projectnublar.registry.MenuTypeInit;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public class ClientRegistrationHolder {

    public static void registerEntityRenderers() {
        // A part has no model of its own: the renderer draws nothing.
        EntityRenderers.register(EntityInit.DINOSAUR_PART.get(),context -> new EntityRenderer<DinosaurPart, EntityRenderState>(context) {
            @Override
            public EntityRenderState createRenderState() {
                return new EntityRenderState();
            }

            @Override
            public void submit(EntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
            }
        });
        EntityRenderers.register(EntityInit.TYRANNOSAURUS_REX.get(), (context) -> new TyrannosaurusRenderer(context, new DinosaurGeoModel(Constants.modLoc("tyrannosaurus_rex"))));
        EntityRenderers.register(EntityInit.VELOCIRAPTOR.get(), (context) -> new VelociraptorRenderer(context, new DinosaurGeoModel(Constants.modLoc("velociraptor"))));
        EntityRenderers.register(EntityInit.DILOPHOSAURUS.get(), (context) -> new DilophosaurusRenderer(context, new DinosaurGeoModel(Constants.modLoc("dilophosaurus"))));
        EntityRenderers.register(EntityInit.TRICERATOPS.get(), (context) -> new TriceratopsRenderer(context, new DinosaurGeoModel(Constants.modLoc("triceratops"))));
        EntityRenderers.register(EntityInit.BRACHIOSAURUS.get(), (context) -> new BrachiosaurusRenderer(context, new DinosaurGeoModel(Constants.modLoc("brachiosaurus"))));
        EntityRenderers.register(EntityInit.GALLIMIMUS.get(), (context) -> new GallimimusRenderer(context, new DinosaurGeoModel(Constants.modLoc("gallimimus"))));
    }

    public static void menuScreens() {
        MenuScreens.register(MenuTypeInit.PROCESSOR.get(), ProcessorScreen::new);
        MenuScreens.register(MenuTypeInit.SEQUENCER.get(), SequencerScreen::new);
        MenuScreens.register(MenuTypeInit.EGG_PRINTER.get(), EggPrinterScreen::new);
        MenuScreens.register(MenuTypeInit.INCUBATOR.get(), IncubatorScreen::new);
        MenuScreens.register(MenuTypeInit.GENERATOR_MENU.get(), GeneratorScreen::new);
    }

    public static void registerBlockEntityRenderers() {
        BlockEntityRenderers.register(BlockInit.FEEDER_BLOCK_ENTITY.get(), CarnivoreFeederRenderer::new);
        BlockEntityRenderers.register(BlockInit.PROCESSOR_BLOCK_ENTITY.get(), ProcessorRenderer::new);
         BlockEntityRenderers.register(BlockInit.SEQUENCER_BLOCK_ENTITY.get(), SequencerRenderer::new);
         BlockEntityRenderers.register(BlockInit.EGG_PRINTER_BLOCK_ENTITY.get(), EggPrinterRenderer::new);
         BlockEntityRenderers.register(BlockInit.INCUBATOR_BLOCK_ENTITY.get(), IncubatorRenderer::new);
         BlockEntityRenderers.register(BlockInit.ELECTRIC_FENCE_POST_BLOCK_ENTITY.get(), ElectricFenceRenderer::new);
         BlockEntityRenderers.register(BlockInit.ELECTRIC_FENCE_BLOCK_ENTITY.get(), (c)-> new ElectricWireRenderer());
    }
}

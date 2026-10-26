package net.dumbcode.projectnublar.client;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.DefaultedBlockGeoModel;
import com.geckolib.renderer.GeoBlockRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.block.entity.EggPrinterBlockEntity;
import net.dumbcode.projectnublar.block.entity.IncubatorBlockEntity;
import net.dumbcode.projectnublar.client.renderer.*;
import net.dumbcode.projectnublar.client.renderer.dinosaurs.*;
import net.dumbcode.projectnublar.client.screen.EggPrinterScreen;
import net.dumbcode.projectnublar.client.screen.GeneratorScreen;
import net.dumbcode.projectnublar.client.screen.IncubatorScreen;
import net.dumbcode.projectnublar.client.screen.ProcessorScreen;
import net.dumbcode.projectnublar.client.screen.SequencerScreen;
import net.dumbcode.projectnublar.client.model.DinosaurGeoModel;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.entity.dinosaur.DinosaurPart;
import net.dumbcode.projectnublar.registry.BlockInit;
import net.dumbcode.projectnublar.registry.EntityInit;
import net.dumbcode.projectnublar.registry.ItemInit;
import net.dumbcode.projectnublar.registry.MenuTypeInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;

public class ClientRegistrationHolder {

    public static void registerEntityRenderers() {
        EntityRenderers.register(EntityInit.DINOSAUR_PART.get(),context -> new EntityRenderer<DinosaurPart>(context) {
            @Override
            public Identifier getTextureLocation(DinosaurPart entity) {
                return null;
            }
        });
        EntityRenderers.register(EntityInit.TYRANNOSAURUS_REX.get(), (context) -> new TyrannosaurusRenderer(context, new DinosaurGeoModel(Constants.modLoc("tyrannosaurus_rex"))));
        EntityRenderers.register(EntityInit.VELOCIRAPTOR.get(), (context) -> new VelociraptorRenderer(context, new DinosaurGeoModel(Constants.modLoc("velociraptor"))));
        EntityRenderers.register(EntityInit.DILOPHOSAURUS.get(), (context) -> new DilophosaurusRenderer(context, new DinosaurGeoModel(Constants.modLoc("dilophosaurus"))));
        EntityRenderers.register(EntityInit.TRICERATOPS.get(), (context) -> new TriceratopsRenderer(context, new DinosaurGeoModel(Constants.modLoc("triceratops"))));
        EntityRenderers.register(EntityInit.BRACHIOSAURUS.get(), (context) -> new BrachiosaurusRenderer(context, new DinosaurGeoModel(Constants.modLoc("brachiosaurus"))));
        EntityRenderers.register(EntityInit.GALLIMIMUS.get(), (context) -> new GallimimusRenderer(context, new GallimimusBaseModel()));
    }

    public static void menuScreens() {
        MenuScreens.register(MenuTypeInit.PROCESSOR.get(), ProcessorScreen::new);
        MenuScreens.register(MenuTypeInit.SEQUENCER.get(), SequencerScreen::new);
        MenuScreens.register(MenuTypeInit.EGG_PRINTER.get(), EggPrinterScreen::new);
        MenuScreens.register(MenuTypeInit.INCUBATOR.get(), IncubatorScreen::new);
        MenuScreens.register(MenuTypeInit.GENERATOR_MENU.get(), GeneratorScreen::new);
        Minecraft.getInstance().getTextureManager().register(Constants.modLoc("textures/entity/tyrannosaurus_rex.png"), createRexTexture());
        Minecraft.getInstance().getTextureManager().register(Constants.modLoc("textures/entity/velociraptor.png"), createVelociraptorTexture());
        Minecraft.getInstance().getTextureManager().register(Constants.modLoc("textures/entity/dilophosaurus.png"), createDiloTexture());
        Minecraft.getInstance().getTextureManager().register(Constants.modLoc("textures/entity/triceratops.png"), createTrikeTexture());
        Minecraft.getInstance().getTextureManager().register(Constants.modLoc("textures/entity/brachiosaurus.png"), createBrachTexture());
    }

    public static void registerBlockEntityRenderers() {
        BlockEntityRenderers.register(BlockInit.FEEDER_BLOCK_ENTITY.get(), CarnivoreFeederRenderer::new);
        BlockEntityRenderers.register(BlockInit.PROCESSOR_BLOCK_ENTITY.get(), ProcessorRenderer::new);
         BlockEntityRenderers.register(BlockInit.SEQUENCER_BLOCK_ENTITY.get(), SequencerRenderer::new);
         BlockEntityRenderers.register(BlockInit.EGG_PRINTER_BLOCK_ENTITY.get(), (context) -> new GeoBlockRenderer<EggPrinterBlockEntity, BlockEntityRenderState>(context, new DefaultedBlockGeoModel<>(Identifier.fromNamespaceAndPath(Constants.MODID,"egg_printer"))));
         // Incubator: translucent, and each part shows only while its item is in the incubator.
         BlockEntityRenderers.register(BlockInit.INCUBATOR_BLOCK_ENTITY.get(), (context) -> new GeoBlockRenderer<IncubatorBlockEntity, BlockEntityRenderState>(context, new DefaultedBlockGeoModel<>(Identifier.fromNamespaceAndPath(Constants.MODID,"incubator"))) {
            private static final DataTicket<Boolean> HAS_NEST = DataTicket.create("projectnublar_incubator_has_nest", Boolean.class);
            private static final DataTicket<Boolean> HAS_LID = DataTicket.create("projectnublar_incubator_has_lid", Boolean.class);
            private static final DataTicket<Boolean> HAS_BASE = DataTicket.create("projectnublar_incubator_has_base", Boolean.class);
            private static final DataTicket<Boolean> HAS_ARM = DataTicket.create("projectnublar_incubator_has_arm", Boolean.class);

            @Override
            public void addRenderData(IncubatorBlockEntity animatable, Void relatedObject, BlockEntityRenderState renderState, float partialTick) {
                renderState.addGeckolibData(HAS_NEST, !animatable.getNestStack().isEmpty());
                renderState.addGeckolibData(HAS_LID, !animatable.getLidStack().isEmpty());
                renderState.addGeckolibData(HAS_BASE, !animatable.getBaseStack().isEmpty());
                renderState.addGeckolibData(HAS_ARM, !animatable.getArmStack().isEmpty());
            }

            @Override
            public void adjustModelBonesForRender(RenderPassInfo<BlockEntityRenderState> renderPassInfo, BoneSnapshots snapshots) {
                setHidden(snapshots, "nest", !renderPassInfo.getGeckolibData(HAS_NEST));
                setHidden(snapshots, "cover", !renderPassInfo.getGeckolibData(HAS_LID));
                setHidden(snapshots, "arm_base", !renderPassInfo.getGeckolibData(HAS_BASE));
                setHidden(snapshots, "RoboticHand1", !renderPassInfo.getGeckolibData(HAS_ARM));
            }

            /** Hides or shows a bone and its children, as GeckoLib 4's {@code GeoBone.setHidden} did. */
            private static void setHidden(BoneSnapshots snapshots, String bone, boolean hidden) {
                snapshots.ifPresent(bone, snapshot -> snapshot.skipRender(hidden).skipChildrenRender(hidden));
            }

            @Override
            public RenderType getRenderType(BlockEntityRenderState renderState, Identifier texture) {
                return RenderTypes.entityTranslucent(texture);
            }
        });
         BlockEntityRenderers.register(BlockInit.ELECTRIC_FENCE_POST_BLOCK_ENTITY.get(), ElectricFenceRenderer::new);
         BlockEntityRenderers.register(BlockInit.ELECTRIC_FENCE_BLOCK_ENTITY.get(), (c)-> new ElectricWireRenderer());
    }

    public static void registerItemProperties() {
        ItemProperties.register(ItemInit.SYRINGE.get(), Constants.modLoc("filled"), (stack, world, entity, i) -> stack.hasTag() ? stack.getTag().getBoolean("dna_percentage") ? 0.5F : 1.0F : 0f);
    }

    public static AbstractTexture createRexTexture() {return Minecraft.getInstance().getTextureManager().getTexture(Constants.modLoc("textures/entity/tyrannosaurus_rex/female/tyrannosaurus_rex.png"));}
    public static AbstractTexture createVelociraptorTexture() {return Minecraft.getInstance().getTextureManager().getTexture(Constants.modLoc("textures/entity/velociraptor/male/velociraptor.png"));}
    public static AbstractTexture createDiloTexture() {return Minecraft.getInstance().getTextureManager().getTexture(Constants.modLoc("textures/entity/dilophosaurus/male/dilophosaurus.png"));}
    public static AbstractTexture createTrikeTexture() {return Minecraft.getInstance().getTextureManager().getTexture(Constants.modLoc("textures/entity/triceratops/male/triceratops.png"));}
    public static AbstractTexture createBrachTexture() {return Minecraft.getInstance().getTextureManager().getTexture(Constants.modLoc("textures/entity/brachiosaurus/male/brachiosaurus.png"));}

    /**
     * Base pass model for Gallimimus. Draws the plain per-gender {@code base.png}, not the composite {@code gallimimus.png}.
     * The composite already contains every marking. The colour layers (CommonClientClass) draw those markings again,
     * so the old alias to {@code male/gallimimus.png} doubled them. It also gave female animals the male base.
     */
    private static class GallimimusBaseModel extends DinosaurGeoModel {
        private static final Identifier MALE_BASE = Constants.modLoc("textures/entity/gallimimus/male/base.png");
        private static final Identifier FEMALE_BASE = Constants.modLoc("textures/entity/gallimimus/female/base.png");

        GallimimusBaseModel() {
            super(Constants.modLoc("gallimimus"));
        }

        @Override
        public Identifier getTextureResource(Dinosaur animatable) {
            // Same rule as Dinosaur.getStringDinoGender: gene value 2.0 is male, anything else female.
            return animatable.getDinoGender() == 2.0D ? MALE_BASE : FEMALE_BASE;
        }
    }
}

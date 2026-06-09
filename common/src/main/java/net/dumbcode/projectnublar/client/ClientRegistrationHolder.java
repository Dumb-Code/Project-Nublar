package net.dumbcode.projectnublar.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.block.entity.IncubatorBlockEntity;
import net.dumbcode.projectnublar.client.renderer.DinosaurRenderer;
import net.dumbcode.projectnublar.client.renderer.ElectricFenceRenderer;
import net.dumbcode.projectnublar.client.renderer.ElectricWireRenderer;
import net.dumbcode.projectnublar.client.renderer.ProcessorRenderer;
import net.dumbcode.projectnublar.client.renderer.SequencerRenderer;
import net.dumbcode.projectnublar.client.renderer.dinosaurs.*;
import net.dumbcode.projectnublar.client.screen.EggPrinterScreen;
import net.dumbcode.projectnublar.client.screen.GeneratorScreen;
import net.dumbcode.projectnublar.client.screen.IncubatorScreen;
import net.dumbcode.projectnublar.client.screen.ProcessorScreen;
import net.dumbcode.projectnublar.client.screen.SequencerScreen;
import net.dumbcode.projectnublar.entity.dinosaur.DinosaurPart;
import net.dumbcode.projectnublar.registry.BlockInit;
import net.dumbcode.projectnublar.registry.EntityInit;
import net.dumbcode.projectnublar.registry.ItemInit;
import net.dumbcode.projectnublar.registry.MenuTypeInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class ClientRegistrationHolder {

    public static void registerEntityRenderers() {
        EntityRenderers.register(EntityInit.DINOSAUR_PART.get(),context -> new EntityRenderer<DinosaurPart>(context) {
            @Override
            public ResourceLocation getTextureLocation(DinosaurPart entity) {
                return null;
            }
        });
        EntityRenderers.register(EntityInit.TYRANNOSAURUS_REX.get(), (context) -> new TyrannosaurusRenderer(context, new DefaultedEntityGeoModel<>(Constants.modLoc("tyrannosaurus_rex"))));
        EntityRenderers.register(EntityInit.VELOCIRAPTOR.get(), (context) -> new VelociraptorRenderer(context, new DefaultedEntityGeoModel<>(Constants.modLoc("velociraptor"))));
        EntityRenderers.register(EntityInit.DILOPHOSAURUS.get(), (context) -> new DilophosaurusRenderer(context, new DefaultedEntityGeoModel<>(Constants.modLoc("dilophosaurus"))));
        EntityRenderers.register(EntityInit.TRICERATOPS.get(), (context) -> new TriceratopsRenderer(context, new DefaultedEntityGeoModel<>(Constants.modLoc("triceratops"))));
        EntityRenderers.register(EntityInit.BRACHIOSAURUS.get(), (context) -> new BrachiosaurusRenderer(context, new DefaultedEntityGeoModel<>(Constants.modLoc("brachiosaurus"))));
        EntityRenderers.register(EntityInit.GALLIMIMUS.get(), (context) -> new GallimimusRenderer(context, new DefaultedEntityGeoModel<>(Constants.modLoc("gallimimus"))));
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
        Minecraft.getInstance().getTextureManager().register(Constants.modLoc("textures/entity/gallimimus.png"), createGalliTexture());
    }

    public static void registerBlockEntityRenderers() {
        BlockEntityRenderers.register(BlockInit.PROCESSOR_BLOCK_ENTITY.get(), (context) -> new ProcessorRenderer());
         BlockEntityRenderers.register(BlockInit.SEQUENCER_BLOCK_ENTITY.get(), (context) -> new SequencerRenderer());
         BlockEntityRenderers.register(BlockInit.EGG_PRINTER_BLOCK_ENTITY.get(), (context) -> new GeoBlockRenderer<>(new DefaultedBlockGeoModel<>(new ResourceLocation(Constants.MODID, "egg_printer"))));
         BlockEntityRenderers.register(BlockInit.INCUBATOR_BLOCK_ENTITY.get(), (context) -> new GeoBlockRenderer<IncubatorBlockEntity>(new DefaultedBlockGeoModel<>(new ResourceLocation(Constants.MODID, "incubator"))) {
            @Override
            public void renderRecursively(PoseStack poseStack, IncubatorBlockEntity animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
                if (bone.getName().equals("nest")) {
                    bone.setHidden(animatable.getNestStack().isEmpty());
                }
                if (bone.getName().equals("cover")) {
                    bone.setHidden(animatable.getLidStack().isEmpty());
                }
                if (bone.getName().equals("arm_base")) {
                    bone.setHidden(animatable.getBaseStack().isEmpty());
                }
                if (bone.getName().equals("RoboticHand1")) {
                    bone.setHidden(animatable.getArmStack().isEmpty());
                }

                super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
            }

            @Override
            public RenderType getRenderType(IncubatorBlockEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
                return RenderType.entityTranslucent(texture);
            }
        });
         BlockEntityRenderers.register(BlockInit.ELECTRIC_FENCE_POST_BLOCK_ENTITY.get(), (c)-> new ElectricFenceRenderer());
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
    public static AbstractTexture createGalliTexture() {return Minecraft.getInstance().getTextureManager().getTexture(Constants.modLoc("textures/entity/gallimimus/male/gallimimus.png"));}
}

package net.dumbcode.projectnublar.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.block.entity.IncubatorBlockEntity;
import net.dumbcode.projectnublar.client.renderer.DinosaurRenderer;
import net.dumbcode.projectnublar.client.renderer.SequencerRenderer;
import net.dumbcode.projectnublar.client.screen.EggPrinterScreen;
import net.dumbcode.projectnublar.client.screen.IncubatorScreen;
import net.dumbcode.projectnublar.client.screen.ProcessorScreen;
import net.dumbcode.projectnublar.client.screen.SequencerScreen;
import net.dumbcode.projectnublar.init.BlockInit;
import net.dumbcode.projectnublar.init.EntityInit;
import net.dumbcode.projectnublar.init.ItemInit;
import net.dumbcode.projectnublar.init.MenuTypeInit;
import net.dumbcode.projectnublar.client.renderer.ProcessorRenderer;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import java.util.function.Supplier;

public class ClientRegistrationHolder {

    public static Object2ObjectMap<Supplier<? extends EntityType>, EntityRendererProvider> entityRenderers(){
        Object2ObjectMap<Supplier<? extends EntityType>, EntityRendererProvider> map = new Object2ObjectOpenHashMap<>();
        map.put(EntityInit.TYRANNOSAURUS_REX, (context)->new DinosaurRenderer(context, new DefaultedEntityGeoModel(Constants.modLoc("tyrannosaurus_rex"))));
        return map;
    }
    public static void menuScreens(){
        MenuScreens.register(MenuTypeInit.PROCESSOR.get(), ProcessorScreen::new);
        MenuScreens.register(MenuTypeInit.SEQUENCER.get(), SequencerScreen::new);
        MenuScreens.register(MenuTypeInit.EGG_PRINTER.get(), EggPrinterScreen::new);
        MenuScreens.register(MenuTypeInit.INCUBATOR.get(), IncubatorScreen::new);
        Minecraft.getInstance().getTextureManager().register(Constants.modLoc( "textures/entity/tyrannosaurus_rex.png"), createTexture());
    }
    public static Object2ObjectMap<Supplier<? extends BlockEntityType>, BlockEntityRendererProvider> getBlockEntityRenderers(){
        Object2ObjectMap<Supplier<? extends BlockEntityType>, BlockEntityRendererProvider> map = new Object2ObjectOpenHashMap<>();
        map.put(BlockInit.PROCESSOR_BLOCK_ENTITY, (context)->new ProcessorRenderer());
        map.put(BlockInit.SEQUENCER_BLOCK_ENTITY, (context)->new SequencerRenderer());
        map.put(BlockInit.EGG_PRINTER_BLOCK_ENTITY, (context)->new GeoBlockRenderer<>(new DefaultedBlockGeoModel<>(new ResourceLocation(Constants.MODID, "egg_printer"))));
        map.put(BlockInit.INCUBATOR_BLOCK_ENTITY, (context)->new GeoBlockRenderer<IncubatorBlockEntity>(new DefaultedBlockGeoModel<>(new ResourceLocation(Constants.MODID, "incubator"))){
            @Override
            public void renderRecursively(PoseStack poseStack, IncubatorBlockEntity animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
                if(bone.getName().equals("nest")){
                    bone.setHidden(animatable.getNestStack().isEmpty());
                }
                if(bone.getName().equals("cover")){
                    bone.setHidden(animatable.getLidStack().isEmpty());
                }
                if(bone.getName().equals("arm_base")){
                    bone.setHidden(animatable.getBaseStack().isEmpty());
                }
                if(bone.getName().equals("RoboticHand1")){
                    bone.setHidden(animatable.getArmStack().isEmpty());
                }

                super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
            }

            @Override
            public RenderType getRenderType(IncubatorBlockEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
                return RenderType.entityTranslucent(texture);
            }
        });
        return map;
    }
    public static void registerItemProperties(){
        ItemProperties.register(ItemInit.SYRINGE.get(), Constants.modLoc( "filled"), (stack, world, entity, i) -> stack.hasTag() ? stack.getTag().getBoolean("dna_percentage") ? 0.5F : 1.0F : 0f);
    }

    public static AbstractTexture createTexture(){
        return Minecraft.getInstance().getTextureManager().getTexture(Constants.modLoc( "textures/entity/tyrannosaurus_rex/male/tyrannosaurus_rex.png"));
    }
}

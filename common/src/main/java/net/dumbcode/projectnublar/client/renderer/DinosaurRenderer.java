package net.dumbcode.projectnublar.client.renderer;

import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.client.renderer.layer.DinoLayer;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.entity.dinosaur.animation.AnimationChoice;
import net.dumbcode.projectnublar.entity.dinosaur.animation.IdleSway;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Shared renderer for all dinosaur species, on GeckoLib 5 render states ({@link LivingEntityRenderState}: GeckoLib
 * creates it for every {@code LivingEntity}). Everything a frame needs is read from the animal in
 * {@link #addRenderData} into data tickets; the passes read only the tickets.
 *
 * <p>Size. With {@link Dinosaur#SCALE_MODE} false the model is scaled by hand by {@link Dinosaur#scaleFactor()}, the
 * 1.20.1 render scale; with it true GeckoLib's own entity scale does it. {@code GeoEntityRenderer.scaleModelForRender}
 * multiplies by {@code LivingEntityRenderState.scale}, which {@code extractLivingEntityRenderState} reads from
 * {@code entity.getScale()} (the {@code SCALE} attribute): 1.0 with the mode off, the scale factor with it on, so
 * nothing is scaled twice. Age scale: GeckoLib 5 stores {@code getAgeScale()} in {@code LivingEntityRenderState.ageScale}
 * but no GeckoLib 5 render step reads it, and this renderer does not extend vanilla's {@code LivingEntityRenderer},
 * so a baby's model is not halved in either mode, as on 1.20.1 (GeckoLib 4.7.3 applied no entity scale at all). The
 * growth stage is in the factor itself.
 *
 * <p>Colour layers are {@link DinoColourLayer}s, built once per gender with the first animal of that gender. The
 * head look of GeckoLib 4 ({@code turnsHead}) was off for every species, so no bone pass turns the head.
 */
public class DinosaurRenderer extends GeoEntityRenderer<Dinosaur, LivingEntityRenderState> {
    /**
     * The animal is male (gender gene 2.0, as {@link Dinosaur#isMale()}); the base texture of every species and the
     * Brachiosaurus crests read it.
     */
    public static final DataTicket<Boolean> MALE = DataTicket.create("projectnublar_dinosaur_male", Boolean.class);
    /** Per colour layer, by its position in {@link Dinosaur#getLayers()}: its ARGB colour. */
    static final DataTicket<int[]> LAYER_COLOURS = DataTicket.create("projectnublar_dinosaur_layer_colours", int[].class);
    /** Per colour layer, by its position in {@link Dinosaur#getLayers()}: {@link DinoLayer#isVisibleFor}. */
    static final DataTicket<boolean[]> LAYER_VISIBLE = DataTicket.create("projectnublar_dinosaur_layer_visible", boolean[].class);
    private static final DataTicket<Boolean> SCALE_MODE = DataTicket.create("projectnublar_dinosaur_scale_mode", Boolean.class);
    private static final DataTicket<Float> MANUAL_SCALE = DataTicket.create("projectnublar_dinosaur_manual_scale", Float.class);
    private static final DataTicket<Boolean> GROUP_LEADER = DataTicket.create("projectnublar_dinosaur_group_leader", Boolean.class);

    private static final Identifier LEADER_CROWN = Identifier.fromNamespaceAndPath(Constants.MODID,"textures/misc/leader_crown.png");
    private static final float CROWN_SIZE = 0.6F;
    private static final float CROWN_GAP = 0.5F;
    /** The culling box is the model's bind-pose bounds plus 25 %, for poses that reach further. */
    private static final float CULLING_MARGIN = 1.25F;
    /**
     * The first bone of each gender's crest chain in the Brachiosaurus model ({@code crestMale1-4},
     * {@code crestFemale1, 2, 4, 5}); no other species model has gender bones.
     */
    private static final String MALE_CREST = "crestMale1";
    private static final String FEMALE_CREST = "crestFemale1";

    /** The colour layers of each gender are built (once, with the first animal of that gender). */
    private boolean maleLayersBuilt;
    private boolean femaleLayersBuilt;

    /** A renderer for one species, drawing {@code model}. */
    public DinosaurRenderer(EntityRendererProvider.Context context, GeoModel<Dinosaur> model) {
        super(context, model);
    }

    @Override
    public void addRenderData(Dinosaur animatable, Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
        boolean male = animatable.isMale();
        buildLayers(animatable, male);
        renderState.addGeckolibData(MALE, male);
        renderState.addGeckolibData(SCALE_MODE, animatable.getEntityData().get(Dinosaur.SCALE_MODE));
        renderState.addGeckolibData(MANUAL_SCALE, animatable.scaleFactor());
        renderState.addGeckolibData(GROUP_LEADER, animatable.isGroupLeader());

        List<DinoLayer> layers = animatable.getLayers();
        int[] colours = new int[layers.size()];
        boolean[] visible = new boolean[layers.size()];
        for (int i = 0; i < colours.length; i++) {
            DinoLayer layer = layers.get(i);
            colours[i] = animatable.layerColor(i + 1, layer);
            visible[i] = layer.isVisibleFor(animatable);
        }
        renderState.addGeckolibData(LAYER_COLOURS, colours);
        renderState.addGeckolibData(LAYER_VISIBLE, visible);

        // TODO(BUG): writes the {@code DINOSAUR_HEAD_POS} synched entity data from the client
        // render thread.
        getGeoModel().getBakedModel(getGeoModel().getModelResource(renderState)).getBone("head").ifPresent(head -> {
            Vec3 worldpos = animatable.position().add(head.pivotX(), head.pivotY(), head.pivotZ());
            animatable.getEntityData().set(Dinosaur.DINOSAUR_HEAD_POS, worldpos.toVector3f());
        });
    }

    /**
     * Adds a colour layer per {@link DinoLayer} of {@code animal}'s gender the first time that gender is seen.
     * {@link DinoLayer#getRenderRequirement()} is read here, once, with this first animal of that gender.
     */
    private void buildLayers(Dinosaur animal, boolean male) {
        if (male ? maleLayersBuilt : femaleLayersBuilt) {
            return;
        }
        List<DinoLayer> layers = animal.getLayers();
        for (int i = 0; i < layers.size(); i++) {
            DinoLayer layer = layers.get(i);
            if (layer.getRenderRequirement().apply(animal)) {
                withRenderLayer(new DinoColourLayer(this, male, i, layer, layer.getTextureLocation(animal)));
            }
        }
        if (male) {
            maleLayersBuilt = true;
        } else {
            femaleLayersBuilt = true;
        }
    }

    /**
     * The box vanilla tests against the view (after inflating it by 0.5): the hitbox, grown to hold the drawn model.
     * The hitbox alone sits at the feet, so a Brachiosaurus vanished while the camera saw only its head or tail. The
     * model's bind-pose bounds ({@link Dinosaur#modelBounds()}) are scaled by the render scale
     * ({@link Dinosaur#scaleFactor()}, in both size modes) and by {@link #CULLING_MARGIN}. Turning culling off instead
     * would draw every dinosaur behind the camera too, which costs too much on player hardware. This runs for every
     * dinosaur every frame, so the model box is built straight from the two numbers.
     */
    @Override
    protected AABB getBoundingBoxForCulling(Dinosaur entity) {
        EntityDimensions bounds = entity.modelBounds();
        float scale = entity.scaleFactor() * CULLING_MARGIN;
        double half = bounds.width() * scale / 2;
        double top = bounds.height() * scale;
        AABB model = new AABB(entity.getX() - half, entity.getY(), entity.getZ() - half,
                entity.getX() + half, entity.getY() + top, entity.getZ() + half);
        return super.getBoundingBoxForCulling(entity).minmax(model);
    }

    /** Scales by {@link Dinosaur#scaleFactor()} by hand only with the scale attribute off (see the class note). */
    @Override
    public void scaleModelForRender(RenderPassInfo<LivingEntityRenderState> renderPassInfo, float widthScale, float heightScale) {
        if (renderPassInfo.getGeckolibData(SCALE_MODE)) {
            super.scaleModelForRender(renderPassInfo, widthScale, heightScale);
            return;
        }
        float factor = renderPassInfo.getGeckolibData(MANUAL_SCALE);
        super.scaleModelForRender(renderPassInfo, widthScale * factor, heightScale * factor);
    }

    /**
     * Shows only the animal's own gender's crest: the Brachiosaurus model carries both, and without this every
     * Brachiosaurus drew the two on top of each other (on 1.20.1 too).
     *
     * <p>Then adds the always-on breathing sway ({@link IdleSway}, fix P12a) to the bones the species' model has, after
     * GeckoLib applied the keyframed animations, so the sway shows under every animation. A dead animal does not
     * sway. The time is the one Molang's {@code query.life_time} uses for this animal (GeckoLib 5
     * {@code AnimationProcessor.extractControllerStates}: {@code getAnimatableAge()} minus the manager's
     * {@code getFirstRenderTick()}, in ticks), so the sway runs in step with the file's {@code misc.idle}. GeckoLib
     * turns a Bedrock rotation into model space by negating x and y, so the angles are negated to match
     * {@code misc.idle}. The snapshots are new for every render pass, so adding to them does not build up.
     */
    @Override
    public void adjustModelBonesForRender(RenderPassInfo<LivingEntityRenderState> renderPassInfo, BoneSnapshots snapshots) {
        boolean male = renderPassInfo.getGeckolibData(MALE);
        setHidden(snapshots, MALE_CREST, !male);
        setHidden(snapshots, FEMALE_CREST, male);
        if (renderPassInfo.getGeckolibData(DataTickets.IS_DEAD_OR_DYING)) {
            return;
        }
        LivingEntityRenderState state = renderPassInfo.renderState();
        AnimatableManager<?> manager = state.getGeckolibData(DataTickets.ANIMATABLE_MANAGER);
        double seconds = (state.getAnimatableAge() - manager.getFirstRenderTick()) / AnimationChoice.TICKS_PER_SECOND;
        for (int i = 0; i < IdleSway.BONE_COUNT; i++) {
            int bone = i;
            snapshots.ifPresent(IdleSway.bone(bone), snapshot -> {
                snapshot.setRotX(snapshot.getRotX() - (float) Math.toRadians(IdleSway.pitchDegrees(bone, seconds)));
                snapshot.setRotY(snapshot.getRotY() - (float) Math.toRadians(IdleSway.yawDegrees(bone, seconds)));
            });
        }
    }

    /** Hides or shows a bone and its children, as GeckoLib 4's {@code GeoBone.setHidden} did. */
    private static void setHidden(BoneSnapshots snapshots, String bone, boolean hidden) {
        snapshots.ifPresent(bone, snapshot -> snapshot.skipRender(hidden).skipChildrenRender(hidden));
    }

    @Override
    public void submit(LivingEntityRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        super.submit(renderState, poseStack, submitNodeCollector, camera);
        if (renderState.getGeckolibData(GROUP_LEADER)) {
            submitLeaderCrown(renderState, poseStack, submitNodeCollector, camera);
        }
    }

    /** A crown facing the camera just above the hitbox, at full brightness so it reads at night too. */
    private static void submitLeaderCrown(LivingEntityRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.0F, renderState.boundingBoxHeight + CROWN_GAP + CROWN_SIZE / 2F, 0.0F);
        poseStack.mulPose(camera.orientation);
        // On 26.2 the camera rotation turns the quad's front to the viewer with +x to the right, as vanilla's XP orb
        // uses it, so no flip: the 1.20.1 x flip now shows the back, which the text render type culls.
        poseStack.scale(CROWN_SIZE, CROWN_SIZE, CROWN_SIZE);
        collector.submitCustomGeometry(poseStack, RenderTypes.text(LEADER_CROWN), (pose, consumer) -> {
            crownVertex(consumer, pose, -0.5F, -0.5F, 0F, 1F);
            crownVertex(consumer, pose, 0.5F, -0.5F, 1F, 1F);
            crownVertex(consumer, pose, 0.5F, 0.5F, 1F, 0F);
            crownVertex(consumer, pose, -0.5F, 0.5F, 0F, 0F);
        });
        poseStack.popPose();
    }

    private static void crownVertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float u, float v) {
        consumer.addVertex(pose, x, y, 0F).setColor(255, 255, 255, 255).setUv(u, v).setLight(LightCoordsUtil.FULL_BRIGHT);
    }

    /** The base pass colour: the animal's layer 0 colour, as on 1.20.1. */
    @Override
    public int getRenderColor(Dinosaur animatable, Void relatedObject, float partialTick) {
        return animatable.layerColor(0, null);
    }

    @Override
    public float getMotionAnimThreshold(Dinosaur animatable) {
        return 0.005f;
    }
}

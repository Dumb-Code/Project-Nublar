package net.dumbcode.projectnublar.client.screen;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.state.level.CameraRenderState;

/**
 * Draws a {@link GuiEntityGroupRenderState}: all of its entities into the same texture, so one depth
 * buffer sorts them. Modelled on vanilla {@code GuiEntityRenderer}, which draws one entity. Vanilla
 * keeps one picture-in-picture renderer per state class, so two entity draws in one frame would
 * overwrite each other's texture; one group state per frame avoids that.
 *
 * <p>The loaders register this renderer for {@link GuiEntityGroupRenderState} with their
 * picture-in-picture renderer registration.
 */
public class GuiEntityGroupRenderer extends PictureInPictureRenderer<GuiEntityGroupRenderState> {
    private final EntityRenderDispatcher entityRenderDispatcher;

    /** Creates the renderer; the loaders pass the game's entity render dispatcher. */
    public GuiEntityGroupRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        this.entityRenderDispatcher = entityRenderDispatcher;
    }

    @Override
    public Class<GuiEntityGroupRenderState> getRenderStateClass() {
        return GuiEntityGroupRenderState.class;
    }

    /**
     * The pose arrives centred on the box and scaled to the state's scale with z flipped, as for
     * vanilla entities. Each entity is moved from the box centre to its feet and turned there.
     */
    @Override
    protected void renderToTexture(GuiEntityGroupRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        Minecraft.getInstance().gameRenderer.lighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
        CameraRenderState camera = new CameraRenderState();
        float centreX = (state.x0() + state.x1()) / 2.0F;
        float centreY = (state.y0() + state.y1()) / 2.0F;
        for (GuiEntityGroupRenderState.Entry entry : state.entities()) {
            poseStack.pushPose();
            // GUI pixels to blocks: the pose is scaled by (scale, scale, -scale).
            poseStack.translate((entry.x() - centreX) / state.scale(), (entry.y() - centreY) / state.scale(), -entry.z() / state.scale());
            poseStack.mulPose(entry.rotation());
            this.entityRenderDispatcher.submit(entry.renderState(), camera, 0.0, 0.0, 0.0, poseStack, submitNodeCollector);
            poseStack.popPose();
        }
    }

    /** The pose origin is the box centre, as for vanilla entities. */
    @Override
    protected float getTranslateY(int height, int guiScale) {
        return height / 2.0F;
    }

    @Override
    protected String getTextureLabel() {
        return "projectnublar_entity_group";
    }
}

package net.dumbcode.projectnublar.client.renderer;

import com.geckolib.constant.DataTickets;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import net.dumbcode.projectnublar.client.renderer.layer.DinoLayer;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.util.DebugFlags;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

/**
 * One colour layer of a dinosaur ({@link DinoLayer}) for one gender: the model drawn again with the layer texture,
 * tinted by the gene colour, then once more in white, exactly as the 1.20.1 layer did. Both passes blend into the
 * same translucent render type, so the white pass doubles the coverage of partial-alpha texels; the current look
 * depends on it. A layer that is never tinted (basic layer -1) is drawn once, white. Texture baking is the real fix
 * ({@code docs/superpowers/specs/2026-10-08-dino-layer-baking-design.md}).
 *
 * <p>How a pass gets its colour on GeckoLib 5 (javap, 5.5.6 and 5.5.5): {@code GeoRenderer} has one
 * {@code submitRenderTasks(RenderPassInfo, OrderedSubmitNodeCollector, RenderType)}, with no colour argument. It reads
 * {@code RenderPassInfo.renderColor()} when it is called and hands that value to {@code BakedGeoModel.render} through
 * {@code submitCustomGeometry}; {@code renderColor()} reads {@code DataTickets.RENDER_COLOR} from the render state on
 * every call. So this layer puts the tint into {@code RENDER_COLOR}, submits, puts white in, submits again, and
 * restores the base colour. The 1.20.1 tint used the colour's red, green and blue with alpha 1, hence
 * {@link ARGB#opaque}. Each layer submits at its own order (its position + 1, after the base pass at order 0), the
 * way vanilla keeps equipment and banner layers in sequence: within one order vanilla batches custom geometry by
 * render type, so layers of different textures would otherwise draw in any sequence. Both passes of one layer share
 * a render type and keep their submit order.
 */
public class DinoColourLayer extends GeoRenderLayer<Dinosaur, Void, LivingEntityRenderState> {
    private static final int WHITE = 0xFFFFFFFF;

    private final boolean male;
    private final int index;
    private final boolean tinted;
    private final RenderType renderType;

    /**
     * The layer at {@code index} of the {@link Dinosaur#getLayers()} list of one gender ({@code male}), drawn with
     * {@code texture}.
     */
    public DinoColourLayer(DinosaurRenderer renderer, boolean male, int index, DinoLayer layer, Identifier texture) {
        super(renderer);
        this.male = male;
        this.index = index;
        this.tinted = layer.getBasicLayer() != -1;
        this.renderType = RenderTypes.entityTranslucent(texture);
    }

    @Override
    public void submitRenderTask(RenderPassInfo<LivingEntityRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
        LivingEntityRenderState state = renderPassInfo.renderState();
        if (DebugFlags.skipDinoColourLayers || state.isInvisibleToPlayer
                || renderPassInfo.getGeckolibData(DinosaurRenderer.MALE) != this.male
                || !renderPassInfo.getGeckolibData(DinosaurRenderer.LAYER_VISIBLE)[this.index]) {
            return;
        }
        int baseColour = renderPassInfo.renderColor();
        OrderedSubmitNodeCollector collector = renderTasks.order(this.index + 1);
        state.addGeckolibData(DataTickets.RENDER_COLOR, ARGB.opaque(renderPassInfo.getGeckolibData(DinosaurRenderer.LAYER_COLOURS)[this.index]));
        this.renderer.submitRenderTasks(renderPassInfo, collector, this.renderType);
        if (this.tinted) {
            state.addGeckolibData(DataTickets.RENDER_COLOR, WHITE);
            this.renderer.submitRenderTasks(renderPassInfo, collector, this.renderType);
        }
        state.addGeckolibData(DataTickets.RENDER_COLOR, baseColour);
    }
}

package net.dumbcode.projectnublar.client.screen;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionfc;

import java.util.List;

/**
 * A screen element that draws several entities into one picture-in-picture texture with one depth
 * buffer, so they overlap and hide each other by depth, as entities drawn in a 1.20.1 screen did.
 * Vanilla's {@code GuiEntityRenderState} holds one entity, and the 26.2 GUI renderer keeps one
 * renderer, with one texture, per state class ({@code GuiRenderer.pictureInPictureRenderers}): two
 * {@code graphics.entity} calls in one frame draw into the same texture, and only the last entity
 * shows. The Sequencer edit tab uses this state for the dinosaur and the player.
 *
 * <p>The box ({@code x0} to {@code x1}, {@code y0} to {@code y1}) is the visible window: nothing
 * outside it is drawn, so it needs no scissor. The loaders register {@link GuiEntityGroupRenderer}
 * for this state.
 *
 * @param entities the entities, drawn in this order into the shared texture
 * @param scale    GUI pixels per block, the same for every entity
 */
public record GuiEntityGroupRenderState(List<Entry> entities, int x0, int y0, int x1, int y1, float scale)
        implements PictureInPictureRenderState {

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return null;
    }

    @Override
    public @Nullable ScreenRectangle bounds() {
        return PictureInPictureRenderState.getBounds(x0, y0, x1, y1, null);
    }

    /**
     * One entity, placed as 1.20.1 {@code InventoryScreen.renderEntityInInventory} placed it: its feet
     * at GUI ({@code x}, {@code y}), {@code z} GUI units toward the viewer (1.20.1's pose z), and
     * turned by {@code rotation}.
     *
     * @param renderState the entity's render state; full light, no shadow, no outline
     */
    public record Entry(EntityRenderState renderState, int x, int y, int z, Quaternionfc rotation) {
    }
}

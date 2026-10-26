package net.dumbcode.projectnublar.network.c2s;

import java.util.Map;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.dinosaur.DinoData;
import net.dumbcode.projectnublar.api.gene.Genes;
import net.dumbcode.projectnublar.block.entity.SequencerBlockEntity;
import net.dumbcode.projectnublar.menutypes.SequencerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/**
 * Client-to-server: pushes the edit-tab {@link DinoData} into the sequencer. Field order on the
 * wire: the DinoData compound, then the BlockPos (as on 1.20.1).
 *
 * <p>The server checks that the sender has this sequencer's menu open, that the block is a
 * sequencer, and that every value is one the sequencer screen can produce ({@link #isValid}).
 */
public record UpdateEditInfoPacket(DinoData info, BlockPos pos) implements CustomPacketPayload {
    /** The payload id; the same id as on 1.20.1. */
    public static final Type<UpdateEditInfoPacket> TYPE = new Type<>(Constants.modLoc("update_edit_info"));

    /** DinoData travels as its NBT compound until Task 16 gives it a stream codec. */
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateEditInfoPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG.map(DinoData::fromNBT, DinoData::toNBT), UpdateEditInfoPacket::info,
            BlockPos.STREAM_CODEC, UpdateEditInfoPacket::pos,
            UpdateEditInfoPacket::new);

    /** DNA percentages: the main DNA slider of {@code SequencerScreen.init} goes from 0 to 100. */
    static final double MIN_PERCENTAGE = 0;
    static final double MAX_PERCENTAGE = 100;
    /** Gene values: {@code CommonClientClass.GENE_SLIDER_MIN/MAX} and {@code SequencerScreen}'s gene slider. */
    static final double MIN_GENE_VALUE = -100;
    static final double MAX_GENE_VALUE = 100;

    @Override
    public Type<UpdateEditInfoPacket> type() {
        return TYPE;
    }

    /**
     * Applies the edit on the server thread (the loader adapter enqueues it). Does nothing and
     * logs one debug line when a check fails.
     */
    public void handle(ServerPlayer sender) {
        if (!(sender.containerMenu instanceof SequencerMenu menu) || !pos.equals(menu.pos())) {
            Constants.LOG.debug("Ignored update_edit_info from {}: no sequencer menu open at {}", sender.getName().getString(), pos);
            return;
        }
        if (!(sender.level().getBlockEntity(pos) instanceof SequencerBlockEntity sequencer)) {
            Constants.LOG.debug("Ignored update_edit_info from {}: no sequencer at {}", sender.getName().getString(), pos);
            return;
        }
        if (!isValid(info)) {
            Constants.LOG.debug("Ignored update_edit_info from {}: a value is out of range", sender.getName().getString());
            return;
        }
        sequencer.setDinoData(info);
    }

    /**
     * True when every value in {@code info} is one the sequencer's edit tab can write. A modified
     * client could send any number; the server stores these values and writes them onto the DNA
     * test tubes the sequencer makes, so values the screen cannot make are refused.
     *
     * <ul>
     *   <li>Each entity DNA percentage and {@code basePercentage}: {@link #MIN_PERCENTAGE} to
     *       {@link #MAX_PERCENTAGE} (the main DNA slider's range; the other sliders stop at 50).</li>
     *   <li>Each explicit gene value: {@link #MIN_GENE_VALUE} to {@link #MAX_GENE_VALUE} (the gene
     *       slider's range), and the gene must be known; an unknown gene name decodes to
     *       {@code null}, which would crash the sequencer's save.</li>
     *   <li>Each layer colour: alpha 0 (the default {@code 0xFFFFFF}) or alpha 0xFF (every colour
     *       the colour wheel and the dye-colour picker send). Any RGB value is allowed.</li>
     * </ul>
     *
     * <p>{@code NaN} fails every range check, so it is refused too.
     */
    static boolean isValid(DinoData info) {
        if (!inRange(info.getBasePercentage(), MIN_PERCENTAGE, MAX_PERCENTAGE)) {
            return false;
        }
        for (double percentage : info.getEntityPercentages().values()) {
            if (!inRange(percentage, MIN_PERCENTAGE, MAX_PERCENTAGE)) {
                return false;
            }
        }
        for (Map.Entry<Genes.Gene, Double> gene : info.getAdvancedGenes().entrySet()) {
            if (gene.getKey() == null || !inRange(gene.getValue(), MIN_GENE_VALUE, MAX_GENE_VALUE)) {
                return false;
            }
        }
        for (int colour : info.getLayerColors()) {
            int alpha = colour >>> 24;
            if (alpha != 0 && alpha != 0xFF) {
                return false;
            }
        }
        return true;
    }

    private static boolean inRange(double value, double min, double max) {
        return value >= min && value <= max;
    }
}

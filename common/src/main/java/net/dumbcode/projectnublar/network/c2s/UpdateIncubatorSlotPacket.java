package net.dumbcode.projectnublar.network.c2s;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.block.entity.IncubatorBlockEntity;
import net.dumbcode.projectnublar.menutypes.IncubatorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/**
 * Client-to-server: moves an incubator egg slot. {@code index} is the menu slot index; the block
 * entity subtracts 1 (see {@link IncubatorBlockEntity#updateSlot}). Field order on the wire:
 * BlockPos, index, x, y.
 *
 * <p>The server checks that the sender has this incubator's menu open, that the block is an
 * incubator, and that the index and position are ones the incubator screen can send
 * ({@link #isValid}).
 */
public record UpdateIncubatorSlotPacket(BlockPos pos, int index, int x, int y) implements CustomPacketPayload {
    /** The payload id; the same id as on 1.20.1. */
    public static final Type<UpdateIncubatorSlotPacket> TYPE = new Type<>(Constants.modLoc("update_incubator_slot"));

    /** The position, then index, x and y as var-ints. */
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateIncubatorSlotPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, UpdateIncubatorSlotPacket::pos,
            ByteBufCodecs.VAR_INT, UpdateIncubatorSlotPacket::index,
            ByteBufCodecs.VAR_INT, UpdateIncubatorSlotPacket::x,
            ByteBufCodecs.VAR_INT, UpdateIncubatorSlotPacket::y,
            UpdateIncubatorSlotPacket::new);

    /** Menu slot 0 is the plant-matter input ({@link IncubatorMenu}); the egg slots follow it. */
    static final int FIRST_EGG_MENU_INDEX = 1;
    /** The incubator has at most {@link IncubatorBlockEntity#EGG_SLOT_COUNT} egg slots. */
    static final int LAST_EGG_MENU_INDEX = IncubatorBlockEntity.EGG_SLOT_COUNT;
    /**
     * The egg bed that the incubator screen draws ({@link IncubatorMenu#BED_OFFSET} and the bed
     * size), in screen-relative pixels: x from {@code EGG_AREA_START} to {@code EGG_AREA_END_X - 1},
     * y from {@code EGG_AREA_START} to {@code EGG_AREA_END_Y - 1}. The screen places eggs only
     * inside it.
     */
    static final int EGG_AREA_START = IncubatorMenu.BED_OFFSET;
    static final int EGG_AREA_END_X = IncubatorMenu.BED_OFFSET + IncubatorMenu.BED_WIDTH;
    static final int EGG_AREA_END_Y = IncubatorMenu.BED_OFFSET + IncubatorMenu.BED_HEIGHT;

    @Override
    public Type<UpdateIncubatorSlotPacket> type() {
        return TYPE;
    }

    /**
     * Moves the egg slot on the server thread (the loader adapter enqueues it). Does nothing and
     * logs one debug line when a check fails; on 1.20.1 a missing block entity threw a
     * {@code NullPointerException} here.
     */
    public void handle(ServerPlayer sender) {
        if (!(sender.containerMenu instanceof IncubatorMenu menu) || !pos.equals(menu.pos())) {
            Constants.LOG.debug("Ignored update_incubator_slot from {}: no incubator menu open at {}", sender.getName().getString(), pos);
            return;
        }
        if (!(sender.level().getBlockEntity(pos) instanceof IncubatorBlockEntity incubator)) {
            Constants.LOG.debug("Ignored update_incubator_slot from {}: no incubator at {}", sender.getName().getString(), pos);
            return;
        }
        if (!isValid(index, x, y)) {
            Constants.LOG.debug("Ignored update_incubator_slot from {}: slot {} at ({}, {}) is out of range", sender.getName().getString(), index, x, y);
            return;
        }
        incubator.updateSlot(index, x, y);
        incubator.updateBlock();
    }

    /**
     * True when the incubator screen can send this slot move. A modified client could send any
     * index (an index outside the egg slots throws in {@link IncubatorBlockEntity#updateSlot}) or
     * any position (an egg drawn off the bed).
     *
     * <ul>
     *   <li>{@code index}: {@link #FIRST_EGG_MENU_INDEX} to {@link #LAST_EGG_MENU_INDEX}.</li>
     *   <li>{@code x}, {@code y}: inside the egg bed ({@link #EGG_AREA_START} up to
     *       {@link #EGG_AREA_END_X} and {@link #EGG_AREA_END_Y}, both exclusive), or exactly the
     *       empty slot's position {@code IncubatorBlockEntity.Slot.EMPTY} (0, -100), where the
     *       screen parks a slot when the player takes its egg out.</li>
     * </ul>
     */
    static boolean isValid(int index, int x, int y) {
        if (index < FIRST_EGG_MENU_INDEX || index > LAST_EGG_MENU_INDEX) {
            return false;
        }
        boolean parked = x == IncubatorBlockEntity.Slot.EMPTY.x() && y == IncubatorBlockEntity.Slot.EMPTY.y();
        boolean onBed = x >= EGG_AREA_START && x < EGG_AREA_END_X && y >= EGG_AREA_START && y < EGG_AREA_END_Y;
        return parked || onBed;
    }
}

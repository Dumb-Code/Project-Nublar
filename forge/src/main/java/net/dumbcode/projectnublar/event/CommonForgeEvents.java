package net.dumbcode.projectnublar.event;

import net.dumbcode.projectnublar.block.api.fence.BlockConnectableBase;
import net.dumbcode.projectnublar.block.api.fence.ConnectableBlockEntity;
import net.dumbcode.projectnublar.block.api.fence.Connection;
import net.dumbcode.projectnublar.data.BehaviourDataReloadListener;
import net.dumbcode.projectnublar.data.DietReloadListener;
import net.dumbcode.projectnublar.data.FossilConfigReloadListener;
import net.dumbcode.projectnublar.data.GeneDataReloadListener;
import net.dumbcode.projectnublar.entity.DeathMessageHandler;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.registry.ItemInit;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Forge-only game-bus events: datapack reload listeners (registered on no other loader - the
 * Fabric side has no equivalent) and the wire-spool fence-repair interaction.
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonForgeEvents {

    @SubscribeEvent
    public static void onReloadListeners(AddReloadListenerEvent event){
        event.addListener(new GeneDataReloadListener());
        event.addListener(new BehaviourDataReloadListener());
        event.addListener(new DietReloadListener());
        event.addListener(new FossilConfigReloadListener());
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event){
        if(event.getEntity() instanceof Dinosaur dinosaur) {
            DeathMessageHandler.onLivingDeath(dinosaur, event.getSource());
        }
    }

    /** Right-clicking a broken wire with a wire spool repairs one connection. */
    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        Level world = event.getLevel();
        if (world.isClientSide) {
            return;
        }
        Direction side = event.getFace();
        if (side != null && !event.getItemStack().isEmpty() && event.getItemStack().getItem() == ItemInit.WIRE_SPOOL.get()) {
            BlockEntity tile = world.getBlockEntity(event.getPos().relative(side));
            if (tile instanceof ConnectableBlockEntity) {
                ConnectableBlockEntity cb = (ConnectableBlockEntity) tile;
                if (side.getAxis() == Direction.Axis.Y) {
                    repairVerticallyNearestWire(event, cb, side);
                } else {
                    repairFirstBrokenWire(event, cb);
                }
            }
        }
    }

    /**
     * Repairs the broken wire vertically closest to the clicked face. The
     * {@code Double.MIN_VALUE}/{@code MAX_VALUE} seeds and the
     * {@code side == DOWN == yin > yRef} comparison are frozen heuristics.
     */
    private static void repairVerticallyNearestWire(
            PlayerInteractEvent.RightClickBlock event, ConnectableBlockEntity cb, Direction side) {
        double yRef = side == Direction.DOWN ? Double.MIN_VALUE : Double.MAX_VALUE;
        Connection ref = null;
        for (Connection connection : cb.getConnections()) {
            if (connection.isBroken()) {
                double[] in = connection.getIn();
                double yin = (in[4] + in[5]) / 2D;
                if (side == Direction.DOWN == yin > yRef) {
                    yRef = yin;
                    ref = connection;
                }
            }
        }
        if (ref != null) {
            ref.setBroken(false);
            if (cb instanceof BlockEntity blockEntity) {
                blockEntity.setChanged();
            }
            event.setCanceled(true);
            BlockConnectableBase.placeEffect(event.getEntity(), event.getHand(), event.getLevel(), event.getPos());
        }
    }

    private static void repairFirstBrokenWire(
            PlayerInteractEvent.RightClickBlock event, ConnectableBlockEntity cb) {
        for (Connection connection : cb.getConnections()) {
            if (connection.isBroken()) {
                connection.setBroken(false);
                if (cb instanceof BlockEntity blockEntity) {
                    blockEntity.setChanged();
                }
                event.setCanceled(true);
                BlockConnectableBase.placeEffect(event.getEntity(), event.getHand(), event.getLevel(), event.getPos());
                break;
            }
        }
    }
}

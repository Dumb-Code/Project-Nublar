package net.dumbcode.projectnublar.event;

import net.dumbcode.projectnublar.block.api.BlockConnectableBase;
import net.dumbcode.projectnublar.block.api.ConnectableBlockEntity;
import net.dumbcode.projectnublar.block.api.Connection;
import net.dumbcode.projectnublar.data.BehaviourDataReloadListener;
import net.dumbcode.projectnublar.data.DietReloadListener;
import net.dumbcode.projectnublar.data.FossilConfigReloadListener;
import net.dumbcode.projectnublar.data.GeneDataReloadListener;
import net.dumbcode.projectnublar.entity.DeathMessageHandler;
import net.dumbcode.projectnublar.entity.Dinosaur;
import net.dumbcode.projectnublar.init.BlockInit;
import net.dumbcode.projectnublar.init.ItemInit;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        Level world = event.getLevel();
        Direction side = event.getFace();
        if (side != null && !event.getItemStack().isEmpty() && event.getItemStack().getItem() == ItemInit.WIRE_SPOOL.get()) {
            BlockEntity tile = world.getBlockEntity(event.getPos().relative(side));
            if (tile instanceof ConnectableBlockEntity) {
                ConnectableBlockEntity cb = (ConnectableBlockEntity) tile;
                if (side.getAxis() == Direction.Axis.Y) {
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
                        event.setCanceled(true);
                        BlockConnectableBase.placeEffect(event.getEntity(), event.getHand(), event.getLevel(), event.getPos());
                    }
                } else {
                    for (Connection connection : cb.getConnections()) {
                        if (connection.isBroken()) {
                            connection.setBroken(false);
                            event.setCanceled(true);
                            BlockConnectableBase.placeEffect(event.getEntity(), event.getHand(), event.getLevel(), event.getPos());
                            break;
                        }
                    }
                }
            }
        }
    }

}

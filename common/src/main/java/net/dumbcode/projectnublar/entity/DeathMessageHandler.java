package net.dumbcode.projectnublar.entity;

import net.dumbcode.projectnublar.entity.dinosaur.AbstractDinosaur;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;



public class DeathMessageHandler {
    public static void onLivingDeath(AbstractDinosaur dinosaur, DamageSource source){
        if(!dinosaur.level().isClientSide()){
                Component message = Component.literal(dinosaur.getName().getString() + reasonForDeath(dinosaur));
                MinecraftServer server = ((ServerLevel) dinosaur.level()).getServer();
                server.getPlayerList().broadcastSystemMessage(message, false);
        }
    }
    public static String reasonForDeath(AbstractDinosaur dinosaur){
        String reason = " has died.";
/*
        if(dinosaur.isDehydrated() && dinosaur.isStarving() && dinosaur.isExhausted()){
            reason = " has died of Exposure.";
        } else if(dinosaur.isDehydrated() && dinosaur.isStarving()) {
            reason = " has died from malnutrition.";
        } else if(dinosaur.isDehydrated()){
            reason = " has died from extreme thirst.";
        } else if(dinosaur.isStarving()){
            reason = " has died from starvation.";
        }

 */
        return reason;
    }
}

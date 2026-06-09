package net.dumbcode.projectnublar.item;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.dinosaur.DNAData;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.util.DinoNeedsUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DebugStick extends Item {

    public DebugStick(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        if(interactionTarget instanceof Dinosaur dinosaur) {
            if (!dinosaur.level().isClientSide()) {
                Component dinoName = Component.literal(dinosaur.getName().getString());
                Component groupStatus = Component.literal("Has Group: " + dinosaur.hasGroup());
                Component healthStatus = Component.literal("Health: " + dinosaur.getHealth());
                Component hungerStatus = Component.literal("Hunger: " + DinoNeedsUtils.getCurrentHunger(dinosaur));
                Component thirstStatus = Component.literal("Thirst: " + DinoNeedsUtils.getCurrentThirst(dinosaur));
                Component staminaStatus = Component.literal("Stamina: " + DinoNeedsUtils.getCurrentStamina(dinosaur));
                MinecraftServer server = ((ServerLevel) dinosaur.level()).getServer();
                server.getPlayerList().broadcastSystemMessage(dinoName, false);
                server.getPlayerList().broadcastSystemMessage(groupStatus, false);
                server.getPlayerList().broadcastSystemMessage(healthStatus, false);
                server.getPlayerList().broadcastSystemMessage(hungerStatus, false);
                server.getPlayerList().broadcastSystemMessage(thirstStatus, false);
                server.getPlayerList().broadcastSystemMessage(staminaStatus, false);
            }
        }
        return super.interactLivingEntity(stack, player, interactionTarget, usedHand);
    }

    @Override
    public Component getName(ItemStack pStack) {
        return Component.literal("Dev Debug Stick");
    }
}

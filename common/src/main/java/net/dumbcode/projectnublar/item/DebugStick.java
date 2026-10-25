package net.dumbcode.projectnublar.item;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.dinosaur.DNAData;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social.DinoGroup;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social.Dominance;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social.GroupManager;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social.HerdTimings;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.util.DebugFlags;
import net.dumbcode.projectnublar.util.DinoNeedsUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The lead's in-game probe. Its combos (none hurts the animal):
 * <ul>
 *   <li><b>Use on a dinosaur</b> (stick in the main hand): the info dump in chat and the log.</li>
 *   <li><b>Sneak-use on a dinosaur</b> (main hand): the herd travel spacing toggle (tight everywhere, or open in the open).</li>
 *   <li><b>Use on a dinosaur with the stick in the off hand</b>, main hand empty, sneaking or not: the herd
 *       debug visuals of that animal's herd on or off ({@code HerdDebugVisuals}; "Herd visuals: on/off").
 *       The off hand is only tried after the empty main hand passes, so the main-hand combos do not fire.</li>
 *   <li><b>Sneak-use in the air</b>: the dinosaur colour layers off and on (client, frame-rate measuring).</li>
 * </ul>
 */
public class DebugStick extends Item {

    public DebugStick(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        if (interactionTarget instanceof Dinosaur dinosaur && usedHand == InteractionHand.OFF_HAND) {
            // Off-hand use on a dinosaur: switch its herd's debug visuals. Consumed on both sides.
            if (!dinosaur.level().isClientSide()) {
                DinoGroup group = GroupManager.groupOf(dinosaur);
                if (group == null) {
                    player.displayClientMessage(Component.literal("Herd visuals: no herd"), true);
                } else {
                    group.setDebugVisuals(!group.debugVisuals());
                    player.displayClientMessage(Component.literal("Herd visuals: " + (group.debugVisuals() ? "on" : "off")), true);
                }
            }
            return InteractionResult.sidedSuccess(dinosaur.level().isClientSide());
        }
        if (interactionTarget instanceof Dinosaur dinosaur && player.isShiftKeyDown()) {
            // Sneak-use on a dinosaur: switch the herd travel spacing. Consumed on both sides, so the
            // sneak-use in the air (colour layers) does not also fire.
            if (!dinosaur.level().isClientSide()) {
                DebugFlags.fixedTravelSpacing = !DebugFlags.fixedTravelSpacing;
                player.displayClientMessage(Component.literal("Herd travel spacing: "
                        + (DebugFlags.fixedTravelSpacing ? "tight everywhere (comparing)" : "open in the open, tight when tight")), true);
            }
            return InteractionResult.sidedSuccess(dinosaur.level().isClientSide());
        }
        if(interactionTarget instanceof Dinosaur dinosaur) {
            if (!dinosaur.level().isClientSide()) {
                ServerLevel level = (ServerLevel) dinosaur.level();
                List<String> lines = new ArrayList<>();
                lines.add(dinosaur.getName().getString() + " #" + dinosaur.getId() + " at " + dinosaur.blockPosition().toShortString());
                DinoGroup group = GroupManager.groupOf(dinosaur);
                if (group == null) {
                    lines.add("Group: none");
                } else {
                    Dinosaur leader = GroupManager.leaderOf(group, level);
                    lines.add("Group: " + group.size() + " members, leader: " + (dinosaur.isGroupLeader() ? "yes" : "no")
                            + ", dominance: " + String.format("%.3f", Dominance.of(dinosaur))
                            + (leader == null || leader == dinosaur ? "" : ", leader #" + leader.getId()
                            + " at " + String.format("%.1f", dinosaur.distanceTo(leader)) + " blocks"));
                    lines.add(group.describe(dinosaur.getUUID()));
                    if (leader == dinosaur) {
                        lines.add(group.describeSchedule(level.getGameTime()));
                    }
                }
                // A leash restricts the animal to 5 blocks round the holder; journeys cannot leave a restriction.
                lines.add("Leashed: " + dinosaur.isLeashed() + ", restriction: " + (dinosaur.hasRestriction()
                        ? "centre " + dinosaur.getRestrictCenter().toShortString() + ", radius " + dinosaur.getRestrictRadius()
                        : "none"));
                addMovementDebug(dinosaur, level, lines);
                lines.add(String.format("Herd AI: average %.3f ms/tick, worst %.3f ms (last %d ticks with herd work)",
                        HerdTimings.averageMillis(), HerdTimings.worstMillis(), 600));
                lines.add("Health: " + dinosaur.getHealth() + ", hunger: " + DinoNeedsUtils.getCurrentHunger(dinosaur)
                        + ", thirst: " + DinoNeedsUtils.getCurrentThirst(dinosaur)
                        + ", stamina: " + DinoNeedsUtils.getCurrentStamina(dinosaur));

                MinecraftServer server = level.getServer();
                for (String line : lines) {
                    server.getPlayerList().broadcastSystemMessage(Component.literal(line), false);
                    Constants.LOG.info("[debug stick] {}", line);
                }
            }
        }
        return super.interactLivingEntity(stack, player, interactionTarget, usedHand);
    }

    /** Sneak and use in the air: switches the dinosaur colour layers off and on, to measure their frame-rate cost. */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!player.isShiftKeyDown()) {
            return super.use(level, player, hand);
        }
        if (level.isClientSide()) {
            DebugFlags.skipDinoColourLayers = !DebugFlags.skipDinoColourLayers;
            player.displayClientMessage(Component.literal("Dinosaur colour layers: "
                    + (DebugFlags.skipDinoColourLayers ? "off (measuring)" : "on")), true);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    /** What the brain and navigation are doing, to find out why an animal stands still. */
    private static void addMovementDebug(Dinosaur dinosaur, ServerLevel level, List<String> lines) {
        Brain<?> brain = dinosaur.getBrain();
        lines.add("Running: " + brain.getRunningBehaviors().stream()
                .map(behaviour -> behaviour.getClass().getSimpleName()).collect(Collectors.joining(", ")));

        WalkTarget walkTarget = brain.getMemory(MemoryModuleType.WALK_TARGET).orElse(null);
        lines.add(walkTarget == null ? "Walk target: none"
                : "Walk target: " + walkTarget.getTarget().currentBlockPosition().toShortString()
                + " (close enough " + walkTarget.getCloseEnoughDist() + ", speed " + walkTarget.getSpeedModifier() + ")");

        Path path = dinosaur.getNavigation().getPath();
        lines.add(path == null ? "Path: none"
                : "Path: " + path.getNextNodeIndex() + "/" + path.getNodeCount() + " nodes, reaches target: " + path.canReach()
                + ", ends at " + path.getTarget().toShortString() + ", navigation done: " + dinosaur.getNavigation().isDone());

        lines.add("Can't reach walk target since: " + brain.getMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)
                .map(tick -> (level.getGameTime() - tick) + " ticks").orElse("-"));

        lines.add("Idle: " + dinosaur.isIdle() + " (eating " + dinosaur.isEating() + ", drinking " + dinosaur.isDrinking()
                + ", resting " + dinosaur.isResting() + ", sitting " + dinosaur.isSitting()
                + ", roaring " + dinosaur.isRoaring() + ", attacking " + dinosaur.isAttacking() + ")");
    }

    @Override
    public Component getName(ItemStack pStack) {
        return Component.literal("Dev Debug Stick");
    }
}

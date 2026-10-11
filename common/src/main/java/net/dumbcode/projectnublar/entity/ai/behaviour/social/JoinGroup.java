package net.dumbcode.projectnublar.entity.ai.behaviour.social;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dumbcode.projectnublar.api.Genes;
import net.dumbcode.projectnublar.entity.dinosaur.CarnivoreDinosaur;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.init.GeneInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.EntityRetrievalUtil;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Forms groups and merges small ones.
 * <ul>
 *   <li>A groupless animal joins the nearest group of its species that has room. If there is none, it
 *       starts a group with the nearest groupless animal of its species.</li>
 *   <li>A leader merges its group into a nearby group of its species if that group's leader outranks it
 *       and all its members fit. Only the lower-ranked leader acts, so two groups never swap back and forth.</li>
 *   <li>Followers do nothing here.</li>
 * </ul>
 * Babies stay out of groups because they follow a parent. Tamed animals stay out because they follow
 * their owner.
 * <p>
 * The behaviour scans for animals itself, and only for groupless animals and leaders. An always-on
 * sensor would scan for every follower too, every second, for nothing. The scan radius grows with body
 * width, so it measures roughly the same gap between bodies for a raptor and a sauropod.
 */
public class JoinGroup<E extends Dinosaur> extends ExtendedBehaviour<E> {

    private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of();

    private double radius = 24D;

    public JoinGroup() {
        // Groupless animals look every 2 seconds. Groups look for a merge every 10 seconds.
        cooldownFor(entity -> GroupManager.groupOf(entity) == null ? 40 : 200);
    }

    /** Scan radius in blocks, before the body-width allowance. Default 24. */
    public JoinGroup<E> radius(double radius) {
        this.radius = radius;
        return this;
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        return canBeGrouped(entity);
    }

    @Override
    protected void start(E entity) {
        ServerLevel level = (ServerLevel) entity.level();
        GroupManager manager = GroupManager.of(level);
        DinoGroup own = manager.groupOf(entity.getUUID());
        if (own != null && !own.isLeader(entity.getUUID())) {
            return;
        }

        double reach = this.radius + 2D * entity.getBbWidth();
        List<Dinosaur> nearby = EntityRetrievalUtil.getEntities(level, entity.getBoundingBox().inflate(reach),
                other -> other != entity && other.getType() == entity.getType()
                        && other instanceof Dinosaur dinosaur && canBeGrouped(dinosaur));
        nearby.sort(Comparator.comparingDouble(entity::distanceToSqr));

        DinoGroup.Roster roster = GroupManager.roster(level);
        if (own == null) {
            joinNearest(manager, roster, entity, nearby);
        } else {
            mergeIntoStronger(manager, roster, entity, own, nearby);
        }
    }

    private static void joinNearest(GroupManager manager, DinoGroup.Roster roster, Dinosaur entity,
                                    List<Dinosaur> nearby) {
        Set<DinoGroup> tried = new HashSet<>();
        Dinosaur partner = null;
        for (Dinosaur other : nearby) {
            DinoGroup group = manager.groupOf(other.getUUID());
            if (group == null) {
                if (partner == null) {
                    partner = other;
                }
            } else if (tried.add(group) && manager.tryJoin(group, entity.getUUID(), roster)) {
                return;
            }
        }
        if (partner != null) {
            manager.create(entity.getUUID(), partner.getUUID(), capacity(entity), roster);
        }
    }

    private static void mergeIntoStronger(GroupManager manager, DinoGroup.Roster roster, Dinosaur leader,
                                          DinoGroup own, List<Dinosaur> nearby) {
        float dominance = Dominance.of(leader);
        Set<DinoGroup> tried = new HashSet<>();
        for (Dinosaur other : nearby) {
            DinoGroup group = manager.groupOf(other.getUUID());
            if (group == null || group == own || !tried.add(group)) {
                continue;
            }
            UUID otherLeader = group.leader(roster);
            if (otherLeader != null
                    && Dominance.outranks(roster.dominance(otherLeader), otherLeader, dominance, leader.getUUID())
                    && manager.tryMerge(own, group, roster)) {
                return;
            }
        }
    }

    private static boolean canBeGrouped(Dinosaur dinosaur) {
        return dinosaur.isAlive() && !dinosaur.isBaby() && !dinosaur.isTame()
                && dinosaur.getDinoBehaviour().canFormGroup();
    }

    /** Species {@code group_size}, scaled by the herd or pack size gene (-100% .. +100%). At least 2. */
    private static int capacity(Dinosaur founder) {
        Genes.Gene gene = founder instanceof CarnivoreDinosaur ? GeneInit.PACK_SIZE.get() : GeneInit.HERD_SIZE.get();
        double scale = 1D + founder.getDinoData().getGeneValue(gene) / 100D;
        return Math.max(2, (int) Math.round(founder.getDinoBehaviour().groupSize() * scale));
    }
}

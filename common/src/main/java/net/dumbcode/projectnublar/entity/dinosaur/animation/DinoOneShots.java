package net.dumbcode.projectnublar.entity.dinosaur.animation;

import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social.DinoGroup;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social.GroupManager;
import net.dumbcode.projectnublar.entity.dinosaur.ai.behavior.social.HerdState;
import net.dumbcode.projectnublar.entity.dinosaur.CarnivoreDinosaur;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.registry.MemoryModuleTypeInit;
import net.dumbcode.projectnublar.util.DinoAnimationLengths;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The server's one-shot animation triggers for one dinosaur (fix P10). The current action goes to the client in
 * the synced byte {@link Dinosaur#ONE_SHOT}; the server clears it after the animation length.
 * <ul>
 *   <li><b>Calling</b>: the herd leader calls when it starts a roaming journey ({@link #beginJourneyCall});
 *       the journey's first walk waits for the call, at most {@link OneShotAction#JOURNEY_CALL_MAX_WAIT_TICKS}
 *       ({@code HerdLeading} counts the wait down). A species without a call sets off at once.</li>
 *   <li><b>Sentry</b>: a check every 200-400 ticks with a 30% chance, while the herd leader stands in GRAZE and
 *       is not eating, drinking or resting. It ends early when that stops being true.</li>
 *   <li><b>Startled</b>: a survival or adventure player, a hostile mob, or a carnivore dinosaur from another herd
 *       comes within {@link StartleWatch#RADIUS} ({@link StartleWatch}: an approach, not a standing neighbour).
 *       It reads the brain's nearby-entity memory, not the world. Not while attacking, dead or resting.</li>
 * </ul>
 * A one-shot does not stop movement; only the journey-start call holds the leader's first walk.
 */
public final class DinoOneShots {
    private static final long UNSET = -1L;
    private static final double STARTLE_RADIUS_SQR = StartleWatch.RADIUS * StartleWatch.RADIUS;

    private final Dinosaur dinosaur;
    private final OneShotState state = new OneShotState();
    private final StartleWatch startle = new StartleWatch();
    private long nextSentryCheck = UNSET;
    private int journeyCallWait;

    public DinoOneShots(Dinosaur dinosaur) {
        this.dinosaur = dinosaur;
    }

    /** Once per server tick, dead or alive. */
    public void serverTick() {
        if (this.dinosaur.isDeadOrDying()) {
            if (this.state.current() != OneShotAction.NONE) {
                this.state.clear();
                sync();
            }
            return;
        }
        if (this.state.current() == OneShotAction.SENTRY && !sentryAllowed()) {
            this.state.clear();
            sync();
        } else if (this.state.tick()) {
            sync();
        }
        long now = this.dinosaur.level().getGameTime();
        if (now >= this.nextSentryCheck) {
            if (this.nextSentryCheck != UNSET && sentryAllowed() && OneShotAction.sentryRollPasses(this.dinosaur.getRandom().nextDouble())) {
                start(OneShotAction.SENTRY);
            }
            this.nextSentryCheck = OneShotAction.nextSentryCheck(now, this.dinosaur.getRandom().nextDouble());
        }
        if (this.dinosaur.tickCount % StartleWatch.CHECK_TICKS == 0) {
            checkStartle(now);
        }
    }

    /**
     * The herd leader starts a roaming journey: it calls, and the journey's first walk waits.
     *
     * @return the ticks the first walk waits, or 0 when there is no call (the species has none, or a startle plays)
     */
    public int beginJourneyCall() {
        int callTicks = start(OneShotAction.CALLING);
        this.journeyCallWait = OneShotAction.journeyWaitTicks(callTicks);
        return this.journeyCallWait;
    }

    /** A roaming journey's first walk still waits for the call. */
    public boolean journeyCallWaiting() {
        return this.journeyCallWait > 0;
    }

    /** Counts the journey-start wait down by one tick. @return true on the tick the wait ends */
    public boolean countDownJourneyCall() {
        return this.journeyCallWait > 0 && --this.journeyCallWait == 0;
    }

    public void cancelJourneyCall() {
        this.journeyCallWait = 0;
    }

    /** Starts {@code action} for its animation length. @return the length in ticks, or 0 when it did not start */
    private int start(OneShotAction action) {
        int ticks = action.lengthTicks(DinoAnimationLengths.lengthTicks(this.dinosaur.getType(), action.choice()));
        if (!this.state.start(action, ticks)) {
            return 0;
        }
        sync();
        return ticks;
    }

    private void sync() {
        this.dinosaur.getEntityData().set(Dinosaur.ONE_SHOT, this.state.current().id());
    }

    /** The herd leader stands in GRAZE and is not eating, drinking, resting, roaring or attacking. */
    private boolean sentryAllowed() {
        Dinosaur dino = this.dinosaur;
        if (!dino.isIdle() || resting(dino) || BrainUtils.hasMemory(dino, MemoryModuleType.WALK_TARGET)
                || dino.getNavigation().isInProgress()) {
            return false;
        }
        DinoGroup group = GroupManager.groupOf(dino);
        return group != null && GroupManager.isLeader(dino) && group.state() == HerdState.GRAZE;
    }

    private void checkStartle(long now) {
        Dinosaur dino = this.dinosaur;
        this.startle.beginCheck();
        List<LivingEntity> nearby = BrainUtils.getMemory(dino, MemoryModuleType.NEAREST_LIVING_ENTITIES);
        if (nearby != null) {
            DinoGroup group = GroupManager.groupOf(dino);
            for (LivingEntity other : nearby) {
                if (other.isAlive() && dino.distanceToSqr(other) < STARTLE_RADIUS_SQR && isThreat(other, group)) {
                    this.startle.inside(other.getId());
                }
            }
        }
        boolean allowed = !dino.isAttacking() && !BrainUtils.hasMemory(dino, MemoryModuleType.ATTACK_TARGET) && !resting(dino);
        if (this.startle.endCheck(now, allowed)) {
            start(OneShotAction.STARTLED);
        }
    }

    /** A survival or adventure player, a hostile mob, or a carnivore dinosaur that is not in this animal's herd. */
    private static boolean isThreat(LivingEntity other, @Nullable DinoGroup group) {
        if (other instanceof Player player) {
            return !player.isCreative() && !player.isSpectator();
        }
        if (other instanceof Enemy) {
            return true;
        }
        return other instanceof CarnivoreDinosaur carnivore && (group == null || GroupManager.groupOf(carnivore) != group);
    }

    /** Sitting down, resting or getting up. */
    private static boolean resting(Dinosaur dino) {
        return dino.isSitting() || dino.isResting() || dino.isRising()
                || BrainUtils.hasMemory(dino, MemoryModuleTypeInit.IS_RESTING.get());
    }
}

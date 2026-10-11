package net.dumbcode.projectnublar.entity.dinosaur.animation;

import java.util.Map;
import java.util.function.Predicate;

/**
 * Which animation each action state plays (fix P10). Pure: names only, no GeckoLib or Minecraft.
 * <p>
 * Species name their animations differently, so each state has an ordered list of candidate names. A state
 * plays the first candidate the species' animation file has, or (for variants: attack, eat, drink, idle) a
 * random one of the candidates it has. A state whose species has no candidate plays its {@link #resolve fallback}:
 * run falls back to walk; eat, drink and get up fall back to the idle choice, so the model does not freeze in its
 * base pose. Dead has no fallback: a corpse plays nothing rather than idles (controller ruling). A get-up fallback,
 * and every idle while the herd travels, plays the main idle only ({@link #mainIdleOnly}, P13b).
 * {@code DinoAnimationAvailability} answers "does the species have this name" and caches the
 * result per species and state; {@code DinoAnimator} plays the result.
 */
public enum AnimationChoice {
    WALK(Loop.LOOP, "move.walk"),
    /** {@code "Sprint "} with a trailing space is how {@code triceratops.animation.json} names its run. */
    RUN(Loop.LOOP, "run", "Sprint", "Sprint "),
    ATTACK(Loop.LOOP, "attack1", "attack2", "Attack2"),
    /** Plays once and holds (P12a): the state outlasts the animation by a tick or two, and a loop restarted it. */
    FLINCH(Loop.HOLD, "flinch"),
    /** The roar state ({@code ThreatDisplay}). */
    ROAR(Loop.LOOP, "roar", "calling", "call"),
    /** One-shot: the herd leader calls before a roaming journey. */
    CALL(Loop.HOLD, "calling", "call"),
    /** One-shot: the herd leader keeps watch while the herd grazes. */
    SENTRY(Loop.HOLD, "sentry"),
    /** One-shot: a threat came close. */
    STARTLED(Loop.HOLD, "startled"),
    /**
     * Standing idle. The first name is the main idle, picked most ({@link #MAIN_IDLE_WEIGHT}) so the variants stay
     * occasional; the model's always-on sway ({@code IdleSway}) shows the breathing motion in every idle (P12a).
     */
    IDLE(Loop.FILE, "misc.idle", "idle2", "sniffair", "sniffground", "lookleft", "lookright", "lookaround",
            "shakehead", "headshake", "head_shake", "shakebody", "scratch"),
    /** Sitting down. The file's own loop type: some species hold the last frame, some loop. */
    SIT(Loop.FILE, "rest"),
    REST(Loop.LOOP, "restidle"),
    SWIM(Loop.LOOP, "swim"),
    EAT(Loop.LOOP, "eat1", "eat2"),
    DRINK(Loop.LOOP, "drink", "drink2", "Drinking2"),
    GET_UP(Loop.FILE, "getup"),
    DEAD(Loop.HOLD, "dead");

    /** How a state's animation repeats. */
    public enum Loop {
        /** Loops while the state lasts. */
        LOOP,
        /** The loop type written in the animation file. */
        FILE,
        /** Plays once and holds its last frame until the state ends. */
        HOLD
    }

    /** Animation length answer: the species' file is loaded and has none of the candidates. */
    public static final int LENGTH_NOT_DEFINED = -1;
    /** Animation length answer: the file is not loaded (a dedicated server, or before the first resource load). */
    public static final int LENGTH_UNKNOWN = 0;
    /** Animation file lengths are in seconds; the game counts ticks. */
    public static final int TICKS_PER_SECOND = 20;
    /** Flinch length when the file cannot be read: the Gallimimus flinch, 1.21 s. */
    public static final int FLINCH_FALLBACK_TICKS = 25;
    /**
     * Weight of the main idle ({@code misc.idle}) in the idle pick: the highest, so variants stay occasional (lead,
     * playtest 5: sniffair and look left/right played too often at 1 against 2). For the Gallimimus a variant now
     * starts about every 16 s and plays about a quarter of the idle time, against about every 5 s and 80 % before.
     */
    public static final int MAIN_IDLE_WEIGHT = 6;
    /** Weight of every other idle in the idle pick. */
    public static final int IDLE_VARIANT_WEIGHT = 1;
    /** An idle pick changes after at most this many ticks plus up to {@link #IDLE_SPREAD_TICKS} (4-8 s). */
    public static final int IDLE_MIN_TICKS_TO_CHANGE = 80;
    public static final int IDLE_SPREAD_TICKS = 80;
    /** A finished cycle repicks only this long after the pick: the finished flag may belong to the previous animation. */
    public static final int IDLE_MIN_TICKS = 10;

    private final Loop loop;
    private final String[] names;

    AnimationChoice(Loop loop, String... names) {
        this.loop = loop;
        this.names = names;
    }

    public Loop loop() {
        return this.loop;
    }

    /** Number of candidate names. */
    public int size() {
        return this.names.length;
    }

    public String name(int index) {
        return this.names[index];
    }

    /** The candidate indices the species has, in candidate order. Allocates: callers cache the result. */
    public int[] availableIndices(Predicate<String> has) {
        int[] found = new int[this.names.length];
        int count = 0;
        for (int i = 0; i < this.names.length; i++) {
            if (has.test(this.names[i])) {
                found[count++] = i;
            }
        }
        int[] result = new int[count];
        System.arraycopy(found, 0, result, 0, count);
        return result;
    }

    /**
     * The weight of a candidate in {@link #pick}: for idles {@link #MAIN_IDLE_WEIGHT} (the main idle) or
     * {@link #IDLE_VARIANT_WEIGHT}; 1 for the variants of the other states.
     */
    public int weight(int index) {
        if (this != IDLE) {
            return 1;
        }
        return index == 0 ? MAIN_IDLE_WEIGHT : IDLE_VARIANT_WEIGHT;
    }

    /**
     * A weighted random pick from the available candidates.
     *
     * @param available candidate indices from {@link #availableIndices}
     * @param roll      a random number in [0, 1)
     * @return a candidate index from {@code available}, or -1 when it is empty
     */
    public int pick(int[] available, double roll) {
        int total = 0;
        for (int index : available) {
            total += weight(index);
        }
        if (total == 0) {
            return -1;
        }
        double target = roll * total;
        for (int index : available) {
            target -= weight(index);
            if (target < 0D) {
                return index;
            }
        }
        return available[available.length - 1];
    }

    /**
     * The state to play: {@code choice} when the species has one of its candidates, otherwise its fallback
     * (run: walk; eat, drink, get up: idle), or {@code null} (the controller stops; dead included).
     */
    public static AnimationChoice resolve(AnimationChoice choice, boolean anyAvailable) {
        if (anyAvailable) {
            return choice;
        }
        return switch (choice) {
            case RUN -> WALK;
            case EAT, DRINK, GET_UP -> IDLE;
            default -> null;
        };
    }

    /**
     * The length in ticks (rounded up) of the first candidate the species has.
     *
     * @param secondsByName the species' animations: name to {@code animation_length} in seconds, NaN when a name
     *                      has no length
     * @return the ticks, {@link #LENGTH_NOT_DEFINED} when the species has no candidate, or {@link #LENGTH_UNKNOWN}
     *         when the first one it has has no length
     */
    public int lengthTicks(Map<String, Double> secondsByName) {
        for (String name : this.names) {
            Double seconds = secondsByName.get(name);
            if (seconds != null) {
                return ticks(seconds);
            }
        }
        return LENGTH_NOT_DEFINED;
    }

    /**
     * The length in ticks (rounded up) of one candidate (P12a: the idle pick's own cycle).
     *
     * @param secondsByName as in {@link #lengthTicks(Map)}
     * @return the ticks, {@link #LENGTH_NOT_DEFINED} when the species does not have it, or {@link #LENGTH_UNKNOWN}
     *         when it has no length
     */
    public int lengthTicks(Map<String, Double> secondsByName, int candidate) {
        Double seconds = secondsByName.get(this.names[candidate]);
        return seconds == null ? LENGTH_NOT_DEFINED : ticks(seconds);
    }

    private static int ticks(double seconds) {
        return seconds > 0D ? (int) Math.ceil(seconds * TICKS_PER_SECOND) : LENGTH_UNKNOWN;
    }

    /**
     * How long the flinch state lasts, in ticks. A positive species value is kept (it was set on purpose);
     * otherwise the flinch animation's length, the fallback when the file cannot be read, or 0 when the species
     * has no flinch animation (as before).
     *
     * @param configured the species' {@code flinchAnimLength}
     * @param known      {@link #lengthTicks} for {@link #FLINCH}, or {@link #LENGTH_UNKNOWN}
     */
    public static int flinchTicks(int configured, int known) {
        if (configured > 0) {
            return configured;
        }
        if (known == LENGTH_UNKNOWN) {
            return FLINCH_FALLBACK_TICKS;
        }
        return Math.max(known, 0);
    }

    /** The idle timer: how long an idle pick lasts before a new pick, in ticks (4-8 s); see {@link #idleChangeAt}. */
    public static long idleTicks(double roll) {
        return IDLE_MIN_TICKS_TO_CHANGE + (long) (roll * (IDLE_SPREAD_TICKS + 1));
    }

    /**
     * When the timer may change an idle pick (P12a): after the timer, but never before one full cycle of the pick
     * (with the blend into it) has played, so a 7.88 s {@code sniffair} is not cut.
     *
     * @param timerTicks      {@link #idleTicks}
     * @param cycleTicks      the pick's length ({@link #lengthTicks(Map, int)}); 0 or less when not known
     * @param transitionTicks the controller's blend into a new animation, played before the cycle starts
     */
    public static long idleChangeAt(long now, long timerTicks, int cycleTicks, int transitionTicks) {
        long cycle = cycleTicks > 0 ? cycleTicks + transitionTicks : 0L;
        return now + Math.max(timerTicks, cycle);
    }

    /**
     * Whether the idle timer starts from now: for a new pick, and (P12a fix round 1) for a kept pick that plays
     * again from its first frame because the controller played another state, or nothing, since it last played (a
     * walk, an eat or a one-shot interrupted it). The pick time and the change time then count from now
     * ({@link #idleChangeAt}, with the pick's timer), so the timer cannot cut a restarted pick before one full cycle.
     *
     * @param picked      a new pick was made on this frame
     * @param pickPlaying the controller last played the idle pick, so it plays on
     */
    public static boolean idleTimerStarts(boolean picked, boolean pickPlaying) {
        return picked || !pickPlaying;
    }

    /**
     * Whether an idle plays only the main idle (the first idle candidate the species has, {@code misc.idle}) with
     * the always-on sway, and no variants (P13b): while the animal's herd travels, the journey's pauses included
     * (ruling 3b), and for a get-up that falls back to the idle because the species has no get-up animation
     * (ruling 3c: the Gallimimus). A look left or right then does not start each time the herd stops or an animal
     * gets up.
     *
     * @param state       the state the idle plays for: {@link #IDLE} for a standing idle, or the state that fell
     *                    back to the idle ({@link #resolve})
     * @param herdTravels the server's synced flag: the animal's herd is in TRAVEL
     */
    public static boolean mainIdleOnly(AnimationChoice state, boolean herdTravels) {
        return herdTravels || state == GET_UP;
    }

    /**
     * Whether the idle animation changes now: its cycle finished (not right after the pick), or its time is up.
     *
     * @param finished the controller reports the current idle animation finished
     */
    public static boolean shouldRepickIdle(boolean finished, long now, long pickedAt, long changeAt) {
        return now >= changeAt || finished && now - pickedAt >= IDLE_MIN_TICKS;
    }
}

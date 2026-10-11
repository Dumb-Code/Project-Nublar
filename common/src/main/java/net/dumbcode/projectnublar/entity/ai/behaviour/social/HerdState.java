package net.dumbcode.projectnublar.entity.ai.behaviour.social;

/** What a herd is doing as a whole. Regrouping is per follower, not a herd state. */
public enum HerdState {
    /** The leader walks to a destination; followers hold wedge slots along its trail. */
    TRAVEL,
    /** The leader stands; followers graze around the weighted anchor. */
    GRAZE
}

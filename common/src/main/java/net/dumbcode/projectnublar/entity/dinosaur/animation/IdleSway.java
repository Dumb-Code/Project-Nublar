package net.dumbcode.projectnublar.entity.dinosaur.animation;

/**
 * The always-on breathing sway (fix P12a, lead choice). Pure: bone names and angles only.
 * <p>
 * The model adds these angles to the keyframed pose on every frame ({@code DinosaurGeoModel}), so the sway shows
 * under every animation: standing, idle variants, resting and walking. It is the Gallimimus {@code misc.idle}
 * (gallimimus.animation.json): {@code A * math.cos(query.life_time * rate + phase)} per bone, with Molang's
 * cosine in degrees and the time in seconds. Pitch turns at {@link #PITCH_RATE} (one cycle in 3.6 s), yaw at
 * {@link #YAW_RATE} (7.2 s). The file's {@code bicepLeft} and {@code bicepRight} are left out: the Gallimimus model
 * has no such bones.
 */
public final class IdleSway {
    /** Pitch sway, degrees of phase per second. */
    public static final double PITCH_RATE = 100D;
    /** Yaw sway, degrees of phase per second. */
    public static final double YAW_RATE = 50D;

    /** The swaying bones; the model skips the ones its species does not have. */
    private static final String[] BONES = {"hips", "neck1", "head", "tail1", "tail2", "tail3"};
    /** Per bone, in {@link #BONES} order: pitch amplitude (degrees) and phase (degrees). */
    private static final double[] PITCH_AMPLITUDE = {0.5D, 1D, -3D, 2D, 2D, 2D};
    private static final double[] PITCH_PHASE = {0D, 90D, 100D, -60D, -90D, -120D};
    /** Per bone, in {@link #BONES} order: yaw amplitude (degrees) and phase (degrees). Only the tail sways sideways. */
    private static final double[] YAW_AMPLITUDE = {0D, 0D, 0D, 2D, 2D, 2D};
    private static final double[] YAW_PHASE = {0D, 0D, 0D, -210D, -250D, -300D};

    public static final int BONE_COUNT = BONES.length;

    private IdleSway() {
    }

    public static String bone(int index) {
        return BONES[index];
    }

    /** Pitch (x rotation) of a bone, in degrees, at {@code seconds} of animation time. */
    public static double pitchDegrees(int index, double seconds) {
        return PITCH_AMPLITUDE[index] * Math.cos(Math.toRadians(seconds * PITCH_RATE + PITCH_PHASE[index]));
    }

    /** Yaw (y rotation) of a bone, in degrees, at {@code seconds} of animation time. */
    public static double yawDegrees(int index, double seconds) {
        return YAW_AMPLITUDE[index] * Math.cos(Math.toRadians(seconds * YAW_RATE + YAW_PHASE[index]));
    }
}

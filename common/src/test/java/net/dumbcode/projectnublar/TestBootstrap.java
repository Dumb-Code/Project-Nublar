package net.dumbcode.projectnublar;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

/**
 * Starts vanilla's registries once per test JVM. Tests that touch {@code EntityType} constants,
 * registry keys or codecs that read built-in registries call {@link #ensure()} in
 * {@code @BeforeAll}. This is the only bootstrap helper; other tests reuse it.
 */
public final class TestBootstrap {
    private static boolean done;

    private TestBootstrap() {
    }

    /** Detects the game version and bootstraps the built-in registries; later calls do nothing. */
    public static synchronized void ensure() {
        if (done) {
            return;
        }
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        done = true;
    }
}

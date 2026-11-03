/**
 * GameTest bodies with no loader code, run as vanilla 26.2 test instances on both loaders.
 * {@code GameTestInit} registers each body as a test function, and the NeoForge data run
 * ({@code ModGameTestProvider}) writes the matching test instance (template, environment, time
 * limit) into {@code common/src/generated/resources/data/projectnublar/test_instance/}.
 * <p>
 * To add a test: a {@code public static void} method here, one {@code TEST_FUNCTIONS.register(...)}
 * line in {@code GameTestInit}, one entry in {@code ModGameTestProvider}, then {@code runData}.
 */
package net.dumbcode.projectnublar.gametest;

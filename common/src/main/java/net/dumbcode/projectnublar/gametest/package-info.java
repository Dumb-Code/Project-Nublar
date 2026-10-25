/**
 * GameTest bodies with no loader code; each loader's holder class ({@code ForgeGameTests}, {@code FabricGameTests})
 * registers them.
 * <p>
 * To add a test, write a {@code public static void} method that takes a {@code GameTestHelper} in a class of this
 * package. Then add one {@code @GameTest} method that calls it to each holder:
 * {@code forge/src/main/java/net/dumbcode/projectnublar/gametest/ForgeGameTests.java} and
 * {@code fabric/src/main/java/net/dumbcode/projectnublar/gametest/FabricGameTests.java}. Give both entries the same
 * {@code batch} and {@code timeoutTicks}. On Fabric, write the template as {@code Constants.MODID + ":" + <template>};
 * Forge adds the namespace itself.
 */
package net.dumbcode.projectnublar.gametest;

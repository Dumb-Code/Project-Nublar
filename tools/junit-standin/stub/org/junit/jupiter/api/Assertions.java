package org.junit.jupiter.api;

import java.util.Objects;

/** Stand-in for the JUnit 5 assertions the unit tests use. Failures throw AssertionError. */
public final class Assertions {

    private Assertions() {
    }

    private static void fail(String message) {
        throw new AssertionError(message);
    }

    public static void assertTrue(boolean condition) { assertTrue(condition, "expected true"); }
    public static void assertTrue(boolean condition, String message) { if (!condition) fail(message); }
    public static void assertFalse(boolean condition) { assertFalse(condition, "expected false"); }
    public static void assertFalse(boolean condition, String message) { if (condition) fail(message); }
    public static void assertNull(Object value) { assertNull(value, "expected null"); }
    public static void assertNull(Object value, String message) { if (value != null) fail(message + " (was " + value + ")"); }
    public static void assertNotNull(Object value) { assertNotNull(value, "expected a value"); }
    public static void assertNotNull(Object value, String message) { if (value == null) fail(message); }
    public static void assertSame(Object expected, Object actual) { assertSame(expected, actual, "not the same object"); }
    public static void assertSame(Object expected, Object actual, String message) { if (expected != actual) fail(message + ": expected " + expected + ", got " + actual); }
    public static void assertEquals(Object expected, Object actual) { assertEquals(expected, actual, "not equal"); }
    public static void assertEquals(Object expected, Object actual, String message) { if (!Objects.equals(expected, actual)) fail(message + ": expected " + expected + ", got " + actual); }
    public static void assertEquals(int expected, int actual) { assertEquals(expected, actual, "not equal"); }
    public static void assertEquals(int expected, int actual, String message) { if (expected != actual) fail(message + ": expected " + expected + ", got " + actual); }
    public static void assertEquals(long expected, long actual) { assertEquals(expected, actual, "not equal"); }
    public static void assertEquals(long expected, long actual, String message) { if (expected != actual) fail(message + ": expected " + expected + ", got " + actual); }
    public static void assertNotEquals(long unexpected, long actual) { assertNotEquals(unexpected, actual, "unexpectedly equal"); }
    public static void assertNotEquals(long unexpected, long actual, String message) { if (unexpected == actual) fail(message + ": both " + actual); }
    public static void assertEquals(float expected, float actual, float delta) { assertEquals(expected, actual, delta, "not equal"); }
    public static void assertEquals(float expected, float actual, float delta, String message) { if (Math.abs(expected - actual) > delta) fail(message + ": expected " + expected + ", got " + actual); }
    public static void assertEquals(double expected, double actual, double delta) { assertEquals(expected, actual, delta, "not equal"); }
    public static void assertEquals(double expected, double actual, double delta, String message) { if (Math.abs(expected - actual) > delta) fail(message + ": expected " + expected + ", got " + actual); }
}

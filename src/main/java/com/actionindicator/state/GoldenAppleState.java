package com.actionindicator.state;

/**
 * Tracks golden apple / enchanted golden apple usage timers.
 */
public final class GoldenAppleState {

    /** Ticks remaining on regular golden apple absorption effect (5 seconds = 100 ticks) */
    public static int goldenAppleTicksRemaining = 0;

    /** Ticks remaining on enchanted golden apple (30 seconds = 600 ticks) */
    public static int enchantedAppleTicksRemaining = 0;

    /** True if the most recent apple was enchanted */
    public static boolean lastWasEnchanted = false;

    public static void tick() {
        if (goldenAppleTicksRemaining > 0)   goldenAppleTicksRemaining--;
        if (enchantedAppleTicksRemaining > 0) enchantedAppleTicksRemaining--;
    }

    private GoldenAppleState() {}
}

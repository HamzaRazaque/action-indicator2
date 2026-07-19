package com.actionindicator.state;

public final class ActionState {
    public static int attackFlashTicksRemaining = 0;
    public static boolean attackFullyCharged = false;
    public static boolean targetHasShieldUp = false;
    public static int shieldBrokenTicksRemaining = 0;
    public static int brokenShieldEntityId = -1;
    public static int blockHitPulseTicksRemaining = 0;

    public static void tick() {
        if (attackFlashTicksRemaining > 0)  attackFlashTicksRemaining--;
        if (shieldBrokenTicksRemaining > 0) shieldBrokenTicksRemaining--;
        if (blockHitPulseTicksRemaining > 0) blockHitPulseTicksRemaining--;
    }

    private ActionState() {}
}

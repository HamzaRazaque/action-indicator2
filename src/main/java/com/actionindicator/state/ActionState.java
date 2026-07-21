package com.actionindicator.state;

/**
 * ActionState — singleton-style static store.
 *
 * All three features write their state here each tick/frame.
 * The HUD renderer reads from here — zero coupling between mixins and renderer.
 */
public final class ActionState {

    // ── Feature 1: Attack Cooldown Flash ─────────────────────────────────────
    /**
     * How many ticks remain to display the "fully charged" crosshair flash.
     * Written by ClientPlayerEntityMixin, read by ActionIndicatorHud.
     */
    public static int attackFlashTicksRemaining = 0;

    /** True while the player's attack cooldown is at 1.0 (fully charged). */
    public static boolean attackFullyCharged = false;

    // ── Feature 2: Shield Status ──────────────────────────────────────────────
    /** The targeted player is currently blocking with a raised shield. */
    public static boolean targetHasShieldUp = false;

    /**
     * How many ticks the "shield broken" indicator should stay visible.
     * Set when we receive an EntityStatusS2CPacket with status 30 (shield disabled).
     * Written by ClientPlayNetworkHandlerMixin.
     */
    public static int shieldBrokenTicksRemaining = 0;

    /**
     * Entity ID of the player whose shield was just broken.
     * We compare this to the current crosshair target so the indicator
     * is only shown when relevant.
     */
    public static int brokenShieldEntityId = -1;

    // ── Feature 3: Block-Hit Pulse ────────────────────────────────────────────
    /**
     * How many ticks remain for the block-hit edge pulse.
     * Written by ClientPlayerEntityMixin when a block-hit is detected.
     */
    public static int blockHitPulseTicksRemaining = 0;

    // ── Tick helpers ──────────────────────────────────────────────────────────
    /**
     * Decrement all countdown timers each client tick.
     * Called once per tick from ActionIndicatorClient.
     */
    public static void tick() {
        if (attackFlashTicksRemaining > 0)  attackFlashTicksRemaining--;
        if (shieldBrokenTicksRemaining > 0) shieldBrokenTicksRemaining--;
        if (blockHitPulseTicksRemaining > 0) blockHitPulseTicksRemaining--;
    }

    private ActionState() {}
}

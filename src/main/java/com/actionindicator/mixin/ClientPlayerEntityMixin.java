package com.actionindicator.mixin;

import com.actionindicator.hud.ActionIndicatorHud;
import com.actionindicator.state.ActionState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.UseAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ClientPlayerEntityMixin
 *
 * Taps into the local player's tick to read two purely-passive states:
 *
 *  A) Attack cooldown — triggers the crosshair flash when fully charged.
 *  B) Block-hit detection — triggers the edge pulse when the player
 *     raises a shield/sword within 2 ticks of swinging.
 *
 * No packets are sent. No actions are performed. Read-only observation.
 */
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

    // ── Shadow fields we need to read ────────────────────────────────────────

    @Shadow public abstract float getAttackCooldownProgress(float baseTime);

    // ── Internal tracking ────────────────────────────────────────────────────

    /** Was the cooldown already at 1.0 last tick? Prevents repeated triggers. */
    private boolean ai$wasFullyChargedLastTick = false;

    /**
     * Tick counter since the last swing (attack input).
     * Reset to 0 on swing; we check within 2 ticks for a raised shield.
     * -1 means no recent swing.
     */
    private int ai$ticksSinceSwing = -1;

    /** Was the player blocking last tick? Used to detect the transition. */
    private boolean ai$wasBlockingLastTick = false;

    // ── Tick injection ───────────────────────────────────────────────────────

    /**
     * Injected at the tail of ClientPlayerEntity#tick().
     * Reads cooldown and blocking state — no writes to game state.
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void ai$onTick(CallbackInfo ci) {
        ClientPlayerEntity self = (ClientPlayerEntity)(Object)this;

        tickAttackCooldown(self);
        tickBlockHit(self);
    }

    // ── Feature 1: Attack Cooldown Flash ─────────────────────────────────────

    private void tickAttackCooldown(ClientPlayerEntity player) {
        float progress = getAttackCooldownProgress(0.0F);
        boolean isFullNow = progress >= 1.0F;

        if (isFullNow && !ai$wasFullyChargedLastTick) {
            // Edge: cooldown just became fully charged → start the 3-tick flash
            ActionState.attackFlashTicksRemaining = 3;
            ActionState.attackFullyCharged = true;
        } else if (!isFullNow) {
            ActionState.attackFullyCharged = false;
        }

        ai$wasFullyChargedLastTick = isFullNow;
    }

    // ── Feature 3: Block-Hit Detection ───────────────────────────────────────

    /**
     * A "block-hit" is the technique of swinging then immediately raising a
     * shield/sword to absorb incoming damage. We detect it client-side by
     * checking:
     *   • The player started actively using a shield within 2 ticks of their
     *     last swing (ai$ticksSinceSwing resets on attack input via the
     *     separate swing injection below).
     *
     * This is a heuristic — we are observing inputs the player already made,
     * not assisting with any automation.
     */
    private void tickBlockHit(ClientPlayerEntity player) {
        boolean isBlockingNow = player.isUsingItem()
                && player.getActiveItem().getItem() == Items.SHIELD;

        // Count up ticks since last swing (cap at 3 to avoid overflow)
        if (ai$ticksSinceSwing >= 0) {
            ai$ticksSinceSwing++;
            if (ai$ticksSinceSwing > 3) ai$ticksSinceSwing = -1; // window expired
        }

        // Rising edge: player just started blocking
        boolean justStartedBlocking = isBlockingNow && !ai$wasBlockingLastTick;

        if (justStartedBlocking && ai$ticksSinceSwing >= 0 && ai$ticksSinceSwing <= 2) {
            // Block-hit confirmed — trigger the edge pulse
            ActionState.blockHitPulseTicksRemaining = 5;
        }

        ai$wasBlockingLastTick = isBlockingNow;
    }

    /**
     * Injected at the call site where the player performs a swing arm animation.
     * We use this as our "swing happened" signal to start the block-hit window.
     *
     * swingHand() is called by the attack input handler for every left-click swing.
     */
    @Inject(
        method = "swingHand(Lnet/minecraft/util/Hand;)V",
        at = @At("HEAD")
    )
    private void ai$onSwingHand(net.minecraft.util.Hand hand, CallbackInfo ci) {
        // Record that a swing just happened; block-hit window is now open
        ai$ticksSinceSwing = 0;
    }
}

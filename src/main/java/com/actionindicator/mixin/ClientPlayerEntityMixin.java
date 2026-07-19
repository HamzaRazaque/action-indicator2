package com.actionindicator.mixin;

import com.actionindicator.state.ActionState;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Tracks attack cooldown and block-hit timing on the local player.
 * Mojang-mapped names for 1.21.1:
 *   ClientPlayerEntity → LocalPlayer  (net.minecraft.client.player.LocalPlayer)
 *   Hand               → InteractionHand
 */
@Mixin(LocalPlayer.class)
public abstract class ClientPlayerEntityMixin {

    @Shadow public abstract float getAttackStrengthScale(float adjustTicks);

    private boolean ai$wasFullyChargedLastTick = false;
    private int ai$ticksSinceSwing = -1;
    private boolean ai$wasBlockingLastTick = false;

    @Inject(method = "tick", at = @At("TAIL"))
    private void ai$onTick(CallbackInfo ci) {
        LocalPlayer self = (LocalPlayer)(Object)this;
        tickAttackCooldown();
        tickBlockHit(self);
    }

    private void tickAttackCooldown() {
        // getAttackStrengthScale(0F) returns 0.0–1.0 where 1.0 = fully charged
        float progress = getAttackStrengthScale(0.0F);
        boolean isFullNow = progress >= 1.0F;

        if (isFullNow && !ai$wasFullyChargedLastTick) {
            ActionState.attackFlashTicksRemaining = 3;
            ActionState.attackFullyCharged = true;
        } else if (!isFullNow) {
            ActionState.attackFullyCharged = false;
        }

        ai$wasFullyChargedLastTick = isFullNow;
    }

    private void tickBlockHit(LocalPlayer player) {
        boolean isBlockingNow = player.isUsingItem()
                && player.getUseItem().is(Items.SHIELD);

        if (ai$ticksSinceSwing >= 0) {
            ai$ticksSinceSwing++;
            if (ai$ticksSinceSwing > 3) ai$ticksSinceSwing = -1;
        }

        boolean justStartedBlocking = isBlockingNow && !ai$wasBlockingLastTick;
        if (justStartedBlocking && ai$ticksSinceSwing >= 0 && ai$ticksSinceSwing <= 2) {
            ActionState.blockHitPulseTicksRemaining = 5;
        }

        ai$wasBlockingLastTick = isBlockingNow;
    }

    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;)V", at = @At("HEAD"))
    private void ai$onSwing(InteractionHand hand, CallbackInfo ci) {
        ai$ticksSinceSwing = 0;
    }
}

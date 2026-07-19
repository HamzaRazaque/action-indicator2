package com.actionindicator.mixin;

import com.actionindicator.state.ActionState;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class ClientPlayerEntityMixin {

    private boolean ai$wasFullyChargedLastTick = false;
    private int ai$ticksSinceSwing = -1;
    private boolean ai$wasBlockingLastTick = false;

    @Inject(method = "tick", at = @At("TAIL"))
    private void ai$onTick(CallbackInfo ci) {
        LocalPlayer self = (LocalPlayer)(Object)this;
        tickAttackCooldown(self);
        tickBlockHit(self);
    }

    private void tickAttackCooldown(LocalPlayer player) {
        // getAttackStrengthScale is defined on Player (the parent class)
        float progress = player.getAttackStrengthScale(0.0F);
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

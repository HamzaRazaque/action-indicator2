package com.actionindicator.mixin;

import com.actionindicator.hud.ActionIndicatorHud;
import com.actionindicator.state.ActionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ClientPlayNetworkHandlerMixin
 *
 * Listens to EntityStatusS2CPacket (a packet the server sends to ALL nearby
 * clients, not just the target). This is vanilla, unmodified packet data —
 * we are only reading it, never spoofing or suppressing it.
 *
 * Status code 30 = shield/item-use disabled (axe parry or cooldown trigger).
 * When we see this for the player we are currently targeting, we set the
 * "broken shield" indicator state.
 */
@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    /**
     * Vanilla status byte 30 is sent when a player's shield use is disabled
     * (e.g. hit by an axe). The entity ID in the packet tells us who it happened to.
     *
     * We record the entity ID so the HUD can match it against the crosshair target.
     */
    @Inject(
        method = "onEntityStatus",
        at = @At("HEAD")
    )
    private void ai$onEntityStatus(EntityStatusS2CPacket packet, CallbackInfo ci) {
        // Status 30 == shield disabled (see EntityStatuses.BREAK_SHIELD = 30)
        if (packet.getStatus() == 30) {
            net.minecraft.client.MinecraftClient client =
                    net.minecraft.client.MinecraftClient.getInstance();
            if (client.world == null) return;

            net.minecraft.entity.Entity entity = packet.getEntity(client.world);
            if (entity == null) return;

            // Store broken-shield state for this entity
            ActionState.brokenShieldEntityId       = entity.getId();
            ActionState.shieldBrokenTicksRemaining = ActionIndicatorHud.SHIELD_BROKEN_TICKS;

            // The target is no longer actively blocking (shield was forcibly lowered)
            // tickShieldTargetScan() will update targetHasShieldUp next tick automatically.
        }
    }
}

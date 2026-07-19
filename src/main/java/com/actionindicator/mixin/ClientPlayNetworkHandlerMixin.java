package com.actionindicator.mixin;

import com.actionindicator.hud.ActionIndicatorHud;
import com.actionindicator.state.ActionState;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Listens for shield-disable events from the server.
 * Mojang-mapped names for 1.21.1:
 *   ClientPlayNetworkHandler  → ClientPacketListener
 *   EntityStatusS2CPacket     → ClientboundEntityEventPacket
 *
 * Event byte 30 = shield/item-use disabled (hit by axe).
 */
@Mixin(ClientPacketListener.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "handleEntityEvent", at = @At("HEAD"))
    private void ai$onEntityEvent(ClientboundEntityEventPacket packet, CallbackInfo ci) {
        // Event ID 30 == shield disabled
        if (packet.getEventId() == 30) {
            net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getInstance();
            if (client.level == null) return;

            net.minecraft.world.entity.Entity entity = packet.getEntity(client.level);
            if (entity == null) return;

            ActionState.brokenShieldEntityId       = entity.getId();
            ActionState.shieldBrokenTicksRemaining = ActionIndicatorHud.SHIELD_BROKEN_TICKS;
        }
    }
}

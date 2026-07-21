package com.actionindicator;

import com.actionindicator.hud.ActionIndicatorHud;
import com.actionindicator.state.ActionState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ActionIndicatorClient — Fabric client entrypoint.
 *
 * Registers:
 *  • END_CLIENT_TICK  → decrements state timers and scans the crosshair target
 *  • HudRenderCallback → draws all visual indicators via ActionIndicatorHud
 */
public class ActionIndicatorClient implements ClientModInitializer {

    public static final String MOD_ID = "action-indicator";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("[ActionIndicator] Initialising…");

        // Tick all state timers and update the shield-target scan once per tick.
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ActionState.tick();
            ActionIndicatorHud.tickShieldTargetScan(client);
        });

        // Draw all HUD elements after the vanilla HUD renders.
        HudRenderCallback.EVENT.register((drawContext, tickDelta) ->
                ActionIndicatorHud.render(drawContext, tickDelta));

        LOGGER.info("[ActionIndicator] Ready.");
    }
}

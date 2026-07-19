package com.actionindicator;

import com.actionindicator.hud.ActionIndicatorHud;
import com.actionindicator.state.ActionState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class ActionIndicatorClient implements ClientModInitializer {

    public static final String MOD_ID = "action-indicator";

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ActionState.tick();
            ActionIndicatorHud.tickShieldTargetScan(client);
        });

        HudRenderCallback.EVENT.register((drawContext, tickDelta) ->
                ActionIndicatorHud.render(drawContext, tickDelta));
    }
}

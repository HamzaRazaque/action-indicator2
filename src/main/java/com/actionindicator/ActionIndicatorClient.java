package com.actionindicator;

import com.actionindicator.hud.HealthHud;
import com.actionindicator.hud.WaypointHud;
import com.actionindicator.keybind.KeybindManager;
import com.actionindicator.keybind.KeybindScreen;
import com.actionindicator.state.GoldenAppleState;
import com.actionindicator.waypoint.WaypointScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Items;

public class ActionIndicatorClient implements ClientModInitializer {

    public static boolean showHealth = true;

    @Override
    public void onInitializeClient() {

        // Register keybindings
        KeybindManager.register();

        // Tick events: golden apple timer + keybind polling
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            GoldenAppleState.tick();
            trackGoldenApple(client);
            handleKeybinds(client);
        });

        // HUD rendering
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            float delta = tickCounter.getGameTimeDeltaPartialTick(true);
            if (showHealth) HealthHud.render(drawContext, delta);
            WaypointHud.render(drawContext, delta);
        });
    }

    /** Detect when the player finishes eating a golden apple */
    private void trackGoldenApple(Minecraft client) {
        if (client.player == null) return;

        // Check if player has absorption effect (from golden apple)
        var effects = client.player.getActiveEffects();
        boolean hasAbsorption = effects.stream()
            .anyMatch(e -> e.getEffect().value() == net.minecraft.world.effect.MobEffects.ABSORPTION.value());

        // Check currently using item
        var useItem = client.player.getUseItem();
        if (useItem.is(Items.GOLDEN_APPLE) && client.player.isUsingItem()) {
            GoldenAppleState.goldenAppleTicksRemaining = 100; // 5 seconds
            GoldenAppleState.lastWasEnchanted = false;
        } else if (useItem.is(Items.ENCHANTED_GOLDEN_APPLE) && client.player.isUsingItem()) {
            GoldenAppleState.enchantedAppleTicksRemaining = 600; // 30 seconds
            GoldenAppleState.lastWasEnchanted = true;
        }
    }

    /** Poll keybinds each tick and open the right screen */
    private void handleKeybinds(Minecraft client) {
        if (client.screen != null) return; // don't open if a screen is already up

        if (KeybindManager.openMenu.consumeClick()) {
            client.setScreen(new KeybindScreen());
        }

        if (KeybindManager.openWaypoints.consumeClick()) {
            client.setScreen(new WaypointScreen());
        }

        if (KeybindManager.toggleHealth.consumeClick()) {
            showHealth = !showHealth;
        }
    }
}

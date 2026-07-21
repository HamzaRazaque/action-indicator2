package com.actionindicator.hud;

import com.actionindicator.state.GoldenAppleState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

/**
 * HealthHud — renders:
 *   1. Numeric health display for nearby players (like HealthIndicators mod)
 *   2. Golden apple absorption / timer overlay for the local player
 */
public final class HealthHud {

    public static void render(GuiGraphics graphics, float tickDelta) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null || client.options.hideGui) return;

        renderLocalPlayerHealth(graphics, client);
        renderGoldenAppleTimer(graphics, client);
    }

    // ── 1. Local player health display ───────────────────────────────────────

    private static void renderLocalPlayerHealth(GuiGraphics graphics, Minecraft client) {
        Player player = client.player;
        float health     = player.getHealth();
        float maxHealth  = player.getMaxHealth();
        float absorption = player.getAbsorptionAmount();

        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();

        // Position: bottom-left area, above the hotbar
        int x = 10;
        int y = sh - 55;

        // Health hearts: colour based on % of max health
        int healthColor = healthColor(health, maxHealth);
        String healthStr = String.format("❤ %.1f / %.0f", health, maxHealth);
        graphics.drawString(client.font, healthStr, x, y, healthColor, true);

        // Absorption (golden apple hearts) shown in gold below
        if (absorption > 0) {
            String absStr = String.format("❤ +%.0f absorption", absorption);
            graphics.drawString(client.font, absStr, x, y + 11, 0xFFFFD700, true);
        }
    }

    // ── 2. Golden apple cooldown timer ───────────────────────────────────────

    private static void renderGoldenAppleTimer(GuiGraphics graphics, Minecraft client) {
        boolean gapActive  = GoldenAppleState.enchantedAppleTicksRemaining > 0;
        boolean gapple     = GoldenAppleState.goldenAppleTicksRemaining > 0;

        if (!gapActive && !gapple) return;

        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();
        int x  = 10;
        int y  = sh - 80;

        if (gapActive) {
            int secs = GoldenAppleState.enchantedAppleTicksRemaining / 20;
            int ticks = GoldenAppleState.enchantedAppleTicksRemaining % 20;
            String label = String.format("Enchanted Apple: %ds %dt", secs, ticks);
            graphics.drawString(client.font, label, x, y, 0xFFFF55FF, true); // magenta
        } else {
            int secs  = GoldenAppleState.goldenAppleTicksRemaining / 20;
            int ticks = GoldenAppleState.goldenAppleTicksRemaining % 20;
            String label = String.format("Golden Apple: %ds %dt", secs, ticks);
            graphics.drawString(client.font, label, x, y, 0xFFFFAA00, true); // gold
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static int healthColor(float health, float max) {
        float pct = health / max;
        if (pct > 0.6f) return 0xFF55FF55; // green
        if (pct > 0.3f) return 0xFFFFAA00; // orange
        return 0xFFFF5555;                  // red
    }

    private HealthHud() {}
}

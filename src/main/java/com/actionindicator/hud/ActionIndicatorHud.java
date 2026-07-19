package com.actionindicator.hud;

import com.actionindicator.state.ActionState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * ActionIndicatorHud — draws all three visual features.
 * Uses Mojang (Yarn-style) mapped names for 1.21.1:
 *   MinecraftClient  → Minecraft
 *   DrawContext      → GuiGraphics
 *   PlayerEntity     → Player
 *   ClientPlayerEntity → net.minecraft.client.player.LocalPlayer
 */
public final class ActionIndicatorHud {

    public static final int SHIELD_BROKEN_TICKS = 30;
    private static final int BLOCK_HIT_PULSE_TICKS = 5;

    private static float chromaHue = 0f;

    // ── Called once per tick from the client tick event ──────────────────────

    public static void tickShieldTargetScan(Minecraft client) {
        if (client.player == null || client.level == null) return;

        ActionState.targetHasShieldUp = false;

        HitResult hit = client.hitResult;
        if (hit == null || hit.getType() != HitResult.Type.ENTITY) return;

        Entity entity = ((EntityHitResult) hit).getEntity();
        if (!(entity instanceof Player target)) return;

        // Shield is raised when the player is actively using a shield item
        if (target.isUsingItem() && target.getUseItem().is(Items.SHIELD)) {
            ActionState.targetHasShieldUp = true;
        }
    }

    // ── Called every frame from HudRenderCallback ─────────────────────────────

    public static void render(GuiGraphics graphics, float tickDelta) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui) return;

        chromaHue = (chromaHue + 0.003f) % 1.0f;

        int sw = client.getWindow().getGuiScaledWidth();
        int sh = client.getWindow().getGuiScaledHeight();

        renderAttackFlash(graphics, sw, sh);
        renderShieldBadge(graphics, client, sw, sh);
        renderBlockHitPulse(graphics, sw, sh);
    }

    // ── Feature 1: Attack Timing Crosshair Flash ──────────────────────────────

    private static void renderAttackFlash(GuiGraphics graphics, int sw, int sh) {
        if (ActionState.attackFlashTicksRemaining <= 0) return;

        int cx = sw / 2;
        int cy = sh / 2;
        int radius = 8;
        int thick = 2;
        int color = chromaColor(chromaHue, 0xFF);

        // Draw a square ring around the crosshair
        graphics.fill(cx - radius, cy - radius - thick, cx + radius, cy - radius, color);         // top
        graphics.fill(cx - radius, cy + radius, cx + radius, cy + radius + thick, color);         // bottom
        graphics.fill(cx - radius - thick, cy - radius, cx - radius, cy + radius, color);         // left
        graphics.fill(cx + radius, cy - radius, cx + radius + thick, cy + radius, color);         // right
    }

    // ── Feature 2: Shield Status Badge ───────────────────────────────────────

    private static void renderShieldBadge(GuiGraphics graphics, Minecraft client, int sw, int sh) {
        boolean broken  = ActionState.shieldBrokenTicksRemaining > 0;
        boolean shieldUp = ActionState.targetHasShieldUp;

        if (!shieldUp && !broken) return;

        int cx = sw / 2;
        int cy = sh / 2 - 36;

        String label;
        int color;

        if (broken) {
            boolean flicker = (System.currentTimeMillis() / 125) % 2 == 0;
            label = "SHIELD BROKEN";
            color = flicker ? 0xFFFF2222 : 0xFFFF8888;
        } else {
            label = "SHIELD UP";
            color = 0xFF44AAFF;
        }

        int textW = client.font.width(label);
        int tx = cx - textW / 2;

        graphics.fill(tx - 4, cy - 2, tx + textW + 4, cy + 10, 0x88000000);
        graphics.drawString(client.font, label, tx, cy, color, false);
    }

    // ── Feature 3: Block-Hit Edge Pulse ──────────────────────────────────────

    private static void renderBlockHitPulse(GuiGraphics graphics, int sw, int sh) {
        if (ActionState.blockHitPulseTicksRemaining <= 0) return;

        float progress = (float) ActionState.blockHitPulseTicksRemaining / BLOCK_HIT_PULSE_TICKS;
        int alpha = (int) (170 * progress);
        int color = (alpha << 24) | 0x00FFFFFF;

        int pulseH = 18;
        graphics.fill(0, sh - pulseH, sw, sh, color);
        graphics.fill(0, 0, sw, pulseH, color);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static int chromaColor(float hue, int alpha) {
        int rgb = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);
        return (alpha << 24) | (rgb & 0x00FFFFFF);
    }

    private ActionIndicatorHud() {}
}

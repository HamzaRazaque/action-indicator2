package com.actionindicator.hud;

import com.actionindicator.state.ActionState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

/**
 * ActionIndicatorHud
 *
 * Draws three purely-visual overlays:
 *   1. Attack Timing Flash  — coloured ring around the crosshair when fully charged
 *   2. Shield Status Badge  — icon above the targeted player's nameplate area
 *   3. Block-Hit Pulse      — subtle edge pulse confirming block-hit timing
 *
 * Nothing here sends packets or fakes inputs. Strictly read-and-draw.
 */
public final class ActionIndicatorHud {

    // ── Flash durations (ticks) ───────────────────────────────────────────────
    /** How many ticks the "fully charged" crosshair flash lasts. */
    private static final int ATTACK_FLASH_TICKS = 3;

    /** How many ticks the block-hit edge pulse lasts. */
    private static final int BLOCK_HIT_PULSE_TICKS = 5;

    /** How many ticks the broken-shield indicator stays visible after disable. */
    public static final int SHIELD_BROKEN_TICKS = 30; // ~1.5 s

    // ── Colours (ARGB) ───────────────────────────────────────────────────────
    private static final int COLOR_CHARGED_GREEN  = 0xFF00FF44; // bright green flash
    private static final int COLOR_SHIELD_ACTIVE  = 0xFF44AAFF; // calm blue — shield up
    private static final int COLOR_SHIELD_BROKEN  = 0xFFFF2222; // red — shield disabled
    private static final int COLOR_PULSE          = 0x88FFFFFF; // white, semi-transparent

    // ── Chroma state (for optional animated crosshair flash) ─────────────────
    private static float chromaHue = 0f;

    // =========================================================================
    //  Public API called from ActionIndicatorClient
    // =========================================================================

    /**
     * Called once per CLIENT TICK.
     * Scans the crosshair target and updates shield state in ActionState.
     */
    public static void tickShieldTargetScan(MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        ActionState.targetHasShieldUp = false; // reset each tick

        HitResult hit = client.crosshairTarget;
        if (hit == null || hit.getType() != HitResult.Type.ENTITY) return;

        Entity entity = ((EntityHitResult) hit).getEntity();
        if (!(entity instanceof PlayerEntity target)) return;

        // A shield is "raised" when the player is actively using a shield item.
        if (target.isUsingItem()
                && target.getActiveItem().getItem() == Items.SHIELD) {
            ActionState.targetHasShieldUp = true;
        }
    }

    /**
     * Called every frame from HudRenderCallback.
     *
     * @param drawContext Fabric draw context (wraps the matrix stack + render layers)
     * @param tickDelta   Partial tick for smooth interpolation
     */
    public static void render(DrawContext drawContext, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;

        // Advance chroma hue each frame (cycles 0→1 in ~5 s at 60 fps)
        chromaHue = (chromaHue + 0.003f) % 1.0f;

        int screenW = client.getWindow().getScaledWidth();
        int screenH = client.getWindow().getScaledHeight();

        renderAttackFlash(drawContext, screenW, screenH);
        renderShieldBadge(drawContext, client, screenW, screenH);
        renderBlockHitPulse(drawContext, screenW, screenH);
    }

    // =========================================================================
    //  Feature 1 — Attack Timing Crosshair Flash
    // =========================================================================

    /**
     * Draws a thin coloured ring around the vanilla crosshair while the
     * "attack fully charged" flash is active.
     *
     * The ring is 2 px thick and sits 8 px from the crosshair centre so it
     * does not obscure aim. Uses a chroma (rainbow) pulse for flair.
     */
    private static void renderAttackFlash(DrawContext ctx, int sw, int sh) {
        if (ActionState.attackFlashTicksRemaining <= 0) return;

        int cx = sw / 2;
        int cy = sh / 2;
        int radius = 8;   // distance from crosshair centre to ring edge
        int thick  = 2;   // ring thickness in pixels

        // Build a chroma colour from the current hue
        int color = chromaColor(chromaHue, 0xFF);

        // Draw ring as four L-shaped arcs using filled rectangles.
        // Top bar
        ctx.fill(cx - radius, cy - radius - thick, cx + radius, cy - radius, color);
        // Bottom bar
        ctx.fill(cx - radius, cy + radius, cx + radius, cy + radius + thick, color);
        // Left bar
        ctx.fill(cx - radius - thick, cy - radius, cx - radius, cy + radius, color);
        // Right bar
        ctx.fill(cx + radius, cy - radius, cx + radius + thick, cy + radius, color);
    }

    // =========================================================================
    //  Feature 2 — Shield Status Badge
    // =========================================================================

    /**
     * Renders a small text badge above the crosshair reticle area when the
     * targeted player is blocking or has a recently-broken shield.
     *
     * Position: centred, 36 px above screen centre (just above the crosshair).
     */
    private static void renderShieldBadge(DrawContext ctx, MinecraftClient client,
                                          int sw, int sh) {
        boolean broken = ActionState.shieldBrokenTicksRemaining > 0;
        boolean shieldUp = ActionState.targetHasShieldUp;

        if (!shieldUp && !broken) return;

        int cx = sw / 2;
        int cy = sh / 2 - 36; // above the crosshair

        String label;
        int color;

        if (broken) {
            // Flicker the broken indicator at 4 Hz for urgency
            boolean flicker = (System.currentTimeMillis() / 125) % 2 == 0;
            label = "⚔ SHIELD BROKEN";
            color = flicker ? COLOR_SHIELD_BROKEN : 0xFFFF8888;
        } else {
            label = "🛡 SHIELD UP";
            color = COLOR_SHIELD_ACTIVE;
        }

        // Centre the text
        int textW = client.textRenderer.getWidth(label);
        int tx = cx - textW / 2;

        // Dim background pill for legibility
        ctx.fill(tx - 4, cy - 2, tx + textW + 4, cy + 10, 0x88000000);

        // Label
        ctx.drawText(client.textRenderer, label, tx, cy, color, false);
    }

    // =========================================================================
    //  Feature 3 — Block-Hit Edge Pulse
    // =========================================================================

    /**
     * Renders a subtle white vignette pulse on the bottom edge of the screen
     * when a block-hit timing is confirmed.
     *
     * Fades out linearly over BLOCK_HIT_PULSE_TICKS ticks.
     */
    private static void renderBlockHitPulse(DrawContext ctx, int sw, int sh) {
        if (ActionState.blockHitPulseTicksRemaining <= 0) return;

        // Alpha fades from ~170 → 0 over the duration
        float progress = (float) ActionState.blockHitPulseTicksRemaining / BLOCK_HIT_PULSE_TICKS;
        int alpha = (int) (170 * progress);

        int color = (alpha << 24) | 0x00FFFFFF; // white with fading alpha

        int pulseH = 18; // height of the bottom-edge pulse bar

        // Bottom edge pulse
        ctx.fill(0, sh - pulseH, sw, sh, color);

        // Mirrored top edge (symmetric feels cleaner)
        ctx.fill(0, 0, sw, pulseH, color);
    }

    // =========================================================================
    //  Helpers
    // =========================================================================

    /**
     * Convert HSB hue (0–1) to an ARGB int.
     * Saturation and brightness are fixed for a vivid chroma effect.
     */
    private static int chromaColor(float hue, int alpha) {
        int rgb = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);
        return (alpha << 24) | (rgb & 0x00FFFFFF);
    }

    private ActionIndicatorHud() {}
}

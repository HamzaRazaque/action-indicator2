package com.actionindicator.hud;

import com.actionindicator.state.GoldenAppleState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.joml.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

public final class HealthHud {

    public static void render(DrawContext ctx, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || client.options.hudHidden) return;

        renderGappleTimer(ctx, client);
    }

    // Called from WorldRenderMixin after entity rendering — renders hearts above each player
    public static void renderPlayerHearts(DrawContext ctx, PlayerEntity target, MinecraftClient client) {
        if (target == client.player) return; // skip self

        float health    = target.getHealth();
        float maxHealth = target.getMaxHealth();
        float absorption = target.getAbsorptionAmount();

        int totalHearts    = (int) Math.ceil(maxHealth / 2);
        int fullHearts     = (int) (health / 2);
        float partialHeart = (health % 2) / 2f;
        int absHearts      = (int) Math.ceil(absorption / 2);

        int sw = client.getWindow().getScaledWidth();
        int sh = client.getWindow().getScaledHeight();

        // Project the entity's head position to screen space
        Vec3d camPos = client.gameRenderer.getCamera().getPos();
        Vec3d headPos = target.getPos().add(0, target.getStandingEyeHeight() + 0.6, 0);

        double[] screen = worldToScreen(headPos, camPos, client);
        if (screen == null) return; // behind camera

        int sx = (int) screen[0];
        int sy = (int) screen[1];

        // Draw hearts centred above the player's head
        int heartSize  = 7;
        int heartGap   = 1;
        int heartsPerRow = Math.min(totalHearts, 10);
        int rowW = heartsPerRow * (heartSize + heartGap);
        int startX = sx - rowW / 2;
        int startY = sy - 10;

        for (int i = 0; i < heartsPerRow; i++) {
            int hx = startX + i * (heartSize + heartGap);
            if (i < fullHearts) {
                drawHeart(ctx, hx, startY, 0xFFFF2222); // full red heart
            } else if (i == fullHearts && partialHeart > 0) {
                drawHeart(ctx, hx, startY, 0xFFFF6666); // half heart (lighter)
            } else {
                drawHeart(ctx, hx, startY, 0xFF555555); // empty heart
            }
        }

        // Absorption hearts in gold below
        if (absHearts > 0) {
            int absRowW = Math.min(absHearts, 10) * (heartSize + heartGap);
            int absStartX = sx - absRowW / 2;
            for (int i = 0; i < Math.min(absHearts, 10); i++) {
                drawHeart(ctx, absStartX + i * (heartSize + heartGap), startY - 10, 0xFFFFD700);
            }
        }

        // Health number above hearts
        String healthStr = String.format("%.1f", health);
        int textW = client.textRenderer.getWidth(healthStr);
        ctx.drawText(client.textRenderer, healthStr, sx - textW / 2, startY - 20, 0xFFFFFFFF, true);
    }

    // Draw a simple filled heart shape using rectangles
    private static void drawHeart(DrawContext ctx, int x, int y, int color) {
        // Heart pattern using pixel rectangles (7x7 grid)
        ctx.fill(x + 1, y,     x + 3, y + 1, color);
        ctx.fill(x + 4, y,     x + 6, y + 1, color);
        ctx.fill(x,     y + 1, x + 7, y + 4, color);
        ctx.fill(x + 1, y + 4, x + 6, y + 5, color);
        ctx.fill(x + 2, y + 5, x + 5, y + 6, color);
        ctx.fill(x + 3, y + 6, x + 4, y + 7, color);
    }

    // Project 3D world position to 2D screen coordinates
    private static double[] worldToScreen(Vec3d worldPos, Vec3d camPos, MinecraftClient client) {
        org.joml.Vector4f pos = new org.joml.Vector4f(
            (float)(worldPos.x - camPos.x),
            (float)(worldPos.y - camPos.y),
            (float)(worldPos.z - camPos.z),
            1.0f
        );

        org.joml.Matrix4f proj = client.gameRenderer.getBasicProjectionMatrix(
            client.options.getFov().getValue(), true);
        org.joml.Matrix4f view = new org.joml.Matrix4f(
            client.getEntityRenderDispatcher().getRotation().get(new org.joml.Matrix3f())
                .get(new org.joml.Matrix4f()));

        pos.mul(view).mul(proj);

        if (pos.w <= 0) return null; // behind camera

        float ndcX = pos.x / pos.w;
        float ndcY = pos.y / pos.w;

        int sw = client.getWindow().getScaledWidth();
        int sh = client.getWindow().getScaledHeight();

        double sx = (ndcX + 1.0) / 2.0 * sw;
        double sy = (1.0 - ndcY) / 2.0 * sh;

        return new double[]{sx, sy};
    }

    private static void renderGappleTimer(DrawContext ctx, MinecraftClient client) {
        int x = 10;
        int y = client.getWindow().getScaledHeight() - 80;

        if (GoldenAppleState.enchantedAppleTicksRemaining > 0) {
            int s = GoldenAppleState.enchantedAppleTicksRemaining / 20;
            int t = GoldenAppleState.enchantedAppleTicksRemaining % 20;
            ctx.drawText(client.textRenderer,
                String.format("EGap: %ds %dt", s, t), x, y, 0xFFFF55FF, true);
        } else if (GoldenAppleState.goldenAppleTicksRemaining > 0) {
            int s = GoldenAppleState.goldenAppleTicksRemaining / 20;
            int t = GoldenAppleState.goldenAppleTicksRemaining % 20;
            ctx.drawText(client.textRenderer,
                String.format("Gap: %ds %dt", s, t), x, y, 0xFFFFAA00, true);
        }
    }

    private HealthHud() {}
}

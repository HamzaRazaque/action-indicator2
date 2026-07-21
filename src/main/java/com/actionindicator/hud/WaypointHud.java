package com.actionindicator.hud;

import com.actionindicator.state.WaypointData;
import com.actionindicator.state.WaypointData.Waypoint;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Vec3d;

public final class WaypointHud {

    public static void render(DrawContext ctx, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;

        Vec3d pos = client.player.getPos();
        int sw = client.getWindow().getScaledWidth();
        int y = 10;

        for (Waypoint wp : WaypointData.waypoints) {
            double dist = Math.sqrt(
                Math.pow(pos.x - wp.x, 2) +
                Math.pow(pos.y - wp.y, 2) +
                Math.pow(pos.z - wp.z, 2));

            String label = wp.name + " [" + (int)wp.x + ", " + (int)wp.y + ", " + (int)wp.z + "] " + (int)dist + "m";
            int textW = client.textRenderer.getWidth(label);
            int x = (sw - textW) / 2;

            ctx.fill(x - 3, y - 2, x + textW + 3, y + 10, 0x88000000);
            ctx.drawText(client.textRenderer, label, x, y, wp.color, false);
            y += 14;
        }
    }

    private WaypointHud() {}
}

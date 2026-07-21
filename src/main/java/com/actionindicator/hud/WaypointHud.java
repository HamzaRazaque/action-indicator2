package com.actionindicator.hud;

import com.actionindicator.state.WaypointData;
import com.actionindicator.state.WaypointData.Waypoint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec3;

/**
 * Renders waypoint labels on the HUD with distance.
 */
public final class WaypointHud {

    public static void render(GuiGraphics graphics, float tickDelta) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui) return;

        Vec3 pos = client.player.position();
        int sw = client.getWindow().getGuiScaledWidth();
        int y  = 10;

        for (Waypoint wp : WaypointData.waypoints) {
            double dist = Math.sqrt(
                Math.pow(pos.x - wp.x, 2) +
                Math.pow(pos.y - wp.y, 2) +
                Math.pow(pos.z - wp.z, 2)
            );
            String label = wp.name + " [" + (int)wp.x + ", " + (int)wp.y + ", " + (int)wp.z + "] — " + (int)dist + "m";
            int textW = client.font.width(label);
            int x = (sw - textW) / 2;

            // Background pill
            graphics.fill(x - 3, y - 2, x + textW + 3, y + 10, 0x88000000);
            graphics.drawString(client.font, label, x, y, wp.color, false);
            y += 14;
        }
    }

    private WaypointHud() {}
}

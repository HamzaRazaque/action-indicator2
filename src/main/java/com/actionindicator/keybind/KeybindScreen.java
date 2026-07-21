package com.actionindicator.keybind;

import com.actionindicator.waypoint.WaypointScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Keybind info / menu screen — opened via the main keybind (default: Y).
 * Shows all mod keybinds and lets the player open sub-menus.
 */
public class KeybindScreen extends Screen {

    public KeybindScreen() {
        super(Component.literal("Action Indicator — Menu"));
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int startY = height / 2 - 50;

        // Open Waypoints screen
        addRenderableWidget(Button.builder(
            Component.literal("📍 Waypoints"),
            btn -> Minecraft.getInstance().setScreen(new WaypointScreen())
        ).pos(cx - 80, startY).size(160, 22).build());

        // Close
        addRenderableWidget(Button.builder(
            Component.literal("Close  [Y]"),
            btn -> onClose()
        ).pos(cx - 80, startY + 80).size(160, 22).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);

        int cx = width / 2;

        graphics.drawCenteredString(font, "§b§lAction Indicator", cx, height / 2 - 80, 0xFFFFFFFF);
        graphics.drawCenteredString(font, "§7Version 2.0", cx, height / 2 - 68, 0xFF888888);

        // Keybind reference table
        int tx = cx - 120;
        int ty = height / 2 - 20;
        graphics.drawString(font, "§eKeybinds:", tx, ty, 0xFFFFFF55, false);
        graphics.drawString(font, "§7[Y]  §fOpen this menu", tx, ty + 12, 0xFFFFFFFF, false);
        graphics.drawString(font, "§7[U]  §fOpen Waypoints", tx, ty + 24, 0xFFFFFFFF, false);
        graphics.drawString(font, "§7[H]  §fToggle health display", tx, ty + 36, 0xFFFFFFFF, false);

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

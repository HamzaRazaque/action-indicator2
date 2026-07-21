package com.actionindicator.waypoint;

import com.actionindicator.state.WaypointData;
import com.actionindicator.state.WaypointData.Waypoint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Waypoint menu screen — opened via keybind.
 * Lets the player:
 *   • Add a waypoint at their current position
 *   • Add a waypoint by typing X/Y/Z coordinates
 *   • See and delete existing waypoints
 */
public class WaypointScreen extends Screen {

    private EditBox nameBox, xBox, yBox, zBox;
    private final List<Button> deleteButtons = new ArrayList<>();

    // Colour palette for auto-assigning waypoint colours
    private static final int[] COLORS = {
        0xFFFF5555, 0xFF55FF55, 0xFF5555FF,
        0xFFFFFF55, 0xFFFF55FF, 0xFF55FFFF,
        0xFFFFAA00, 0xFFFFFFFF
    };
    private static int colorIndex = 0;

    public WaypointScreen() {
        super(Component.literal("Waypoints"));
    }

    @Override
    protected void init() {
        deleteButtons.clear();
        int cx = width / 2;
        int startY = 30;

        // ── Name field ────────────────────────────────────────────────────────
        nameBox = new EditBox(font, cx - 100, startY, 200, 20, Component.literal("Name"));
        nameBox.setHint(Component.literal("Waypoint name..."));
        addRenderableWidget(nameBox);

        // ── Coordinate fields ─────────────────────────────────────────────────
        xBox = new EditBox(font, cx - 100, startY + 25, 60, 20, Component.literal("X"));
        xBox.setHint(Component.literal("X"));
        addRenderableWidget(xBox);

        yBox = new EditBox(font, cx - 35, startY + 25, 60, 20, Component.literal("Y"));
        yBox.setHint(Component.literal("Y"));
        addRenderableWidget(yBox);

        zBox = new EditBox(font, cx + 30, startY + 25, 60, 20, Component.literal("Z"));
        zBox.setHint(Component.literal("Z"));
        addRenderableWidget(zBox);

        // ── Add at current position button ────────────────────────────────────
        addRenderableWidget(Button.builder(
            Component.literal("Add at My Position"),
            btn -> addAtCurrentPosition()
        ).pos(cx - 100, startY + 50).size(95, 20).build());

        // ── Add at typed coordinates button ───────────────────────────────────
        addRenderableWidget(Button.builder(
            Component.literal("Add at Coordinates"),
            btn -> addAtCoordinates()
        ).pos(cx + 5, startY + 50).size(95, 20).build());

        // ── Existing waypoints list with delete buttons ───────────────────────
        int listY = startY + 85;
        for (int i = 0; i < WaypointData.waypoints.size(); i++) {
            final int idx = i;
            Waypoint wp = WaypointData.waypoints.get(i);
            Button del = Button.builder(
                Component.literal("✕"),
                btn -> {
                    WaypointData.waypoints.remove(idx);
                    rebuildScreen();
                }
            ).pos(cx + 105, listY + i * 16).size(16, 14).build();
            addRenderableWidget(del);
            deleteButtons.add(del);
        }

        // ── Close button ──────────────────────────────────────────────────────
        addRenderableWidget(Button.builder(
            Component.literal("Close"),
            btn -> onClose()
        ).pos(cx - 40, height - 30).size(80, 20).build());
    }

    private void addAtCurrentPosition() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        String name = nameBox.getValue().trim();
        if (name.isEmpty()) name = "Waypoint " + (WaypointData.waypoints.size() + 1);

        double x = client.player.getX();
        double y = client.player.getY();
        double z = client.player.getZ();

        WaypointData.waypoints.add(new Waypoint(name, x, y, z, nextColor()));
        nameBox.setValue("");
        rebuildScreen();
    }

    private void addAtCoordinates() {
        String name = nameBox.getValue().trim();
        if (name.isEmpty()) name = "Waypoint " + (WaypointData.waypoints.size() + 1);

        try {
            double x = Double.parseDouble(xBox.getValue().trim());
            double y = Double.parseDouble(yBox.getValue().trim());
            double z = Double.parseDouble(zBox.getValue().trim());
            WaypointData.waypoints.add(new Waypoint(name, x, y, z, nextColor()));
            nameBox.setValue("");
            xBox.setValue("");
            yBox.setValue("");
            zBox.setValue("");
            rebuildScreen();
        } catch (NumberFormatException e) {
            // Invalid coords — flash the boxes red by doing nothing (user sees empty/bad input)
        }
    }

    private void rebuildScreen() {
        clearWidgets();
        init();
    }

    private static int nextColor() {
        int c = COLORS[colorIndex % COLORS.length];
        colorIndex++;
        return c;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);

        int cx = width / 2;

        // Title
        graphics.drawCenteredString(font, "§b§lWaypoints", cx, 10, 0xFFFFFFFF);

        // Column headers
        graphics.drawString(font, "§7Name", cx - 100, 78, 0xFFAAAAAA, false);
        graphics.drawString(font, "§7Coordinates", cx - 10, 78, 0xFFAAAAAA, false);

        // Waypoint list
        int listY = 115;
        for (int i = 0; i < WaypointData.waypoints.size(); i++) {
            Waypoint wp = WaypointData.waypoints.get(i);
            String entry = wp.toShortString();
            // Background
            graphics.fill(cx - 105, listY + i * 16 - 1, cx + 105, listY + i * 16 + 10, 0x44000000);
            graphics.drawString(font, entry, cx - 103, listY + i * 16, wp.color, false);
        }

        if (WaypointData.waypoints.isEmpty()) {
            graphics.drawCenteredString(font, "§7No waypoints yet", cx, listY, 0xFFAAAAAA);
        }

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false; // keep game running while menu is open
    }
}

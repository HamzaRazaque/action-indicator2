package com.actionindicator.waypoint;

import com.actionindicator.state.WaypointData;
import com.actionindicator.state.WaypointData.Waypoint;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class WaypointScreen extends Screen {

    private TextFieldWidget nameBox, xBox, yBox, zBox;

    private static final int[] COLORS = {
        0xFFFF5555, 0xFF55FF55, 0xFF5555FF,
        0xFFFFFF55, 0xFFFF55FF, 0xFF55FFFF,
        0xFFFFAA00, 0xFFFFFFFF
    };
    private static int colorIndex = 0;

    public WaypointScreen() {
        super(Text.literal("Waypoints"));
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int sy = 30;

        nameBox = new TextFieldWidget(textRenderer, cx - 100, sy, 200, 20, Text.literal("Name"));
        nameBox.setPlaceholder(Text.literal("Waypoint name..."));
        addDrawableChild(nameBox);

        xBox = new TextFieldWidget(textRenderer, cx - 100, sy + 25, 60, 20, Text.literal("X"));
        xBox.setPlaceholder(Text.literal("X"));
        addDrawableChild(xBox);

        yBox = new TextFieldWidget(textRenderer, cx - 35, sy + 25, 60, 20, Text.literal("Y"));
        yBox.setPlaceholder(Text.literal("Y"));
        addDrawableChild(yBox);

        zBox = new TextFieldWidget(textRenderer, cx + 30, sy + 25, 60, 20, Text.literal("Z"));
        zBox.setPlaceholder(Text.literal("Z"));
        addDrawableChild(zBox);

        addDrawableChild(ButtonWidget.builder(
            Text.literal("Add at My Position"),
            btn -> addAtPosition()
        ).dimensions(cx - 100, sy + 50, 95, 20).build());

        addDrawableChild(ButtonWidget.builder(
            Text.literal("Add at Coordinates"),
            btn -> addAtCoords()
        ).dimensions(cx + 5, sy + 50, 95, 20).build());

        int listY = sy + 90;
        for (int i = 0; i < WaypointData.waypoints.size(); i++) {
            final int idx = i;
            addDrawableChild(ButtonWidget.builder(
                Text.literal("X"),
                btn -> { WaypointData.waypoints.remove(idx); rebuildScreen(); }
            ).dimensions(cx + 105, listY + i * 16, 16, 14).build());
        }

        addDrawableChild(ButtonWidget.builder(
            Text.literal("Close"),
            btn -> close()
        ).dimensions(cx - 40, height - 28, 80, 20).build());
    }

    private void addAtPosition() {
        MinecraftClient c = MinecraftClient.getInstance();
        if (c.player == null) return;
        String name = nameBox.getText().trim();
        if (name.isEmpty()) name = "Waypoint " + (WaypointData.waypoints.size() + 1);
        WaypointData.waypoints.add(new Waypoint(name, c.player.getX(), c.player.getY(), c.player.getZ(), nextColor()));
        nameBox.setText("");
        rebuildScreen();
    }

    private void addAtCoords() {
        String name = nameBox.getText().trim();
        if (name.isEmpty()) name = "Waypoint " + (WaypointData.waypoints.size() + 1);
        try {
            double x = Double.parseDouble(xBox.getText().trim());
            double y = Double.parseDouble(yBox.getText().trim());
            double z = Double.parseDouble(zBox.getText().trim());
            WaypointData.waypoints.add(new Waypoint(name, x, y, z, nextColor()));
            nameBox.setText(""); xBox.setText(""); yBox.setText(""); zBox.setText("");
            rebuildScreen();
        } catch (NumberFormatException ignored) {}
    }

    private void rebuildScreen() { clearChildren(); init(); }

    private static int nextColor() {
        return COLORS[(colorIndex++) % COLORS.length];
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);
        int cx = width / 2;

        ctx.drawCenteredTextWithShadow(textRenderer, "Waypoints", cx, 10, 0xFF55FFFF);
        ctx.drawText(textRenderer, "Name & Coords", cx - 100, 76, 0xFFAAAAAA, false);

        int listY = 120;
        for (int i = 0; i < WaypointData.waypoints.size(); i++) {
            Waypoint wp = WaypointData.waypoints.get(i);
            ctx.fill(cx - 108, listY + i * 16 - 1, cx + 108, listY + i * 16 + 10, 0x44000000);
            ctx.drawText(textRenderer, wp.toShortString(), cx - 106, listY + i * 16, wp.color, false);
        }

        if (WaypointData.waypoints.isEmpty()) {
            ctx.drawCenteredTextWithShadow(textRenderer, "No waypoints yet", cx, listY, 0xFFAAAAAA);
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() { return false; }
}

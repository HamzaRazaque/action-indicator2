package com.actionindicator.keybind;

import com.actionindicator.waypoint.WaypointScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class KeybindScreen extends Screen {

    public KeybindScreen() {
        super(Text.literal("Action Indicator Menu"));
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int sy = height / 2 - 40;

        addDrawableChild(ButtonWidget.builder(
            Text.literal("Waypoints"),
            btn -> client.setScreen(new WaypointScreen())
        ).dimensions(cx - 80, sy, 160, 22).build());

        addDrawableChild(ButtonWidget.builder(
            Text.literal("Close  [Y]"),
            btn -> close()
        ).dimensions(cx - 80, sy + 60, 160, 22).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);
        int cx = width / 2;
        ctx.drawCenteredTextWithShadow(textRenderer, "Action Indicator", cx, height / 2 - 70, 0xFF55FFFF);
        ctx.drawCenteredTextWithShadow(textRenderer, "v2.0", cx, height / 2 - 58, 0xFF888888);

        int tx = cx - 100, ty = height / 2 - 15;
        ctx.drawText(textRenderer, "Keybinds:", tx, ty, 0xFFFFFF55, false);
        ctx.drawText(textRenderer, "[Y]  Open this menu", tx, ty + 12, 0xFFFFFFFF, false);
        ctx.drawText(textRenderer, "[U]  Open Waypoints", tx, ty + 24, 0xFFFFFFFF, false);
        ctx.drawText(textRenderer, "[H]  Toggle Health HUD", tx, ty + 36, 0xFFFFFFFF, false);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() { return false; }
}

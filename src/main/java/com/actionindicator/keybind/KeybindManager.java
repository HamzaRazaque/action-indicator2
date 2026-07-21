package com.actionindicator.keybind;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class KeybindManager {

    public static KeyBinding openMenu;
    public static KeyBinding openWaypoints;
    public static KeyBinding toggleHealth;

    public static void register() {
        openMenu = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.action-indicator.menu",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_Y,
            "category.action-indicator"
        ));

        openWaypoints = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.action-indicator.waypoints",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_U,
            "category.action-indicator"
        ));

        toggleHealth = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.action-indicator.health",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "category.action-indicator"
        ));
    }

    private KeybindManager() {}
}

package com.actionindicator.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/**
 * Registers all mod keybindings via Fabric API.
 */
public final class KeybindManager {

    public static KeyMapping openMenu;
    public static KeyMapping openWaypoints;
    public static KeyMapping toggleHealth;

    public static void register() {
        openMenu = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.action-indicator.menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Y,
            "category.action-indicator"
        ));

        openWaypoints = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.action-indicator.waypoints",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_U,
            "category.action-indicator"
        ));

        toggleHealth = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.action-indicator.health",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "category.action-indicator"
        ));
    }

    private KeybindManager() {}
}

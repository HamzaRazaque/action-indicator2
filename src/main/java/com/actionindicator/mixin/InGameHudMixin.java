package com.actionindicator.mixin;

import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Gui.class)
public class InGameHudMixin {
    // Intentionally empty — crosshair suppression not needed
}

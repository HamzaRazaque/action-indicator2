package com.actionindicator.mixin;

import com.actionindicator.state.ActionState;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Suppresses the vanilla crosshair while our flash ring is active.
 * Mojang-mapped names for 1.21.1:
 *   InGameHud   → Gui  (net.minecraft.client.gui.Gui)
 *   DrawContext → GuiGraphics
 */
@Mixin(Gui.class)
public class InGameHudMixin {

    @Inject(
        method = "renderCrosshair",
        at = @At("HEAD"),
        cancellable = true
    )
    private void ai$onRenderCrosshair(GuiGraphics graphics, net.minecraft.util.profiling.ProfilerFiller profiler, CallbackInfo ci) {
        if (ActionState.attackFlashTicksRemaining > 0) {
            ci.cancel();
        }
    }
}

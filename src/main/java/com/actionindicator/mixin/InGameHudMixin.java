package com.actionindicator.mixin;

import com.actionindicator.state.ActionState;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * InGameHudMixin
 *
 * Hooks the vanilla crosshair render method.
 *
 * When the attack-flash is active we CANCEL the vanilla crosshair draw so our
 * coloured ring (rendered in ActionIndicatorHud via HudRenderCallback) is the
 * only thing visible — cleaner than two overlapping crosshairs.
 *
 * The vanilla crosshair resumes the moment the flash timer reaches zero.
 *
 * NOTE: renderCrosshair is the Mojang-mapped name for the crosshair draw call
 * inside InGameHud. In 1.21.1 it is a private method called from render().
 * We target it by its exact Mojang name; Loom's official mappings resolve this.
 */
@Mixin(InGameHud.class)
public class InGameHudMixin {

    /**
     * Cancel the vanilla crosshair render when our flash ring is active.
     * The ring is drawn by ActionIndicatorHud after the HUD has finished,
     * so the order is: (vanilla HUD - crosshair skipped) → (our ring drawn).
     */
    @Inject(
        method = "renderCrosshair",
        at = @At("HEAD"),
        cancellable = true
    )
    private void ai$onRenderCrosshair(DrawContext context, float tickDelta, CallbackInfo ci) {
        if (ActionState.attackFlashTicksRemaining > 0) {
            // Suppress the white vanilla crosshair; our ring replaces it visually.
            ci.cancel();
        }
    }
}

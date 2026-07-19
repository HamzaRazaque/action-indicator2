package com.actionindicator.mixin;

import com.actionindicator.state.ActionState;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class InGameHudMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void ai$onRender(GuiGraphics graphics, float partialTick, CallbackInfo ci) {
        // No-op: crosshair suppression removed due to signature mismatch.
        // The chroma ring renders on top of the vanilla crosshair instead.
    }
}

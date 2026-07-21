package com.actionindicator.mixin;

import com.actionindicator.ActionIndicatorClient;
import com.actionindicator.hud.HealthHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class WorldRendererMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void ai$afterRender(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        if (!ActionIndicatorClient.showHealth) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        DrawContext ctx = new DrawContext(client, client.getRenderLayer(),
            client.getBufferBuilders().getEntityVertexConsumers());

        for (PlayerEntity player : client.world.getPlayers()) {
            HealthHud.renderPlayerHearts(ctx, player, client);
        }
    }
}

package net.syrupstudios.wellseasoned.mixin.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    /*? if fabric {*/
    /*@Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
    private void wellSeasoned$hideFood(
            GuiGraphics graphics,
            Player player,
            int top,
            int right,
            CallbackInfo callback
    ) {
        callback.cancel();
    }*/
    /*?} else {*/
    @Inject(method = "renderFoodLevel", at = @At("HEAD"), cancellable = true)
    private void wellSeasoned$hideFoodLevel(GuiGraphics graphics, CallbackInfo callback) {
        callback.cancel();
    }
    /*?}*/
}

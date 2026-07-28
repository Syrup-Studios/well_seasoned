package net.syrupstudios.wellseasoned.mixin.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Inject(method = "renderFoodLevel", at = @At("HEAD"), cancellable = true)
    private void wellSeasoned$hideFoodLevel(GuiGraphics graphics, CallbackInfo callback) {
        callback.cancel();
    }
}

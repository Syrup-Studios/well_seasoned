package net.syrupstudios.wellseasoned.mixin.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    /*? if >=1.20.5 {*/
    @Inject(method = "renderFoodLevel", at = @At("HEAD"), cancellable = true)
    private void wellSeasoned$hideFoodLevel(GuiGraphics graphics, CallbackInfo callback) {
        callback.cancel();
    }
    /*?} else {*/
    /*@Shadow
    private int getVehicleMaxHearts(LivingEntity vehicle) {
        throw new AssertionError();
    }

    @Redirect(
            method = "renderPlayerHealth",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Gui;getVehicleMaxHearts(Lnet/minecraft/world/entity/LivingEntity;)I"
            )
    )
    private int wellSeasoned$hideFoodLevel(Gui gui, LivingEntity vehicle) {
        return Math.max(1, getVehicleMaxHearts(vehicle));
    }*/
    /*?}*/
}

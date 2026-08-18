package net.syrupstudios.wellseasoned.mixin.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.syrupstudios.wellseasoned.client.FoodHealthPreview;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Shadow private int tickCount;
    @Shadow private RandomSource random;

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

    @Inject(method = "renderHearts", at = @At("RETURN"))
    private void wellSeasoned$renderFoodHealthPreview(
            GuiGraphics graphics,
            Player player,
            int left,
            int top,
            int rowHeight,
            int regenHeart,
            float maxHealthValue,
            int healthCeil,
            int displayHealth,
            int absorptionCeil,
            boolean blink,
            CallbackInfo callback
    ) {
        FoodHealthPreview.render(
                graphics,
                player,
                left,
                top,
                rowHeight,
                regenHeart,
                maxHealthValue,
                healthCeil,
                absorptionCeil,
                tickCount,
                random
        );
    }
}

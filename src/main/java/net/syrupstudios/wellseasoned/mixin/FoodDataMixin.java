package net.syrupstudios.wellseasoned.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {
    @Shadow private int foodLevel;
    @Shadow private float saturationLevel;
    @Shadow private float exhaustionLevel;
    @Shadow private int tickTimer;
    @Shadow private int lastFoodLevel;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void wellSeasoned$replaceHungerTick(Player player, CallbackInfo callback) {
        foodLevel = 20;
        lastFoodLevel = 20;
        saturationLevel = 5.0F;
        exhaustionLevel = 0.0F;
        tickTimer = 0;
        callback.cancel();
    }
}

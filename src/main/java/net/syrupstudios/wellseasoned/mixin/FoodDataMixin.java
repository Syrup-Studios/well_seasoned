package net.syrupstudios.wellseasoned.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.syrupstudios.wellseasoned.cooking.FoodConsumptionService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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
    @Unique private Player wellSeasoned$player;

    @Inject(method = "eat(IF)V", at = @At("HEAD"))
    private void wellSeasoned$healFromDirectFoodDataCall(
            int nutrition,
            float saturation,
            CallbackInfo callback
    ) {
        if (wellSeasoned$player != null && !FoodConsumptionService.isDirectFoodDataSuppressed()) {
            FoodConsumptionService.finishEatingUnprofiled(wellSeasoned$player, nutrition);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void wellSeasoned$replaceHungerTick(Player player, CallbackInfo callback) {
        wellSeasoned$player = player;
        foodLevel = 20;
        lastFoodLevel = 20;
        saturationLevel = 5.0F;
        exhaustionLevel = 0.0F;
        tickTimer = 0;
        callback.cancel();
    }
}

package net.syrupstudios.wellseasoned.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.syrupstudios.wellseasoned.cooking.FoodConsumptionService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "canEat", at = @At("HEAD"), cancellable = true)
    private void wellSeasoned$allowEatingAtAnyTime(boolean alwaysEat, CallbackInfoReturnable<Boolean> callback) {
        callback.setReturnValue(true);
    }

    @Redirect(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;heal(F)V"
            )
    )
    private void wellSeasoned$disablePeacefulNaturalHealing(Player player, float amount) {
        // Peaceful regeneration is implemented outside FoodData, so suppress only
        // the heal invocation inside Player.aiStep. Other healing remains untouched.
    }

    @Inject(method = "eat", at = @At("HEAD"))
    private void wellSeasoned$applyFoodBenefits(
            Level level,
            ItemStack stack,
            FoodProperties properties,
            CallbackInfoReturnable<ItemStack> callback
    ) {
        FoodConsumptionService.finishEating((Player) (Object) this, stack);
    }
}

package net.syrupstudios.wellseasoned.mixin;

import net.minecraft.world.entity.player.Player;
/*? if <1.20.5 {*/
/*import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
*//*?}*/
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.syrupstudios.wellseasoned.cooking.FoodConsumptionService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
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
            /*? if >=1.20.5 {*/
            FoodProperties properties,
            /*?}*/
            CallbackInfoReturnable<ItemStack> callback
    ) {
        /*? if >=1.20.5 {*/
        FoodConsumptionService.finishEating((Player) (Object) this, stack, properties);
        /*?} else {*/
        /*FoodConsumptionService.finishEating((Player) (Object) this, stack);*/
        /*?}*/
    }

    /*? if <1.20.5 && forge {*/
    /*@Redirect(
            method = "eat",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/food/FoodData;eat(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)V",
                    remap = false
            )
    )
    private void wellSeasoned$suppressNestedForgeFoodDataHealing(
            FoodData foodData,
            Item item,
            ItemStack stack,
            net.minecraft.world.entity.LivingEntity entity
    ) {
        FoodConsumptionService.beginDirectFoodDataSuppression();
        try {
            foodData.eat(item, stack, entity);
        } finally {
            FoodConsumptionService.endDirectFoodDataSuppression();
        }
    }
    */
    /*?}*/

    /*? if <1.20.5 && !forge {*/
    /*@Redirect(
            method = "eat",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/food/FoodData;eat(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/item/ItemStack;)V"
            )
    )
    private void wellSeasoned$suppressNestedFoodDataHealing(
            FoodData foodData,
            Item item,
            ItemStack stack
    ) {
        FoodConsumptionService.beginDirectFoodDataSuppression();
        try {
            foodData.eat(item, stack);
        } finally {
            FoodConsumptionService.endDirectFoodDataSuppression();
        }
    }
    */
    /*?}*/

    /*? if >=1.20.5 {*/
    @ModifyVariable(method = "eat", at = @At("HEAD"), argsOnly = true)
    private FoodProperties wellSeasoned$replaceHandledItemEffects(
            FoodProperties properties,
            Level level,
            ItemStack stack
    ) {
        return FoodConsumptionService.withoutItemEffects(stack, properties);
    }
    /*?}*/
}

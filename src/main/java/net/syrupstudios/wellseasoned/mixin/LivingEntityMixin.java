package net.syrupstudios.wellseasoned.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.syrupstudios.wellseasoned.WellSeasoned;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    /*? if <1.20.5 {*/
    /*@Inject(method = "addEatEffect", at = @At("HEAD"), cancellable = true)
    private void wellSeasoned$skipConfiguredPlayerFoodEffects(
            ItemStack stack,
            Level level,
            LivingEntity entity,
            CallbackInfo callback
    ) {
        if (entity instanceof net.minecraft.world.entity.player.Player
                && WellSeasoned.COOKING_DATA.food(
                        BuiltInRegistries.ITEM.getKey(stack.getItem())
                ).isPresent()) {
            callback.cancel();
        }
    }
    */
    /*?}*/
}

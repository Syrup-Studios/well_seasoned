package net.syrupstudios.wellseasoned.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.syrupstudios.wellseasoned.cooking.FoodConsumptionService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Routes vanilla cake-block consumption through the configured cake profile. */
@Mixin(CakeBlock.class)
public abstract class CakeBlockMixin {
    @Inject(method = "eat", at = @At("HEAD"))
    private static void wellSeasoned$applyCakeFood(
            LevelAccessor level,
            BlockPos pos,
            BlockState state,
            Player player,
            CallbackInfoReturnable<InteractionResult> callback
    ) {
        FoodConsumptionService.beginDirectFoodDataSuppression();
        FoodConsumptionService.finishEatingCake(player);
    }

    @Inject(method = "eat", at = @At("RETURN"))
    private static void wellSeasoned$finishTrackedCakeConsumption(
            LevelAccessor level,
            BlockPos pos,
            BlockState state,
            Player player,
            CallbackInfoReturnable<InteractionResult> callback
    ) {
        FoodConsumptionService.endDirectFoodDataSuppression();
    }
}

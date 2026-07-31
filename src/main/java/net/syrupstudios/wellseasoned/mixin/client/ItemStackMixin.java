package net.syrupstudios.wellseasoned.mixin.client;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.syrupstudios.wellseasoned.client.FoodHeartTooltip;
import net.syrupstudios.wellseasoned.client.FoodTooltip;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(method = "getTooltipLines", at = @At("RETURN"))
    private void wellSeasoned$addFoodDetails(
            Item.TooltipContext context,
            Player player,
            TooltipFlag flag,
            CallbackInfoReturnable<List<Component>> callback
    ) {
        List<Component> tooltip = callback.getReturnValue();
        if (tooltip.isEmpty()) {
            return;
        }

        List<Component> foodLines = FoodTooltip.effectLines((ItemStack) (Object) this, context);
        if (!foodLines.isEmpty()) {
            tooltip.addAll(1, foodLines);
        }
    }

    @Inject(method = "getTooltipImage", at = @At("RETURN"), cancellable = true)
    private void wellSeasoned$addHeartDisplay(
            CallbackInfoReturnable<Optional<TooltipComponent>> callback
    ) {
        ItemStack stack = (ItemStack) (Object) this;
        if (callback.getReturnValue().isPresent() || stack.has(DataComponents.HIDE_TOOLTIP)) {
            return;
        }

        int quarters = FoodTooltip.heartQuarters(stack);
        if (quarters >= 0) {
            callback.setReturnValue(Optional.of(new FoodHeartTooltip(quarters)));
        }
    }
}

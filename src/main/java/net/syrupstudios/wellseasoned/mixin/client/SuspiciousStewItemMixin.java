package net.syrupstudios.wellseasoned.mixin.client;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Keeps the flower-dependent suspicious-stew effect a surprise. Vanilla reveals
 * it in creative-mode hover text, which defeats the point of the hidden effect.
 */
@Mixin(SuspiciousStewItem.class)
public abstract class SuspiciousStewItemMixin {
    @Inject(method = "appendHoverText", at = @At("HEAD"), cancellable = true)
    private void wellSeasoned$hideSuspiciousStewEffect(
            ItemStack stack,
            /*? if >=1.20.5 {*/
            Item.TooltipContext context,
            /*?} else {*/
            /*net.minecraft.world.level.Level level,*/
            /*?}*/
            List<Component> tooltip,
            TooltipFlag flag,
            CallbackInfo callback
    ) {
        callback.cancel();
    }
}

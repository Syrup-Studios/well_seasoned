package net.syrupstudios.wellseasoned.mixin.client;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.syrupstudios.wellseasoned.client.ClientFoodHeartTooltip;
import net.syrupstudios.wellseasoned.client.FoodHeartTooltip;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientTooltipComponent.class)
public interface ClientTooltipComponentMixin {
    //? if !forge {
    @Inject(method = "create(Lnet/minecraft/world/inventory/tooltip/TooltipComponent;)"
            + "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;",
            at = @At("HEAD"),
            cancellable = true)
    private static void wellSeasoned$createHeartDisplay(
            TooltipComponent component,
            CallbackInfoReturnable<ClientTooltipComponent> callback
    ) {
        if (component instanceof FoodHeartTooltip hearts) {
            callback.setReturnValue(new ClientFoodHeartTooltip(hearts.quarters()));
        }
    }
    //?}
}

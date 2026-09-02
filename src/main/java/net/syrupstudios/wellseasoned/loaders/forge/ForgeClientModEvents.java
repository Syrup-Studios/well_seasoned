package net.syrupstudios.wellseasoned.loaders.forge;

//? if forge {
/*import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.syrupstudios.wellseasoned.WellSeasoned;
import net.syrupstudios.wellseasoned.client.ClientFoodHeartTooltip;
import net.syrupstudios.wellseasoned.client.FoodHeartTooltip;

@Mod.EventBusSubscriber(
        modid = WellSeasoned.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public final class ForgeClientModEvents {
    private ForgeClientModEvents() {
    }

    @SubscribeEvent
    public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(
                FoodHeartTooltip.class,
                hearts -> new ClientFoodHeartTooltip(hearts.quarters())
        );
    }
}
*///?}

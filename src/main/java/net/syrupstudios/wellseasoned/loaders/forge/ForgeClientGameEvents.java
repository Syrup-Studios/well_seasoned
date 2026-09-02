package net.syrupstudios.wellseasoned.loaders.forge;

//? if forge {
/*import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.syrupstudios.wellseasoned.WellSeasoned;

@Mod.EventBusSubscriber(
        modid = WellSeasoned.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public final class ForgeClientGameEvents {
    private ForgeClientGameEvents() {
    }

    @SubscribeEvent
    public static void hideFoodOverlay(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay().id().equals(VanillaGuiOverlay.FOOD_LEVEL.id())) {
            event.setCanceled(true);
        }
    }
}
*///?}

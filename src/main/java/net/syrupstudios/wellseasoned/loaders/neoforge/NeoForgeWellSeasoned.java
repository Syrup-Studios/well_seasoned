package net.syrupstudios.wellseasoned.loaders.neoforge;

//? if neoforge {
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.syrupstudios.wellseasoned.WellSeasoned;
import net.syrupstudios.wellseasoned.cooking.CookingReloadListener;

// NeoForge entrypoint for Well Seasoned.
@Mod(WellSeasoned.MOD_ID)
public final class NeoForgeWellSeasoned {
    public NeoForgeWellSeasoned(IEventBus modBus) {
        WellSeasoned.initialize();
        NeoForge.EVENT_BUS.addListener(this::addReloadListeners);
    }

    private void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new CookingReloadListener(WellSeasoned.COOKING_DATA));
    }
}
//?}

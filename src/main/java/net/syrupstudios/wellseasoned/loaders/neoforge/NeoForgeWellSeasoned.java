package net.syrupstudios.wellseasoned.loaders.neoforge;

//? if neoforge {
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.syrupstudios.wellseasoned.WellSeasoned;
import net.syrupstudios.wellseasoned.cooking.CookingReloadListener;
import net.syrupstudios.wellseasoned.network.CookingDataPayload;

// NeoForge entrypoint for Well Seasoned.
@Mod(WellSeasoned.MOD_ID)
public final class NeoForgeWellSeasoned {
    public NeoForgeWellSeasoned(IEventBus modBus) {
        WellSeasoned.initialize();
        modBus.addListener(this::registerPayloads);
        NeoForge.EVENT_BUS.addListener(this::addReloadListeners);
        NeoForge.EVENT_BUS.addListener(this::syncCookingData);
    }

    private void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new CookingReloadListener(WellSeasoned.COOKING_DATA));
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("2").playToClient(
                CookingDataPayload.TYPE,
                CookingDataPayload.STREAM_CODEC,
                (payload, context) -> WellSeasoned.COOKING_DATA.replace(payload.snapshot())
        );
    }

    private void syncCookingData(OnDatapackSyncEvent event) {
        CookingDataPayload payload = new CookingDataPayload(WellSeasoned.COOKING_DATA.snapshot());
        event.getRelevantPlayers().forEach(player -> PacketDistributor.sendToPlayer(player, payload));
    }
}
//?}

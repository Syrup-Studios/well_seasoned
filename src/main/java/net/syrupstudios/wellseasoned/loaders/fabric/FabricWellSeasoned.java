package net.syrupstudios.wellseasoned.loaders.fabric;

//? if fabric {
/*import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.packs.PackType;
import net.syrupstudios.wellseasoned.WellSeasoned;
import net.syrupstudios.wellseasoned.cooking.CookingReloadListener;
import net.syrupstudios.wellseasoned.network.CookingDataPayload;

// Fabric entrypoint for Well Seasoned.
public final class FabricWellSeasoned implements ModInitializer {
    @Override
    public void onInitialize() {
        WellSeasoned.initialize();
        PayloadTypeRegistry.playS2C().register(CookingDataPayload.TYPE, CookingDataPayload.STREAM_CODEC);
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(
                new CookingReloadListener(WellSeasoned.COOKING_DATA)
        );
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                ServerPlayNetworking.send(
                        handler.player,
                        new CookingDataPayload(WellSeasoned.COOKING_DATA.snapshot())
                )
        );
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            if (success) {
                CookingDataPayload payload = new CookingDataPayload(WellSeasoned.COOKING_DATA.snapshot());
                for (var player : server.getPlayerList().getPlayers()) {
                    ServerPlayNetworking.send(player, payload);
                }
            }
        });
    }
}
*///?}

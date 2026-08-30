package net.syrupstudios.wellseasoned.loaders.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
/*? if >=1.20.5 {*/
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
/*?}*/
import net.minecraft.server.packs.PackType;
import net.syrupstudios.wellseasoned.WellSeasoned;
import net.syrupstudios.wellseasoned.cooking.CookingReloadListener;
import net.syrupstudios.wellseasoned.network.CookingDataPayload;

// Fabric entrypoint for Well Seasoned.
public final class FabricWellSeasoned implements ModInitializer {
    @Override
    public void onInitialize() {
        WellSeasoned.initialize();
        /*? if >=1.20.5 {*/
        PayloadTypeRegistry.playS2C().register(CookingDataPayload.TYPE, CookingDataPayload.STREAM_CODEC);
        /*?} else {*/
        /*ServerPlayNetworking.registerGlobalReceiver(WellSeasoned.id("cooking_data"), (server, player, handler, buffer, responseSender) ->
                server.execute(() -> WellSeasoned.COOKING_DATA.replace(CookingDataPayload.decode(buffer).snapshot())));*/
        /*?}*/
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(
                new CookingReloadListener(WellSeasoned.COOKING_DATA)
        );
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
                /*? if >=1.20.5 {*/
                ServerPlayNetworking.send(
                        handler.player,
                        new CookingDataPayload(WellSeasoned.COOKING_DATA.snapshot())
                );
                /*?} else {*/
                /*var buffer = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
                CookingDataPayload.encode(buffer, new CookingDataPayload(WellSeasoned.COOKING_DATA.snapshot()));
                ServerPlayNetworking.send(handler.player, WellSeasoned.id("cooking_data"), buffer);*/
                /*?}*/
        });
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            if (success) {
                CookingDataPayload payload = new CookingDataPayload(WellSeasoned.COOKING_DATA.snapshot());
                for (var player : server.getPlayerList().getPlayers()) {
                    /*? if >=1.20.5 {*/
                    ServerPlayNetworking.send(player, payload);
                    /*?} else {*/
                    /*var buffer = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
                    CookingDataPayload.encode(buffer, payload);
                    ServerPlayNetworking.send(player, WellSeasoned.id("cooking_data"), buffer);*/
                    /*?}*/
                }
            }
        });
    }
}

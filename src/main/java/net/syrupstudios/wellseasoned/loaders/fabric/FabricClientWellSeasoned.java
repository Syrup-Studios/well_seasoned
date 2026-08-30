package net.syrupstudios.wellseasoned.loaders.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.syrupstudios.wellseasoned.WellSeasoned;
import net.syrupstudios.wellseasoned.network.CookingDataPayload;

public final class FabricClientWellSeasoned implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        /*? if >=1.20.5 {*/
        ClientPlayNetworking.registerGlobalReceiver(CookingDataPayload.TYPE, (payload, context) ->
                WellSeasoned.COOKING_DATA.replace(payload.snapshot())
        );
        /*?} else {*/
        /*ClientPlayNetworking.registerGlobalReceiver(WellSeasoned.id("cooking_data"), (client, handler, buffer, responseSender) ->
                client.execute(() -> WellSeasoned.COOKING_DATA.replace(CookingDataPayload.decode(buffer).snapshot())));*/
        /*?}*/
    }
}

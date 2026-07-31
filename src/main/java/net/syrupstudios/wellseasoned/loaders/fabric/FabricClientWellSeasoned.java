package net.syrupstudios.wellseasoned.loaders.fabric;

//? if fabric {
/*import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.syrupstudios.wellseasoned.WellSeasoned;
import net.syrupstudios.wellseasoned.network.CookingDataPayload;

public final class FabricClientWellSeasoned implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(CookingDataPayload.TYPE, (payload, context) ->
                WellSeasoned.COOKING_DATA.replace(payload.snapshot())
        );
    }
}
*///?}

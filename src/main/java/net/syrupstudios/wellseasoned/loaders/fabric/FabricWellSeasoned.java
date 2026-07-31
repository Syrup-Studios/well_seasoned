package net.syrupstudios.wellseasoned.loaders.fabric;

//? if fabric {
/*import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;
import net.syrupstudios.wellseasoned.WellSeasoned;
import net.syrupstudios.wellseasoned.cooking.CookingReloadListener;

// Fabric entrypoint for Well Seasoned.
public final class FabricWellSeasoned implements ModInitializer {
    @Override
    public void onInitialize() {
        WellSeasoned.initialize();
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(
                new CookingReloadListener(WellSeasoned.COOKING_DATA)
        );
    }
}
*///?}

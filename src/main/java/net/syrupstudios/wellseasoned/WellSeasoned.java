package net.syrupstudios.wellseasoned;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.syrupstudios.wellseasoned.cooking.CookingDataManager;
import net.syrupstudios.wellseasoned.cooking.WellSeasonedConfig;
import org.slf4j.Logger;

/** Common entrypoint for Well Seasoned. */
public final class WellSeasoned {
    public static final String MOD_ID = "well_seasoned";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final CookingDataManager COOKING_DATA = new CookingDataManager();

    private static boolean initialized;

    private WellSeasoned() {
    }

    /** Initializes loader-independent mod content once. */
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;
        WellSeasonedConfig.load();
        LOGGER.info("Well Seasoned is ready to cook");
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}

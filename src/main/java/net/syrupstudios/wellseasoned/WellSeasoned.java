package net.syrupstudios.wellseasoned;


/** Common entrypoint for Well Seasoned. */
public final class WellSeasoned {
    public static final String MOD_ID = "well_seasoned";

    private static boolean initialized;

    private WellSeasoned() {
    }

    /** Initializes loader-independent mod content once. */
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;
    }
}

package net.syrupstudios.wellseasoned.loaders;

import java.nio.file.Path;

/** Loader-specific services used by shared Well Seasoned code. */
public interface Platform {
    //? if fabric
    /*Platform INSTANCE = new net.syrupstudios.wellseasoned.loaders.fabric.FabricPlatformImpl();*/
    //? if forge
    /*Platform INSTANCE = new net.syrupstudios.wellseasoned.loaders.forge.ForgePlatformImpl();*/
    //? if neoforge
    Platform INSTANCE = new net.syrupstudios.wellseasoned.loaders.neoforge.NeoForgePlatformImpl();

    /** Returns whether a mod with the supplied ID is loaded. */
    boolean isModLoaded(String modId);

    /** Returns whether the game is running in a client environment. */
    boolean isClientSide();

    /** Returns whether the game is running in a dedicated-server environment. */
    boolean isServerSide();

    /** Returns the normalized directory in which mod configuration files are stored. */
    Path configDirectory();

    /** Returns the lowercase loader identifier. */
    String loader();
}

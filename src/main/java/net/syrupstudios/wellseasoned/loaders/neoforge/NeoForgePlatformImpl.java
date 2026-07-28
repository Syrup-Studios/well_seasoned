package net.syrupstudios.wellseasoned.loaders.neoforge;

//? if neoforge {
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.syrupstudios.wellseasoned.loaders.Platform;

import java.nio.file.Path;

// NeoForge implementation of shared loader services.
public final class NeoForgePlatformImpl implements Platform {
    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isClientSide() {
        return FMLEnvironment.dist == Dist.CLIENT;
    }

    @Override
    public boolean isServerSide() {
        return FMLEnvironment.dist == Dist.DEDICATED_SERVER;
    }

    @Override
    public Path configDirectory() {
        return FMLPaths.CONFIGDIR.get().toAbsolutePath().normalize();
    }

    @Override
    public String loader() {
        return "neoforge";
    }
}
//?}

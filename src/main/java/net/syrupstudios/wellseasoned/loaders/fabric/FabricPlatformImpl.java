package net.syrupstudios.wellseasoned.loaders.fabric;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.syrupstudios.wellseasoned.loaders.Platform;

import java.nio.file.Path;

// Fabric implementation of shared loader services.
public final class FabricPlatformImpl implements Platform {
    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isClientSide() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    @Override
    public boolean isServerSide() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;
    }

    @Override
    public Path configDirectory() {
        return FabricLoader.getInstance().getConfigDir().toAbsolutePath().normalize();
    }

    @Override
    public String loader() {
        return "fabric";
    }
}

package net.syrupstudios.wellseasoned.loaders.forge;

//? if forge {
/*import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraft.resources.ResourceLocation;
import net.syrupstudios.wellseasoned.WellSeasoned;
import net.syrupstudios.wellseasoned.cooking.CookingReloadListener;
import net.syrupstudios.wellseasoned.network.CookingDataPayload;

@Mod(WellSeasoned.MOD_ID)
public final class ForgeWellSeasoned {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            WellSeasoned.id("main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    public ForgeWellSeasoned() {
        WellSeasoned.initialize();
        CHANNEL.registerMessage(
                0,
                CookingDataPayload.class,
                CookingDataPayload::encode,
                CookingDataPayload::decode,
                (payload, context) -> context.get().enqueueWork(() ->
                        WellSeasoned.COOKING_DATA.replace(payload.snapshot())
                )
        );
        MinecraftForge.EVENT_BUS.addListener(this::addReloadListeners);
        MinecraftForge.EVENT_BUS.addListener(this::syncCookingData);
    }

    private void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new CookingReloadListener(WellSeasoned.COOKING_DATA));
    }

    private void syncCookingData(OnDatapackSyncEvent event) {
        CookingDataPayload payload = new CookingDataPayload(WellSeasoned.COOKING_DATA.snapshot());
        if (event.getPlayer() != null) {
            CHANNEL.send(PacketDistributor.PLAYER.with(event::getPlayer), payload);
        } else {
            CHANNEL.send(PacketDistributor.ALL.noArg(), payload);
        }
    }
}
*///?}

package net.syrupstudios.wellseasoned.loaders.neoforge;

//? if neoforge {
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.minecraft.server.level.ServerPlayer;
import net.syrupstudios.wellseasoned.WellSeasoned;
import net.syrupstudios.wellseasoned.cooking.CookingReloadListener;
import net.syrupstudios.wellseasoned.cooking.RecipeDiscoveryService;

// NeoForge entrypoint for Well Seasoned.
@Mod(WellSeasoned.MOD_ID)
public final class NeoForgeWellSeasoned {
    public NeoForgeWellSeasoned(IEventBus modBus) {
        WellSeasoned.initialize();
        NeoForgeContent.register(modBus);
        NeoForge.EVENT_BUS.addListener(this::addReloadListeners);
        NeoForge.EVENT_BUS.addListener(this::pickUpItem);
        NeoForge.EVENT_BUS.addListener(this::craftItem);
        NeoForge.EVENT_BUS.addListener(this::smeltItem);
        NeoForge.EVENT_BUS.addListener(this::playerLoggedIn);
    }

    private void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new CookingReloadListener(WellSeasoned.COOKING_DATA));
    }

    private void pickUpItem(ItemEntityPickupEvent.Post event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            RecipeDiscoveryService.discover(player, event.getOriginalStack());
        }
    }

    private void craftItem(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RecipeDiscoveryService.discover(player, event.getCrafting());
        }
    }

    private void smeltItem(PlayerEvent.ItemSmeltedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RecipeDiscoveryService.discover(player, event.getSmelting());
        }
    }

    private void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RecipeDiscoveryService.discoverInventory(player);
        }
    }
}
//?}

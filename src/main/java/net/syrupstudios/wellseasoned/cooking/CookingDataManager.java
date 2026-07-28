package net.syrupstudios.wellseasoned.cooking;

import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

/** Holds the last completely validated cooking-data reload. */
public final class CookingDataManager {
    private volatile CookingDataSnapshot snapshot = CookingDataSnapshot.EMPTY;

    public CookingDataSnapshot snapshot() {
        return snapshot;
    }

    public Optional<FoodProfile> food(ResourceLocation item) {
        return snapshot.food(item);
    }

    public void replace(CookingDataSnapshot replacement) {
        snapshot = replacement;
    }
}

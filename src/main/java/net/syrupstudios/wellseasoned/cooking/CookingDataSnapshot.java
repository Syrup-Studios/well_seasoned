package net.syrupstudios.wellseasoned.cooking;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;

public record CookingDataSnapshot(
        Map<ResourceLocation, IntrinsicDefinition> intrinsics,
        Map<ResourceLocation, FoodProfile> foods
) {
    public static final CookingDataSnapshot EMPTY = new CookingDataSnapshot(Map.of(), Map.of());

    public CookingDataSnapshot {
        intrinsics = Map.copyOf(intrinsics);
        foods = Map.copyOf(foods);
    }

    public Optional<FoodProfile> food(ResourceLocation item) {
        return Optional.ofNullable(foods.get(item));
    }

    public Optional<IntrinsicDefinition> intrinsic(ResourceLocation id) {
        return Optional.ofNullable(intrinsics.get(id));
    }
}

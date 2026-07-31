package net.syrupstudios.wellseasoned.cooking;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record FoodProfile(
        ResourceLocation item,
        PreparationTier tier,
        float healing,
        List<ResourceLocation> intrinsics
) {
    public FoodProfile {
        intrinsics = List.copyOf(intrinsics);
    }

    public float effectiveHealing() {
        return healing * tier.healingMultiplier();
    }
}

package net.syrupstudios.wellseasoned.cooking;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.syrupstudios.wellseasoned.WellSeasoned;

/**
 * Single source of truth for how much health Well Seasoned restores from food.
 * Used by food consumption, tooltips, and the client-side HUD preview.
 */
public final class FoodHealingResolver {
    private FoodHealingResolver() {
    }

    /**
     * Resolves the healing of a stack. Items without a FOOD component (such as
     * the cake block item) still honor a configured profile, otherwise zero.
     */
    public static float resolve(ItemStack stack) {
        FoodProperties food = FoodCompat.food(stack);
        if (food == null) {
            return resolveConfigured(stack);
        }
        return resolve(stack, food);
    }

    /** Resolves the healing for an edible stack using its already-known FOOD component. */
    public static float resolve(ItemStack stack, FoodProperties foodProperties) {
        float configured = resolveConfigured(stack);
        if (configured >= 0.0F) {
            return configured;
        }

        return resolveNutrition(FoodCompat.nutrition(foodProperties));
    }

    /** Resolves fallback healing when no item or configured profile is known. */
    public static float resolveNutrition(int nutrition) {
        return nutrition > 0 ? Math.max(1.0F, nutrition * 0.5F) : 0.0F;
    }

    /**
     * Returns the profile healing for a stack, or -1 when no profile applies.
     * The -1 sentinel distinguishes "no profile" from a legitimate zero-healing
     * configured food.
     */
    private static float resolveConfigured(ItemStack stack) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        FoodProfile profile = WellSeasoned.COOKING_DATA.food(itemId).orElse(null);
        if (profile != null) {
            return profile.effectiveHealing();
        }
        return -1.0F;
    }
}

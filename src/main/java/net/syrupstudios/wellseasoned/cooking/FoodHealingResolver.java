package net.syrupstudios.wellseasoned.cooking;

import net.minecraft.core.component.DataComponents;
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

    /** Resolves the healing of the stack's FOOD component, or zero for non-food stacks. */
    public static float resolve(ItemStack stack) {
        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food == null) {
            return 0.0F;
        }
        return resolve(stack, food);
    }

    /** Resolves the healing for an edible stack using its already-known FOOD component. */
    public static float resolve(ItemStack stack, FoodProperties foodProperties) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        FoodProfile profile = WellSeasoned.COOKING_DATA.food(itemId).orElse(null);
        if (profile != null) {
            return profile.effectiveHealing();
        }

        int nutrition = foodProperties.nutrition();
        return nutrition > 0 ? Math.max(1.0F, nutrition * 0.5F) : 0.0F;
    }
}

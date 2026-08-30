package net.syrupstudios.wellseasoned.cooking;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

/** Small API bridge for the pre-data-component food model used by 1.20.1. */
public final class FoodCompat {
    private FoodCompat() {
    }

    public static FoodProperties food(ItemStack stack) {
        /*? if >=1.20.5 {*/
        return stack.get(net.minecraft.core.component.DataComponents.FOOD);
        /*?} else {*/
        /*return stack.getItem().getFoodProperties();*/
        /*?}*/
    }

    public static boolean isFood(ItemStack stack) {
        return food(stack) != null;
    }

    public static int nutrition(FoodProperties food) {
        /*? if >=1.20.5 {*/
        return food.nutrition();
        /*?} else {*/
        /*return food.getNutrition();*/
        /*?}*/
    }

    public static float saturation(FoodProperties food) {
        /*? if >=1.20.5 {*/
        return food.saturation();
        /*?} else {*/
        /*return food.getSaturationModifier();*/
        /*?}*/
    }
}

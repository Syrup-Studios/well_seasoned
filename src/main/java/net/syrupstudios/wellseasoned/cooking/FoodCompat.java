package net.syrupstudios.wellseasoned.cooking;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

/** Small API bridge for the pre-data-component food model used by 1.20.1. */
public final class FoodCompat {
    private FoodCompat() {
    }

    public static FoodProperties food(ItemStack stack) {
        return food(stack, null);
    }

    /**
     * Returns the food properties for a stack in the same way vanilla resolves
     * them for the supplied entity.
     */
    public static FoodProperties food(ItemStack stack, LivingEntity entity) {
        /*? if >=1.20.5 {*/
        return stack.get(net.minecraft.core.component.DataComponents.FOOD);
        /*?} elif forge {*/
        /*return stack.getFoodProperties(entity);*/
        /*?} else {*/
        /*return stack.getItem().getFoodProperties();*/
        /*?}*/
    }

    public static boolean isFood(ItemStack stack) {
        return isFood(stack, null);
    }

    public static boolean isFood(ItemStack stack, LivingEntity entity) {
        return food(stack, entity) != null;
    }

    public static int nutrition(FoodProperties food) {
        /*? if >=1.20.5 {*/
        return food.nutrition();
        /*?} else {*/
        /*return food.getNutrition();*/
        /*?}*/
    }

}

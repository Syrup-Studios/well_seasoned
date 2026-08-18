package net.syrupstudios.wellseasoned.cooking;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ChorusFruitItem;
import net.minecraft.world.item.HoneyBottleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression guards for vanilla item behaviors that live outside the FOOD
 * component and must survive the food-effect handling.
 */
class VanillaMechanicsTest extends CookingDataTestSupport {
    @Test
    void chorusFruitTeleportIsItemLevelAndNotStripped() {
        assertInstanceOf(ChorusFruitItem.class, Items.CHORUS_FRUIT);

        ItemStack stack = stack("chorus_fruit");
        var stripped = FoodConsumptionService.withoutItemEffects(stack, food(stack));

        assertTrue(stripped.effects().isEmpty());
        assertEquals(food(stack).nutrition(), stripped.nutrition());
        assertEquals(food(stack).saturation(), stripped.saturation());
    }

    @Test
    void honeyBottlePoisonRemovalIsItemLevelAndNotStripped() {
        assertInstanceOf(HoneyBottleItem.class, Items.HONEY_BOTTLE);

        ItemStack stack = stack("honey_bottle");
        var stripped = FoodConsumptionService.withoutItemEffects(stack, food(stack));

        assertTrue(stripped.effects().isEmpty());
        assertEquals(food(stack).nutrition(), stripped.nutrition());
        assertEquals(food(stack).usingConvertsTo(), stripped.usingConvertsTo());
    }

    @Test
    void suspiciousStewDynamicEffectIsItemLevelAndNotStripped() {
        assertInstanceOf(SuspiciousStewItem.class, Items.SUSPICIOUS_STEW);
        assertTrue(stack("suspicious_stew").has(DataComponents.SUSPICIOUS_STEW_EFFECTS));
    }
}

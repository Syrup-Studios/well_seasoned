package net.syrupstudios.wellseasoned.client;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.syrupstudios.wellseasoned.cooking.CookingDataTestSupport;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FoodTooltipTest extends CookingDataTestSupport {
    @Test
    void appleTooltipShowsRegenerationAndDuration() {
        String line = singleLine("apple");

        assertTrue(line.contains("effect.minecraft.regeneration"), "expected regeneration in: " + line);
        assertTrue(line.contains("00:05"), "expected 5 second duration in: " + line);
        assertFalse(line.contains("well_seasoned.food_effect.chance"), "apple effect is guaranteed: " + line);
    }

    @Test
    void appleHeartDisplayShowsTwoHealthPoints() {
        assertEquals(4, FoodTooltip.heartQuarters(stack("apple")));
    }

    @Test
    void rawChickenTooltipShowsChance() {
        String line = singleLine("chicken");

        assertTrue(line.contains("effect.minecraft.nausea"), "expected nausea in: " + line);
        assertTrue(line.contains("00:05"), "expected 5 second duration in: " + line);
        assertTrue(line.contains("well_seasoned.food_effect.chance"), "expected chance marker in: " + line);
        assertTrue(line.contains("30"), "expected 30 percent chance in: " + line);
    }

    @Test
    void carrotShowsNoEffectLines() {
        assertTrue(FoodTooltip.effectLines(stack("carrot"), net.minecraft.world.item.Item.TooltipContext.EMPTY).isEmpty());
    }

    @Test
    void suspiciousStewNeverRevealsItsEffect() {
        ItemStack stack = stack("suspicious_stew");
        stack.set(
                net.minecraft.core.component.DataComponents.SUSPICIOUS_STEW_EFFECTS,
                new net.minecraft.world.item.component.SuspiciousStewEffects(java.util.List.of(
                        new net.minecraft.world.item.component.SuspiciousStewEffects.Entry(
                                net.minecraft.world.effect.MobEffects.REGENERATION,
                                100
                        )
                ))
        );

        assertTrue(
                FoodTooltip.effectLines(stack, net.minecraft.world.item.Item.TooltipContext.EMPTY).isEmpty(),
                "the hidden suspicious-stew effect must not appear in the Well Seasoned tooltip"
        );
    }

    private static String singleLine(String itemId) {
        ItemStack stack = stack(itemId);
        List<Component> lines = FoodTooltip.effectLines(stack, net.minecraft.world.item.Item.TooltipContext.EMPTY);
        assertTrue(!lines.isEmpty(), "expected a tooltip line for minecraft:" + itemId);
        return flatten(lines.getFirst());
    }

    private static String flatten(Component component) {
        StringBuilder builder = new StringBuilder();
        if (component.getContents() instanceof PlainTextContents.LiteralContents literal) {
            builder.append(literal.text());
        } else if (component.getContents() instanceof TranslatableContents translatable) {
            builder.append(translatable.getKey());
            for (Object argument : translatable.getArgs()) {
                builder.append('[');
                if (argument instanceof Component nested) {
                    builder.append(flatten(nested));
                } else {
                    builder.append(argument);
                }
                builder.append(']');
            }
        }
        for (Component sibling : component.getSiblings()) {
            builder.append(' ').append(flatten(sibling));
        }
        return builder.toString();
    }
}

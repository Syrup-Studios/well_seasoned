package net.syrupstudios.wellseasoned.client;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.syrupstudios.wellseasoned.WellSeasoned;
import net.syrupstudios.wellseasoned.cooking.FoodEffectResolver;
import net.syrupstudios.wellseasoned.cooking.FoodHealingResolver;
import net.syrupstudios.wellseasoned.cooking.FoodProfile;
import net.syrupstudios.wellseasoned.cooking.ResolvedFoodEffect;

import java.util.ArrayList;
import java.util.List;

public final class FoodTooltip {
    private FoodTooltip() {
    }

    public static int heartQuarters(ItemStack stack) {
        FoodProperties vanillaFood = stack.get(DataComponents.FOOD);
        if (vanillaFood == null) {
            return -1;
        }

        float healing = FoodHealingResolver.resolve(stack, vanillaFood);
        return healing > 0.0F ? Math.round(healing * 2.0F) : -1;
    }

    public static List<Component> effectLines(ItemStack stack, Item.TooltipContext context) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        FoodProfile profile = WellSeasoned.COOKING_DATA.food(itemId).orElse(null);
        FoodProperties food = stack.get(DataComponents.FOOD);
        if (profile == null || food == null) {
            return List.of();
        }
        List<Component> lines = new ArrayList<>();
        addEffects(lines, food, profile, context.tickRate());
        return lines;
    }

    private static void addEffects(
            List<Component> lines,
            FoodProperties food,
            FoodProfile profile,
            float tickRate
    ) {
        for (ResolvedFoodEffect effect : FoodEffectResolver.resolveForTooltip(
                food,
                profile,
            WellSeasoned.COOKING_DATA.snapshot()
        )) {
            BuiltInRegistries.MOB_EFFECT.getHolder(effect.effect()).ifPresent(holder ->
                    lines.add(effectLine(holder, effect, tickRate))
            );
        }
    }

    private static Component effectLine(
            Holder<MobEffect> effect,
            ResolvedFoodEffect resolved,
            float tickRate
    ) {
        int amplifier = resolved.amplifier();
        int duration = resolved.duration();
        MutableComponent name = Component.translatable(effect.value().getDescriptionId());

        if (amplifier > 0) {
            name = Component.translatable(
                    "potion.withAmplifier",
                    name,
                    Component.translatable("potion.potency." + amplifier)
            );
        }
        if (duration > 20) {
            MobEffectInstance instance = new MobEffectInstance(effect, duration, amplifier);
            name = Component.translatable(
                    "potion.withDuration",
                    name,
                    MobEffectUtil.formatDuration(instance, 1.0F, tickRate)
            );
        }
        if (resolved.probability() < 1.0F) {
            int percent = Math.round(resolved.probability() * 100.0F);
            name = name.append(Component.literal(" (" + percent + "%)"));
        }

        return name.withStyle(effect.value().getCategory().getTooltipFormatting());
    }
}

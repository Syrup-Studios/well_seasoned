package net.syrupstudios.wellseasoned.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.player.Player;
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
        FoodProperties vanillaFood = net.syrupstudios.wellseasoned.cooking.FoodCompat.food(stack);
        if (vanillaFood == null) {
            return -1;
        }

        float healing = FoodHealingResolver.resolve(stack, vanillaFood);
        return healing > 0.0F ? Math.round(healing * 2.0F) : -1;
    }

    public static List<Component> effectLines(
            ItemStack stack,
            Player player
            /*? if >=1.20.5 {*/
            , Item.TooltipContext context
            /*?}*/
    ) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        FoodProfile profile = WellSeasoned.COOKING_DATA.food(itemId).orElse(null);
        FoodProperties food = net.syrupstudios.wellseasoned.cooking.FoodCompat.food(stack, player);
        if (profile == null || food == null) {
            return List.of();
        }
        List<Component> lines = new ArrayList<>();
        /*? if >=1.20.5 {*/
        addEffects(lines, food, profile, context.tickRate());
        /*?} else {*/
        /*addEffects(lines, food, profile, 1.0F);
        *//*?}*/
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
            /*? if >=1.20.5 {*/
            BuiltInRegistries.MOB_EFFECT.getHolder(effect.effect()).ifPresent(holder ->
                    lines.add(effectLine(holder, effect, tickRate))
            );
            /*?} else {*/
            /*MobEffect mobEffect = BuiltInRegistries.MOB_EFFECT.get(effect.effect());
            if (mobEffect != null) {
                lines.add(effectLine(mobEffect, effect, tickRate));
            }*/
            /*?}*/
        }
    }

    private static Component effectLine(
            /*? if >=1.20.5 {*/
            net.minecraft.core.Holder<MobEffect> effect,
            /*?} else {*/
            /*MobEffect effect,*/
            /*?}*/
            ResolvedFoodEffect resolved,
            float tickRate
    ) {
        int amplifier = resolved.amplifier();
        int duration = resolved.duration();
        /*? if >=1.20.5 {*/
        MutableComponent name = Component.translatable(effect.value().getDescriptionId());
        /*?} else {*/
        /*MutableComponent name = Component.translatable(effect.getDescriptionId());*/
        /*?}*/

        if (amplifier > 0) {
            name = Component.translatable(
                    "potion.withAmplifier",
                    name,
                    Component.translatable("potion.potency." + amplifier)
            );
        }
        if (duration > 20) {
            MobEffectInstance instance = new MobEffectInstance(
                    /*? if >=1.20.5 {*/
                    effect, duration, amplifier
                    /*?} else {*/
                    /*effect, duration, amplifier*/
                    /*?}*/
            );
            /*? if >=1.20.5 {*/
            name = Component.translatable(
                    "potion.withDuration",
                    name,
                    MobEffectUtil.formatDuration(instance, 1.0F, tickRate)
            );
            /*?} else {*/
            /*name = Component.translatable(
                    "potion.withDuration",
                    name,
                    MobEffectUtil.formatDuration(instance, 1.0F)
            );*/
            /*?}*/
        }
        if (resolved.probability() < 1.0F) {
            int percent = Math.round(resolved.probability() * 100.0F);
            name = name.append(Component.translatable("well_seasoned.food_effect.chance", percent));
        }

        /*? if >=1.20.5 {*/
        return name.withStyle(effect.value().getCategory().getTooltipFormatting());
        /*?} else {*/
        /*return name.withStyle(effect.getCategory().getTooltipFormatting());*/
        /*?}*/
    }
}

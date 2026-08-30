package net.syrupstudios.wellseasoned.cooking;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Collects item and catalog effects and reduces each effect type to one result. */
public final class FoodEffectResolver {
    private static final int DEFAULT_MAXIMUM_DURATION = 20 * 60 * 20;

    private FoodEffectResolver() {
    }

    public static List<ResolvedFoodEffect> resolveForTooltip(
            FoodProperties food,
            FoodProfile profile,
            CookingDataSnapshot snapshot
    ) {
        return resolve(itemEffects(food), configuredEffects(profile, snapshot), profile.effectMode());
    }

    public static List<ResolvedFoodEffect> resolveForConsumption(
            FoodProperties food,
            FoodProfile profile,
            CookingDataSnapshot snapshot,
            RandomSource random
    ) {
        List<ResolvedFoodEffect> selectedItemEffects = itemEffects(food).stream()
                .filter(effect -> random.nextFloat() < effect.probability())
                .map(FoodEffectResolver::certain)
                .toList();
        List<ResolvedFoodEffect> selectedConfiguredEffects = configuredEffects(profile, snapshot).stream()
                .filter(effect -> random.nextFloat() < effect.probability())
                .map(FoodEffectResolver::certain)
                .toList();
        return resolve(selectedItemEffects, selectedConfiguredEffects, profile.effectMode());
    }

    /**
     * Combines duplicate effect types. A stronger amplifier wins. Equal
     * amplifiers add their durations up to the largest supplied cap.
     */
    public static List<ResolvedFoodEffect> resolve(
            Collection<ResolvedFoodEffect> itemEffects,
            Collection<ResolvedFoodEffect> configuredEffects,
            FoodEffectMode mode
    ) {
        Map<ResourceLocation, ResolvedFoodEffect> resolved = new LinkedHashMap<>();
        if (mode == FoodEffectMode.APPEND) {
            itemEffects.forEach(effect -> mergeInto(resolved, effect));
        }
        configuredEffects.forEach(effect -> mergeInto(resolved, effect));
        return List.copyOf(resolved.values());
    }

    private static List<ResolvedFoodEffect> itemEffects(FoodProperties food) {
        List<ResolvedFoodEffect> effects = new ArrayList<>();
        /*? if >=1.20.5 {*/
        for (FoodProperties.PossibleEffect possible : food.effects()) {
            MobEffectInstance instance = possible.effect();
            ResourceLocation effectId = BuiltInRegistries.MOB_EFFECT.getKey(instance.getEffect().value());
            int maximumDuration = instance.isInfiniteDuration()
                    ? MobEffectInstance.INFINITE_DURATION
                    : Math.max(instance.getDuration(), DEFAULT_MAXIMUM_DURATION);
            effects.add(new ResolvedFoodEffect(
                    effectId,
                    instance.getDuration(),
                    instance.getAmplifier(),
                    maximumDuration,
                    instance.isAmbient(),
                    instance.isVisible(),
                    instance.showIcon(),
                    possible.probability()
            ));
        }
        /*?} else {*/
        /*for (com.mojang.datafixers.util.Pair<MobEffectInstance, Float> possible : food.getEffects()) {
            MobEffectInstance instance = possible.getFirst();
            ResourceLocation effectId = BuiltInRegistries.MOB_EFFECT.getKey(instance.getEffect());
            int maximumDuration = instance.isInfiniteDuration()
                    ? MobEffectInstance.INFINITE_DURATION
                    : Math.max(instance.getDuration(), DEFAULT_MAXIMUM_DURATION);
            effects.add(new ResolvedFoodEffect(
                    effectId,
                    instance.getDuration(),
                    instance.getAmplifier(),
                    maximumDuration,
                    instance.isAmbient(),
                    instance.isVisible(),
                    instance.showIcon(),
                    possible.getSecond()
            ));
        }*/
        /*?}*/
        return effects;
    }

    private static List<ResolvedFoodEffect> configuredEffects(
            FoodProfile profile,
            CookingDataSnapshot snapshot
    ) {
        List<ResolvedFoodEffect> effects = new ArrayList<>();
        for (ResourceLocation intrinsicId : profile.intrinsics()) {
            IntrinsicDefinition intrinsic = snapshot.intrinsic(intrinsicId).orElse(null);
            if (intrinsic == null) {
                continue;
            }

            for (IntrinsicDefinition.EffectDefinition definition : intrinsic.effects()) {
                effects.add(new ResolvedFoodEffect(
                        definition.effect(),
                        definition.effectiveDuration(profile.tier()),
                        definition.effectiveAmplifier(profile.tier()),
                        definition.maximumDuration(),
                        definition.ambient(),
                        definition.showParticles(),
                        true,
                        definition.chance()
                ));
            }
        }
        return effects;
    }

    private static void mergeInto(
            Map<ResourceLocation, ResolvedFoodEffect> resolved,
            ResolvedFoodEffect incoming
    ) {
        resolved.merge(incoming.effect(), incoming, FoodEffectResolver::merge);
    }

    private static ResolvedFoodEffect merge(ResolvedFoodEffect current, ResolvedFoodEffect incoming) {
        float probability = combinedProbability(current.probability(), incoming.probability());
        if (incoming.amplifier() > current.amplifier()) {
            return withProbability(incoming, probability);
        }
        if (incoming.amplifier() < current.amplifier()) {
            return withProbability(current, probability);
        }

        int maximumDuration;
        int duration;
        if (current.duration() == MobEffectInstance.INFINITE_DURATION
                || incoming.duration() == MobEffectInstance.INFINITE_DURATION) {
            maximumDuration = MobEffectInstance.INFINITE_DURATION;
            duration = MobEffectInstance.INFINITE_DURATION;
        } else {
            maximumDuration = Math.max(current.maximumDuration(), incoming.maximumDuration());
            long combinedDuration = (long) current.duration() + incoming.duration();
            duration = (int) Math.min(maximumDuration, combinedDuration);
        }
        return new ResolvedFoodEffect(
                current.effect(),
                duration,
                current.amplifier(),
                maximumDuration,
                current.ambient() && incoming.ambient(),
                current.showParticles() || incoming.showParticles(),
                current.showIcon() || incoming.showIcon(),
                probability
        );
    }

    private static float combinedProbability(float first, float second) {
        return 1.0F - (1.0F - first) * (1.0F - second);
    }

    private static ResolvedFoodEffect certain(ResolvedFoodEffect effect) {
        return withProbability(effect, 1.0F);
    }

    private static ResolvedFoodEffect withProbability(ResolvedFoodEffect effect, float probability) {
        return new ResolvedFoodEffect(
                effect.effect(),
                effect.duration(),
                effect.amplifier(),
                effect.maximumDuration(),
                effect.ambient(),
                effect.showParticles(),
                effect.showIcon(),
                probability
        );
    }
}

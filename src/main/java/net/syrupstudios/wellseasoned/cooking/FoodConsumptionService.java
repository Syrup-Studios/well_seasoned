package net.syrupstudios.wellseasoned.cooking;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.syrupstudios.wellseasoned.WellSeasoned;

import java.util.List;
import java.util.Optional;

public final class FoodConsumptionService {
    public static final ResourceLocation CAKE_ITEM = ResourceLocation.withDefaultNamespace("cake");

    private static final FoodProperties NO_EFFECTS_FOOD =
            new FoodProperties(0, 0.0F, false, 1.6F, Optional.empty(), List.of());
    private static final ThreadLocal<Integer> DIRECT_FOOD_DATA_SUPPRESSION_DEPTH =
            ThreadLocal.withInitial(() -> 0);

    private FoodConsumptionService() {
    }

    public static boolean isCake(ResourceLocation item) {
        return CAKE_ITEM.equals(item);
    }

    /** Prevents the raw FoodData hook from duplicating a specialized block hook. */
    public static void beginDirectFoodDataSuppression() {
        DIRECT_FOOD_DATA_SUPPRESSION_DEPTH.set(DIRECT_FOOD_DATA_SUPPRESSION_DEPTH.get() + 1);
    }

    /** Ends a specialized block hook. */
    public static void endDirectFoodDataSuppression() {
        int depth = DIRECT_FOOD_DATA_SUPPRESSION_DEPTH.get() - 1;
        if (depth <= 0) {
            DIRECT_FOOD_DATA_SUPPRESSION_DEPTH.remove();
        } else {
            DIRECT_FOOD_DATA_SUPPRESSION_DEPTH.set(depth);
        }
    }

    public static boolean isDirectFoodDataSuppressed() {
        return DIRECT_FOOD_DATA_SUPPRESSION_DEPTH.get() > 0;
    }

    public static void finishEating(Player player, ItemStack consumedStack, FoodProperties foodProperties) {
        if (player.level().isClientSide() || player.isSpectator()) {
            return;
        }

        Optional<FoodProfile> configured = WellSeasoned.COOKING_DATA.food(
                BuiltInRegistries.ITEM.getKey(consumedStack.getItem())
        );
        if (configured.isPresent()) {
            applyProfile(player, foodProperties, configured.get(), consumedStack);
            return;
        }

        float healing = FoodHealingResolver.resolve(consumedStack, foodProperties);
        if (healing > 0.0F) {
            player.heal(healing);
        }
    }

    /**
     * Applies the cake food profile for one eaten cake slice. Vanilla cake
     * consumption never passes through Player.eat, so the block mixin routes
     * here. Healing and effects match the configured minecraft:cake profile;
     * the cake block still advances its bite count itself.
     */
    public static void finishEatingCake(Player player) {
        if (player.level().isClientSide() || player.isSpectator()) {
            return;
        }

        FoodProfile profile = WellSeasoned.COOKING_DATA.food(CAKE_ITEM).orElse(null);
        if (profile == null) {
            return;
        }

        float healing = profile.effectiveHealing();
        if (healing > 0.0F) {
            player.heal(healing);
        }

        for (ResolvedFoodEffect effect : FoodEffectResolver.resolveForConsumption(
                NO_EFFECTS_FOOD,
                profile,
                WellSeasoned.COOKING_DATA.snapshot(),
                player.getRandom()
        )) {
            BuiltInRegistries.MOB_EFFECT.getHolder(effect.effect()).ifPresent(holder ->
                    mergeEffect(player, holder, effect)
            );
        }
    }

    /** Applies fallback healing for direct FoodData calls with no known item. */
    public static void finishEatingUnprofiled(Player player, int nutrition) {
        if (player.level().isClientSide() || player.isSpectator()) {
            return;
        }

        float healing = FoodHealingResolver.resolveNutrition(nutrition);
        if (healing > 0.0F) {
            player.heal(healing);
        }
    }

    /** Prevents vanilla from applying item effects after the resolver handled them. */
    public static FoodProperties withoutItemEffects(ItemStack stack, FoodProperties foodProperties) {
        if (WellSeasoned.COOKING_DATA.food(BuiltInRegistries.ITEM.getKey(stack.getItem())).isEmpty()) {
            return foodProperties;
        }
        return new FoodProperties(
                foodProperties.nutrition(),
                foodProperties.saturation(),
                foodProperties.canAlwaysEat(),
                foodProperties.eatSeconds(),
                foodProperties.usingConvertsTo(),
                List.of()
        );
    }

    private static void applyProfile(
            Player player,
            FoodProperties foodProperties,
            FoodProfile profile,
            ItemStack consumedStack
    ) {
        float healing = FoodHealingResolver.resolve(consumedStack, foodProperties);
        if (healing > 0.0F) {
            player.heal(healing);
        }

        for (ResolvedFoodEffect effect : FoodEffectResolver.resolveForConsumption(
                foodProperties,
                profile,
                WellSeasoned.COOKING_DATA.snapshot(),
                player.getRandom()
        )) {
            BuiltInRegistries.MOB_EFFECT.getHolder(effect.effect()).ifPresent(holder ->
                    mergeEffect(player, holder, effect)
            );
        }
    }

    private static void mergeEffect(
            Player player,
            Holder<MobEffect> effect,
            ResolvedFoodEffect resolved
    ) {
        MobEffectInstance applied = resolveAppliedEffect(
                effect,
                player.getEffect(effect),
                resolved,
                WellSeasonedConfig.stackingMode(),
                WellSeasonedConfig.stackingStrength()
        );
        if (applied != null) {
            player.addEffect(applied);
        }
    }

    /**
     * Computes the MobEffectInstance to apply for one resolved food effect, or
     * null when nothing should be applied (incoming weaker than the active
     * effect, or the active effect is already infinite). Equal amplifiers stack
     * their durations through the configured diminishing-returns curve; the
     * stronger-effect path replaces the active effect unchanged.
     */
    static MobEffectInstance resolveAppliedEffect(
            Holder<MobEffect> effect,
            MobEffectInstance current,
            ResolvedFoodEffect resolved,
            DurationStacking.StackingMode stackingMode,
            double stackingStrength
    ) {
        if (current == null) {
            return new MobEffectInstance(
                    effect,
                    resolved.duration(),
                    resolved.amplifier(),
                    resolved.ambient(),
                    resolved.showParticles(),
                    resolved.showIcon()
            );
        }
        if (current.getAmplifier() > resolved.amplifier()) {
            return null;
        }

        int grantedDuration = resolved.duration();
        if (current.getAmplifier() == resolved.amplifier()) {
            if (current.isInfiniteDuration()) {
                return null;
            }
            if (grantedDuration != MobEffectInstance.INFINITE_DURATION) {
                grantedDuration = DurationStacking.calculateStackedDuration(
                        current.getDuration(),
                        grantedDuration,
                        resolved.maximumDuration(),
                        stackingStrength,
                        stackingMode
                );
            }
        }

        return new MobEffectInstance(
                effect,
                grantedDuration,
                resolved.amplifier(),
                resolved.ambient(),
                resolved.showParticles(),
                resolved.showIcon()
        );
    }
}

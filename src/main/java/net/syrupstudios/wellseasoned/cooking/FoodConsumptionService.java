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

    private FoodConsumptionService() {
    }

    public static boolean isCake(ResourceLocation item) {
        return CAKE_ITEM.equals(item);
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
        int grantedDuration = resolved.duration();
        int grantedAmplifier = resolved.amplifier();

        MobEffectInstance current = player.getEffect(effect);
        if (current != null) {
            if (current.getAmplifier() > grantedAmplifier) {
                return;
            }
            if (current.getAmplifier() == grantedAmplifier) {
                if (current.isInfiniteDuration()) {
                    return;
                }
                if (grantedDuration != MobEffectInstance.INFINITE_DURATION) {
                    grantedDuration = Math.min(
                            resolved.maximumDuration(),
                            current.getDuration() + Math.max(20, grantedDuration / 2)
                    );
                }
            }
        }

        player.addEffect(new MobEffectInstance(
                effect,
                grantedDuration,
                grantedAmplifier,
                resolved.ambient(),
                resolved.showParticles(),
                resolved.showIcon()
        ));
    }
}

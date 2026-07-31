package net.syrupstudios.wellseasoned.cooking;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.syrupstudios.wellseasoned.WellSeasoned;

import java.util.Optional;

public final class FoodConsumptionService {
    private FoodConsumptionService() {
    }

    public static void finishEating(Player player, ItemStack consumedStack) {
        if (player.level().isClientSide() || player.isSpectator()) {
            return;
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(consumedStack.getItem());
        Optional<FoodProfile> configured = WellSeasoned.COOKING_DATA.food(itemId);
        if (configured.isPresent()) {
            applyProfile(player, configured.get());
            return;
        }

        FoodProperties vanillaFood = consumedStack.get(DataComponents.FOOD);
        int nutrition = vanillaFood == null ? 0 : vanillaFood.nutrition();
        if (nutrition > 0) {
            player.heal(Math.max(1.0F, nutrition * 0.5F));
        }
    }

    private static void applyProfile(Player player, FoodProfile profile) {
        float healing = profile.effectiveHealing();
        if (healing > 0.0F) {
            player.heal(healing);
        }

        for (ResourceLocation intrinsicId : profile.intrinsics()) {
            IntrinsicDefinition intrinsic = WellSeasoned.COOKING_DATA.snapshot().intrinsic(intrinsicId).orElse(null);
            if (intrinsic == null) {
                continue;
            }

            for (IntrinsicDefinition.EffectDefinition definition : intrinsic.effects()) {
                BuiltInRegistries.MOB_EFFECT.getHolder(definition.effect()).ifPresent(holder ->
                        mergeEffect(player, holder, definition, profile.tier())
                );
            }
        }
    }

    private static void mergeEffect(
            Player player,
            Holder<MobEffect> effect,
            IntrinsicDefinition.EffectDefinition definition,
            PreparationTier tier
    ) {
        int grantedDuration = definition.effectiveDuration(tier);
        int grantedAmplifier = definition.effectiveAmplifier(tier);

        MobEffectInstance current = player.getEffect(effect);
        if (current != null) {
            if (current.getAmplifier() > grantedAmplifier) {
                return;
            }
            if (current.getAmplifier() == grantedAmplifier) {
                grantedDuration = Math.min(
                        definition.maximumDuration(),
                        current.getDuration() + Math.max(20, grantedDuration / 2)
                );
            }
        }

        player.addEffect(new MobEffectInstance(
                effect,
                grantedDuration,
                grantedAmplifier,
                definition.ambient(),
                definition.showParticles(),
                true
        ));
    }
}

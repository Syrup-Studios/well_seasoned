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

        /*? if >=1.20.5 {*/
        FoodProperties vanillaFood = consumedStack.getFoodProperties(player);
        int nutrition = vanillaFood == null ? 0 : vanillaFood.nutrition();
        /*?} else {*/
        /*FoodProperties vanillaFood = consumedStack.getItem().getFoodProperties();
        int nutrition = vanillaFood == null ? 0 : vanillaFood.getNutrition();*/
        /*?}*/
        if (nutrition > 0) {
            player.heal(Math.max(1.0F, nutrition * 0.5F));
        }
    }

    private static void applyProfile(Player player, FoodProfile profile) {
        float healing = profile.healing() * profile.tier().healingMultiplier();
        if (healing > 0.0F) {
            player.heal(healing);
        }

        for (ResourceLocation intrinsicId : profile.intrinsics()) {
            IntrinsicDefinition intrinsic = WellSeasoned.COOKING_DATA.snapshot().intrinsic(intrinsicId).orElse(null);
            if (intrinsic == null) {
                continue;
            }

            for (IntrinsicDefinition.EffectDefinition definition : intrinsic.effects()) {
                /*? if >=1.20.5 {*/
                BuiltInRegistries.MOB_EFFECT.getHolder(definition.effect()).ifPresent(holder ->
                        mergeEffect(player, holder, definition, profile.tier())
                );
                /*?} else {*/
                /*MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(definition.effect());
                if (effect != null) {
                    mergeEffect(player, effect, definition, profile.tier());
                }*/
                /*?}*/
            }
        }
    }

    private static void mergeEffect(
            Player player,
            /*? if >=1.20.5 {*/
            Holder<MobEffect> effect,
            /*?} else {*/
            /*MobEffect effect,*/
            /*?}*/
            IntrinsicDefinition.EffectDefinition definition,
            PreparationTier tier
    ) {
        int grantedDuration = Math.min(
                definition.maximumDuration(),
                Math.round(definition.duration() * tier.durationMultiplier())
        );
        int grantedAmplifier = Math.min(
                definition.maximumAmplifier(),
                definition.amplifier() + tier.amplifierBonus()
        );

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

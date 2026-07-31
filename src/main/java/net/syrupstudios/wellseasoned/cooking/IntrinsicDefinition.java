package net.syrupstudios.wellseasoned.cooking;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record IntrinsicDefinition(
        ResourceLocation id,
        List<EffectDefinition> effects
) {
    public IntrinsicDefinition {
        effects = List.copyOf(effects);
    }

    public record EffectDefinition(
            ResourceLocation effect,
            int duration,
            int amplifier,
            int maximumDuration,
            int maximumAmplifier,
            boolean ambient,
            boolean showParticles
    ) {
        public int effectiveDuration(PreparationTier tier) {
            return Math.min(maximumDuration, Math.round(duration * tier.durationMultiplier()));
        }

        public int effectiveAmplifier(PreparationTier tier) {
            return Math.min(maximumAmplifier, amplifier + tier.amplifierBonus());
        }
    }
}

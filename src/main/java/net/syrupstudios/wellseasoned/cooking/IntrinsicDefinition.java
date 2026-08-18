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
            boolean showParticles,
            float chance
    ) {
        public EffectDefinition {
            if (!Float.isFinite(chance) || chance < 0.0F || chance > 1.0F) {
                throw new IllegalArgumentException("Effect chance must be between 0 and 1");
            }
        }

        public int effectiveDuration(PreparationTier tier) {
            return Math.min(maximumDuration, Math.round(duration * tier.durationMultiplier()));
        }

        public int effectiveAmplifier(PreparationTier tier) {
            return Math.min(maximumAmplifier, amplifier + tier.amplifierBonus());
        }
    }
}

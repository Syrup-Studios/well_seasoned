package net.syrupstudios.wellseasoned.cooking;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;

/** One effect after all food-effect sources have been combined. */
public record ResolvedFoodEffect(
        ResourceLocation effect,
        int duration,
        int amplifier,
        int maximumDuration,
        boolean ambient,
        boolean showParticles,
        boolean showIcon,
        float probability
) {
    public ResolvedFoodEffect {
        boolean infinite = duration == MobEffectInstance.INFINITE_DURATION;
        if (!infinite && duration <= 0) {
            throw new IllegalArgumentException("Effect duration must be positive");
        }
        if (amplifier < 0) {
            throw new IllegalArgumentException("Effect amplifier cannot be negative");
        }
        if (infinite && maximumDuration != MobEffectInstance.INFINITE_DURATION) {
            throw new IllegalArgumentException("An infinite effect must have an infinite maximum duration");
        }
        if (!infinite && maximumDuration < duration) {
            throw new IllegalArgumentException("Maximum duration cannot be less than duration");
        }
        if (!Float.isFinite(probability) || probability < 0.0F || probability > 1.0F) {
            throw new IllegalArgumentException("Effect probability must be between 0 and 1");
        }
    }
}

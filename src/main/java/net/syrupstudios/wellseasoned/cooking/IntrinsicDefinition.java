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
    }
}

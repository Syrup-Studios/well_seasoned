package net.syrupstudios.wellseasoned.cooking;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FoodEffectResolverTest {
    private static final FoodProperties EMPTY_FOOD =
            new FoodProperties(0, 0.0F, false, 1.6F, Optional.empty(), List.of());

    @BeforeAll
    static void bootStrap() {
        net.minecraft.SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void appendKeepsItemAndConfiguredEffects() {
        List<ResolvedFoodEffect> resolved = FoodEffectResolver.resolve(
                List.of(effect("speed", 100, 0, 1_000, 1.0F)),
                List.of(effect("resistance", 200, 0, 1_000, 1.0F)),
                FoodEffectMode.APPEND
        );

        assertEquals(List.of(id("speed"), id("resistance")), resolved.stream().map(ResolvedFoodEffect::effect).toList());
    }

    @Test
    void replaceRemovesItemEffects() {
        List<ResolvedFoodEffect> resolved = FoodEffectResolver.resolve(
                List.of(effect("speed", 100, 0, 1_000, 1.0F)),
                List.of(effect("resistance", 200, 0, 1_000, 1.0F)),
                FoodEffectMode.REPLACE
        );

        assertEquals(List.of(id("resistance")), resolved.stream().map(ResolvedFoodEffect::effect).toList());
    }

    @Test
    void equalAmplifiersAddDurationUpToLargestCap() {
        ResolvedFoodEffect resolved = FoodEffectResolver.resolve(
                List.of(effect("speed", 300, 1, 600, 1.0F)),
                List.of(effect("speed", 500, 1, 700, 1.0F)),
                FoodEffectMode.APPEND
        ).getFirst();

        assertEquals(700, resolved.duration());
        assertEquals(700, resolved.maximumDuration());
        assertEquals(1, resolved.amplifier());
    }

    @Test
    void strongerAmplifierWins() {
        ResolvedFoodEffect resolved = FoodEffectResolver.resolve(
                List.of(effect("speed", 600, 0, 1_000, 1.0F)),
                List.of(effect("speed", 200, 2, 400, 1.0F)),
                FoodEffectMode.APPEND
        ).getFirst();

        assertEquals(200, resolved.duration());
        assertEquals(2, resolved.amplifier());
    }

    @Test
    void duplicateProbabilitiesAreCombined() {
        ResolvedFoodEffect resolved = FoodEffectResolver.resolve(
                List.of(effect("speed", 100, 0, 1_000, 0.25F)),
                List.of(effect("speed", 100, 0, 1_000, 0.5F)),
                FoodEffectMode.APPEND
        ).getFirst();

        assertEquals(0.625F, resolved.probability(), 0.0001F);
    }

    @Test
    void configuredChanceThirtyPercentAppliesWhenRollSucceeds() {
        CookingDataSnapshot snapshot = chanceSnapshot(0.3F);
        FoodProfile profile = profile(snapshot);
        List<ResolvedFoodEffect> effects = FoodEffectResolver.resolveForConsumption(
                EMPTY_FOOD,
                profile,
                snapshot,
                new FixedRandom(0.1F)
        );

        assertEquals(1, effects.size());
        assertEquals(id("nausea"), effects.getFirst().effect());
        assertEquals(1.0F, effects.getFirst().probability(), 0.0001F);
    }

    @Test
    void configuredChanceThirtyPercentAppliesWhenRollFails() {
        CookingDataSnapshot snapshot = chanceSnapshot(0.3F);
        List<ResolvedFoodEffect> effects = FoodEffectResolver.resolveForConsumption(
                EMPTY_FOOD,
                profile(snapshot),
                snapshot,
                new FixedRandom(0.9F)
        );

        assertTrue(effects.isEmpty());
    }

    @Test
    void configuredChanceZeroNeverApplies() {
        CookingDataSnapshot snapshot = chanceSnapshot(0.0F);
        List<ResolvedFoodEffect> effects = FoodEffectResolver.resolveForConsumption(
                EMPTY_FOOD,
                profile(snapshot),
                snapshot,
                new FixedRandom(0.0001F)
        );

        assertTrue(effects.isEmpty());
    }

    @Test
    void configuredChanceOneAlwaysApplies() {
        CookingDataSnapshot snapshot = chanceSnapshot(1.0F);
        List<ResolvedFoodEffect> effects = FoodEffectResolver.resolveForConsumption(
                EMPTY_FOOD,
                profile(snapshot),
                snapshot,
                new FixedRandom(0.999F)
        );

        assertEquals(1, effects.size());
    }

    @Test
    void everyConsumptionIsAnIndependentRoll() {
        CookingDataSnapshot snapshot = chanceSnapshot(0.3F);
        FoodProfile profile = profile(snapshot);

        List<ResolvedFoodEffect> success = FoodEffectResolver.resolveForConsumption(
                EMPTY_FOOD, profile, snapshot, new FixedRandom(0.1F)
        );
        List<ResolvedFoodEffect> failure = FoodEffectResolver.resolveForConsumption(
                EMPTY_FOOD, profile, snapshot, new FixedRandom(0.9F)
        );

        assertEquals(1, success.size());
        assertTrue(failure.isEmpty());
    }

    @Test
    void failedConfiguredRollDoesNotExtendActiveEffectDurations() {
        CookingDataSnapshot snapshot = chanceSnapshot(0.3F);
        FoodProfile profile = profile(snapshot);
        List<ResolvedFoodEffect> resolved = FoodEffectResolver.resolveForConsumption(
                EMPTY_FOOD, profile, snapshot, new FixedRandom(0.9F)
        );

        assertTrue(resolved.isEmpty());
    }

    private static CookingDataSnapshot chanceSnapshot(float chance) {
        IntrinsicDefinition intrinsic = new IntrinsicDefinition(id("chance_nausea"), List.of(
                new IntrinsicDefinition.EffectDefinition(
                        id("nausea"),
                        100,
                        0,
                        100,
                        2,
                        false,
                        true,
                        chance
                )
        ));
        FoodProfile foodProfile = new FoodProfile(
                id("chicken"),
                PreparationTier.SIMPLE,
                1,
                FoodEffectMode.REPLACE,
                List.of(id("chance_nausea"))
        );
        return new CookingDataSnapshot(
                Map.of(id("chance_nausea"), intrinsic),
                Map.of(id("chicken"), foodProfile)
        );
    }

    private static FoodProfile profile(CookingDataSnapshot snapshot) {
        return snapshot.foods().get(id("chicken"));
    }

    private static ResolvedFoodEffect effect(
            String effect,
            int duration,
            int amplifier,
            int maximumDuration,
            float probability
    ) {
        return new ResolvedFoodEffect(
                id(effect),
                duration,
                amplifier,
                maximumDuration,
                false,
                true,
                true,
                probability
        );
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("test", path);
    }

    /** Deterministic RandomSource returning a scripted nextFloat sequence. */
    private static final class FixedRandom implements RandomSource {
        private final float[] values;
        private int index;

        private FixedRandom(float... values) {
            this.values = values;
        }

        @Override
        public RandomSource fork() {
            return this;
        }

        @Override
        public net.minecraft.world.level.levelgen.PositionalRandomFactory forkPositional() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setSeed(long seed) {
            index = 0;
        }

        @Override
        public int nextInt() {
            return 0;
        }

        @Override
        public int nextInt(int bound) {
            return 0;
        }

        @Override
        public long nextLong() {
            return 0L;
        }

        @Override
        public boolean nextBoolean() {
            return false;
        }

        @Override
        public float nextFloat() {
            return values[Math.min(index++, values.length - 1)];
        }

        @Override
        public double nextDouble() {
            return 0.0;
        }

        @Override
        public double nextGaussian() {
            return 0.0;
        }
    }
}

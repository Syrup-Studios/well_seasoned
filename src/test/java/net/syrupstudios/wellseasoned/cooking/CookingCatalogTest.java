package net.syrupstudios.wellseasoned.cooking;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.syrupstudios.wellseasoned.WellSeasoned;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CookingCatalogTest extends CookingDataTestSupport {
    private static final FoodProperties EMPTY_FOOD =
            new FoodProperties(0, 0.0F, false, 1.6F, Optional.empty(), List.of());

    @Test
    void bundledCatalogDefinesEveryVanillaFood() {
        for (String id : List.of(
                "potato", "beetroot", "carrot", "apple", "melon_slice", "sweet_berries",
                "glow_berries", "chorus_fruit", "dried_kelp", "poisonous_potato", "spider_eye",
                "pufferfish", "rotten_flesh", "beef", "porkchop", "chicken", "mutton", "rabbit",
                "cod", "salmon", "tropical_fish", "cooked_beef", "cooked_porkchop",
                "cooked_chicken", "cooked_mutton", "cooked_rabbit", "cooked_cod", "cooked_salmon",
                "baked_potato", "bread", "cookie", "cake", "honey_bottle", "mushroom_stew",
                "beetroot_soup", "suspicious_stew", "rabbit_stew", "pumpkin_pie", "golden_apple",
                "enchanted_golden_apple", "golden_carrot"
        )) {
            assertTrue(
                    WellSeasoned.COOKING_DATA.food(item(id)).isPresent(),
                    "expected a default profile for minecraft:" + id
            );
        }
    }

    @Test
    void healingValuesMatchTheTable() {
        assertHealing("potato", 1);
        assertHealing("beetroot", 1);
        assertHealing("carrot", 2);
        assertHealing("apple", 2);
        assertHealing("melon_slice", 1);
        assertHealing("sweet_berries", 1);
        assertHealing("glow_berries", 1);
        assertHealing("chorus_fruit", 2);
        assertHealing("dried_kelp", 1);
        assertHealing("poisonous_potato", 1);
        assertHealing("spider_eye", 1);
        assertHealing("pufferfish", 1);
        assertHealing("rotten_flesh", 2);
        assertHealing("chicken", 1);
        assertHealing("beef", 1);
        assertHealing("porkchop", 1);
        assertHealing("mutton", 1);
        assertHealing("rabbit", 1);
        assertHealing("cod", 1);
        assertHealing("salmon", 1);
        assertHealing("tropical_fish", 1);
        assertHealing("cooked_beef", 4);
        assertHealing("cooked_porkchop", 4);
        assertHealing("cooked_chicken", 3);
        assertHealing("cooked_mutton", 3);
        assertHealing("cooked_rabbit", 3);
        assertHealing("cooked_cod", 3);
        assertHealing("cooked_salmon", 3);
        assertHealing("baked_potato", 3);
        assertHealing("bread", 3);
        assertHealing("cookie", 1);
        assertHealing("cake", 1);
        assertHealing("honey_bottle", 3);
        assertHealing("mushroom_stew", 3);
        assertHealing("beetroot_soup", 3);
        assertHealing("suspicious_stew", 3);
        assertHealing("rabbit_stew", 5);
        assertHealing("pumpkin_pie", 4);
        assertHealing("golden_apple", 4);
        assertHealing("enchanted_golden_apple", 4);
        assertHealing("golden_carrot", 3);
    }

    @Test
    void guaranteedEffectsMatchTheTable() {
        assertSingleEffect("apple", "regeneration", 100, 0);
        assertSingleEffect("melon_slice", "speed", 100, 0);
        assertSingleEffect("sweet_berries", "speed", 100, 0);
        assertSingleEffect("glow_berries", "speed", 100, 0);
        assertSingleEffect("chorus_fruit", "slow_falling", 100, 0);
        assertSingleEffect("dried_kelp", "water_breathing", 200, 0);
        assertSingleEffect("rotten_flesh", "nausea", 100, 0);
        assertSingleEffect("cooked_rabbit", "jump_boost", 300, 0);
        assertSingleEffect("cooked_salmon", "water_breathing", 300, 0);
        assertSingleEffect("cookie", "speed", 200, 0);
        assertSingleEffect("honey_bottle", "regeneration", 200, 0);
        assertSingleEffect("rabbit_stew", "jump_boost", 600, 0);
        assertSingleEffect("pumpkin_pie", "absorption", 400, 0);
        assertSingleEffect("golden_carrot", "night_vision", 600, 0);
    }

    @Test
    void effectLessFoodsGainNoStatusEffects() {
        for (String id : List.of(
                "potato", "beef", "cod", "bread", "cooked_beef", "baked_potato", "mushroom_stew"
        )) {
            assertTrue(tooltipEffects(id).isEmpty(), "expected no effects for minecraft:" + id);
        }
    }

    @Test
    void poisonousPotatoAndSpiderEyeUseVanillaPoisonDuration() {
        assertSingleEffect("poisonous_potato", "poison", 100, 0);
        assertSingleEffect("spider_eye", "poison", 100, 0);
    }

    @Test
    void pufferfishUsesVanillaPoisonDurationWithAmplifierFour() {
        assertSingleEffect("pufferfish", "poison", 1200, 3);
    }

    @Test
    void rottenFleshDropsVanillaHunger() {
        List<ResolvedFoodEffect> effects = tooltipEffects("rotten_flesh");
        assertEquals(List.of(effect("nausea")), effects.stream().map(ResolvedFoodEffect::effect).toList());
    }

    @Test
    void rawChickenDropsVanillaHungerAndCarriesThirtyPercentNausea() {
        List<ResolvedFoodEffect> effects = tooltipEffects("chicken");
        assertEquals(List.of(effect("nausea")), effects.stream().map(ResolvedFoodEffect::effect).toList());
        assertEquals(0.3F, effects.getFirst().probability(), 0.0001F);
        assertEquals(100, effects.getFirst().duration());
        assertEquals(0, effects.getFirst().amplifier());
    }

    @Test
    void pufferfishDropsVanillaHungerAndNausea() {
        List<ResolvedFoodEffect> effects = tooltipEffects("pufferfish");
        assertEquals(List.of(effect("poison")), effects.stream().map(ResolvedFoodEffect::effect).toList());
    }

    @Test
    void goldenAppleKeepsVanillaEffectsAndHealsFour() {
        assertHealing("golden_apple", 4);
        List<ResolvedFoodEffect> effects = tooltipEffects("golden_apple");
        assertEquals(2, effects.size());
        ResolvedFoodEffect regeneration = effects.stream()
                .filter(effect -> effect.effect().equals(effect("regeneration")))
                .findFirst().orElseThrow();
        assertEquals(100, regeneration.duration());
        assertEquals(1, regeneration.amplifier());
        ResolvedFoodEffect absorption = effects.stream()
                .filter(effect -> effect.effect().equals(effect("absorption")))
                .findFirst().orElseThrow();
        assertEquals(2400, absorption.duration());
        assertEquals(0, absorption.amplifier());
    }

    @Test
    void enchantedGoldenAppleKeepsVanillaEffectsAndHealsFour() {
        assertHealing("enchanted_golden_apple", 4);
        List<ResolvedFoodEffect> effects = tooltipEffects("enchanted_golden_apple");
        assertEquals(4, effects.size());
        assertEquals(effect("regeneration"), effectOf(effects, "regeneration").effect());
        assertEquals(400, effectOf(effects, "regeneration").duration());
        assertEquals(1, effectOf(effects, "regeneration").amplifier());
        assertEquals(effect("resistance"), effectOf(effects, "resistance").effect());
        assertEquals(6000, effectOf(effects, "resistance").duration());
        assertEquals(effect("fire_resistance"), effectOf(effects, "fire_resistance").effect());
        assertEquals(6000, effectOf(effects, "fire_resistance").duration());
        assertEquals(effect("absorption"), effectOf(effects, "absorption").effect());
        assertEquals(2400, effectOf(effects, "absorption").duration());
        assertEquals(3, effectOf(effects, "absorption").amplifier());
    }

    @Test
    void suspiciousStewHealsThreeAndAddsNoFixedEffect() {
        assertHealing("suspicious_stew", 3);
        assertTrue(tooltipEffects("suspicious_stew").isEmpty());
    }

    @Test
    void cakeSlicesHealOneAndGrantSpeedThroughBlockConsumption() {
        assertHealing("cake", 1);
        List<ResolvedFoodEffect> effects = tooltipEffects("cake", EMPTY_FOOD);
        assertEquals(List.of(effect("speed")), effects.stream().map(ResolvedFoodEffect::effect).toList());
        assertEquals(200, effects.getFirst().duration());
        assertEquals(0, effects.getFirst().amplifier());
    }

    @Test
    void cakeItemIsAcceptedAsAConfiguredFood() {
        assertTrue(WellSeasoned.COOKING_DATA.food(item("cake")).isPresent());
        assertFalse(stack("cake").has(DataComponents.FOOD));
    }

    @Test
    void allDefaultFoodsUseTheSimpleTierSoTableValuesAreExact() {
        for (FoodProfile foodProfile : WellSeasoned.COOKING_DATA.snapshot().foods().values()) {
            assertEquals(
                    PreparationTier.SIMPLE,
                    foodProfile.tier(),
                    foodProfile.item() + " must use the simple tier so the table healing is exact"
            );
        }
    }

    @Test
    void invalidCatalogRejectsTheWholeSetAndKeepsLastValidData() {
        com.google.gson.JsonObject invalid = new com.google.gson.JsonObject();
        com.google.gson.JsonArray foods = new com.google.gson.JsonArray();
        com.google.gson.JsonObject apple = new com.google.gson.JsonObject();
        apple.addProperty("item", "minecraft:apple");
        apple.addProperty("tier", "simple");
        apple.addProperty("healing", 2);
        apple.addProperty("mode", "replace");
        com.google.gson.JsonArray intrinsics = new com.google.gson.JsonArray();
        intrinsics.add("well_seasoned:missing_intrinsic");
        apple.add("intrinsics", intrinsics);
        foods.add(apple);
        invalid.add("foods", foods);

        var resources = new java.util.HashMap<ResourceLocation, com.google.gson.JsonElement>(DEFAULT_RESOURCES);
        resources.put(WellSeasoned.id("broken"), invalid);

        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> CookingReloadListener.parseDefinitions(resources)
        );

        assertTrue(
                WellSeasoned.COOKING_DATA.food(item("apple")).isPresent(),
                "a failed reload must keep the last valid catalog set"
        );
    }

    private static void assertHealing(String itemId, int expected) {
        assertEquals(
                expected,
                FoodHealingResolver.resolve(stack(itemId)),
                0.001F,
                "healing for minecraft:" + itemId
        );
    }

    private static void assertSingleEffect(String itemId, String effectId, int duration, int amplifier) {
        List<ResolvedFoodEffect> effects = tooltipEffects(itemId);
        assertEquals(
                List.of(effect(effectId)),
                effects.stream().map(ResolvedFoodEffect::effect).toList(),
                "effects for minecraft:" + itemId
        );
        ResolvedFoodEffect effect = effects.getFirst();
        assertEquals(duration, effect.duration(), "duration for minecraft:" + itemId);
        assertEquals(amplifier, effect.amplifier(), "amplifier for minecraft:" + itemId);
    }

    private static ResolvedFoodEffect effectOf(List<ResolvedFoodEffect> effects, String effectId) {
        return effects.stream()
                .filter(effect -> effect.effect().equals(effect(effectId)))
                .findFirst().orElseThrow();
    }

    private static ResourceLocation effect(String effectId) {
        return ResourceLocation.withDefaultNamespace(effectId);
    }
}

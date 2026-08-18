package net.syrupstudios.wellseasoned.cooking;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.syrupstudios.wellseasoned.WellSeasoned;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChanceCodecTest {
    private static final String INTRINSIC = """
            {
              "id": "well_seasoned:test_effect",
              "effects": [
                {
                  "id": "minecraft:nausea",
                  "duration": 100,
                  "amplifier": 0%s
                }
              ]
            }
            """;

    @BeforeAll
    static void bootStrap() {
        net.minecraft.SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void missingChanceDefaultsToOne() {
        IntrinsicDefinition.EffectDefinition effect = firstEffect(parseCatalog(chanceSuffix("")));
        assertEquals(1.0F, effect.chance(), 0.0001F);
    }

    @Test
    void chanceZeroIsAccepted() {
        IntrinsicDefinition.EffectDefinition effect = firstEffect(parseCatalog(chanceSuffix(",\n          \"chance\": 0.0")));
        assertEquals(0.0F, effect.chance(), 0.0001F);
    }

    @Test
    void chanceOneIsAccepted() {
        IntrinsicDefinition.EffectDefinition effect = firstEffect(parseCatalog(chanceSuffix(",\n          \"chance\": 1.0")));
        assertEquals(1.0F, effect.chance(), 0.0001F);
    }

    @Test
    void chancePointThreeIsAccepted() {
        IntrinsicDefinition.EffectDefinition effect = firstEffect(parseCatalog(chanceSuffix(",\n          \"chance\": 0.3")));
        assertEquals(0.3F, effect.chance(), 0.0001F);
    }

    @Test
    void chanceBelowZeroFailsValidation() {
        assertThrows(
                RuntimeException.class,
                () -> parseCatalog(chanceSuffix(",\n          \"chance\": -0.1"))
        );
    }

    @Test
    void chanceAboveOneFailsValidation() {
        assertThrows(
                RuntimeException.class,
                () -> parseCatalog(chanceSuffix(",\n          \"chance\": 1.1"))
        );
    }

    @Test
    void chanceNaNIsRejected() {
        assertThrows(
                RuntimeException.class,
                () -> parseCatalog(effectWithNumberChance(Float.NaN))
        );
    }

    @Test
    void chanceInfinityIsRejected() {
        assertThrows(
                RuntimeException.class,
                () -> parseCatalog(effectWithNumberChance(Float.POSITIVE_INFINITY))
        );
    }

    @Test
    void chanceNegativeInfinityIsRejected() {
        assertThrows(
                RuntimeException.class,
                () -> parseCatalog(effectWithNumberChance(Float.NEGATIVE_INFINITY))
        );
    }

    private static String chanceSuffix(String suffix) {
        return "{\n  \"intrinsics\": [\n" + INTRINSIC.formatted(suffix) + "\n  ]\n}";
    }

    private static JsonElement effectWithNumberChance(float chance) {
        JsonObject effect = new JsonObject();
        effect.addProperty("id", "minecraft:nausea");
        effect.addProperty("duration", 100);
        effect.addProperty("amplifier", 0);
        effect.add("chance", new JsonPrimitive(chance));

        JsonObject intrinsic = new JsonObject();
        intrinsic.addProperty("id", "well_seasoned:test_effect");
        com.google.gson.JsonArray effects = new com.google.gson.JsonArray();
        effects.add(effect);
        intrinsic.add("effects", effects);

        JsonObject catalog = new JsonObject();
        com.google.gson.JsonArray intrinsics = new com.google.gson.JsonArray();
        intrinsics.add(intrinsic);
        catalog.add("intrinsics", intrinsics);
        return catalog;
    }

    private static CookingReloadListener.CookingDataDefinitions parseCatalog(String catalogJson) {
        return parseCatalog(JsonParser.parseString(catalogJson));
    }

    private static CookingReloadListener.CookingDataDefinitions parseCatalog(JsonElement catalog) {
        return CookingReloadListener.parseDefinitions(Map.of(WellSeasoned.id("test"), catalog));
    }

    private static IntrinsicDefinition.EffectDefinition firstEffect(
            CookingReloadListener.CookingDataDefinitions definitions
    ) {
        return definitions.intrinsics().get(WellSeasoned.id("test_effect"))
                .effects()
                .getFirst();
    }
}

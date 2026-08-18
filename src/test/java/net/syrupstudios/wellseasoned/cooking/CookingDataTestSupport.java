package net.syrupstudios.wellseasoned.cooking;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.syrupstudios.wellseasoned.WellSeasoned;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Shared test bootstrap: starts the Minecraft registries once, then loads the
 * bundled default vanilla catalog through the same validation path the reload
 * listener uses and installs it into the shared cooking data manager.
 */
public abstract class CookingDataTestSupport {
    public static final String DEFAULT_CATALOG =
            "data/well_seasoned/well_seasoned/cooking/vanilla_foods.json";

    public static final Map<ResourceLocation, JsonElement> DEFAULT_RESOURCES = loadDefaultResources();

    @BeforeAll
    public static void bootStrapCookingData() throws IOException {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        CookingReloadListener.CookingDataDefinitions definitions = CookingReloadListener.parseDefinitions(
                DEFAULT_RESOURCES
        );
        Map<ResourceLocation, FoodProfile> foods = CookingReloadListener.expandTags(
                definitions.itemFoods(),
                definitions.taggedFoods()
        );
        WellSeasoned.COOKING_DATA.replace(new CookingDataSnapshot(definitions.intrinsics(), foods));
    }

    private static Map<ResourceLocation, JsonElement> loadDefaultResources() {
        try (Reader reader = new InputStreamReader(
                CookingDataTestSupport.class.getClassLoader().getResourceAsStream(DEFAULT_CATALOG),
                StandardCharsets.UTF_8
        )) {
            return Map.of(WellSeasoned.id("vanilla_foods"), JsonParser.parseReader(reader));
        } catch (IOException exception) {
            throw new RuntimeException("Could not load " + DEFAULT_CATALOG, exception);
        }
    }

    public static ResourceLocation item(String id) {
        return ResourceLocation.withDefaultNamespace(id);
    }

    public static FoodProfile profile(String itemId) {
        return WellSeasoned.COOKING_DATA.food(item(itemId)).orElseThrow();
    }

    public static ItemStack stack(String itemId) {
        return new ItemStack(BuiltInRegistries.ITEM.get(item(itemId)));
    }

    public static FoodProperties food(ItemStack stack) {
        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food == null) {
            throw new AssertionError(stack + " has no food component");
        }
        return food;
    }

    public static List<ResolvedFoodEffect> tooltipEffects(String itemId) {
        ItemStack stack = stack(itemId);
        return FoodEffectResolver.resolveForTooltip(food(stack), profile(itemId), WellSeasoned.COOKING_DATA.snapshot());
    }

    public static List<ResolvedFoodEffect> tooltipEffects(
            String itemId,
            FoodProperties foodProperties
    ) {
        return FoodEffectResolver.resolveForTooltip(
                foodProperties,
                profile(itemId),
                WellSeasoned.COOKING_DATA.snapshot()
        );
    }
}

package net.syrupstudios.wellseasoned.cooking;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.syrupstudios.wellseasoned.WellSeasoned;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads all cooking definitions as one unit. A malformed resource prevents the
 * partial snapshot from replacing the last known-good data.
 */
public final class CookingReloadListener extends SimplePreparableReloadListener<CookingDataSnapshot> {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final String DIRECTORY = "well_seasoned/cooking";

    private final CookingDataManager manager;

    public CookingReloadListener(CookingDataManager manager) {
        this.manager = manager;
    }

    @Override
    protected CookingDataSnapshot prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, JsonElement> resources = new HashMap<>();
        SimpleJsonResourceReloadListener.scanDirectory(resourceManager, DIRECTORY, GSON, resources);

        Map<ResourceLocation, IntrinsicDefinition> intrinsics = new HashMap<>();
        Map<ResourceLocation, FoodProfile> foods = new HashMap<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : resources.entrySet()) {
            try {
                parseCatalog(entry.getKey(), GsonHelper.convertToJsonObject(entry.getValue(), "catalog"), intrinsics, foods);
            } catch (RuntimeException exception) {
                throw new JsonParseException("Invalid cooking catalog " + entry.getKey(), exception);
            }
        }

        for (FoodProfile food : foods.values()) {
            for (ResourceLocation intrinsic : food.intrinsics()) {
                if (!intrinsics.containsKey(intrinsic)) {
                    throw new JsonParseException("Food " + food.item() + " references missing intrinsic " + intrinsic);
                }
            }
        }

        return new CookingDataSnapshot(intrinsics, foods);
    }

    private static void parseCatalog(
            ResourceLocation resource,
            JsonObject root,
            Map<ResourceLocation, IntrinsicDefinition> intrinsics,
            Map<ResourceLocation, FoodProfile> foods
    ) {
        JsonArray intrinsicArray = GsonHelper.getAsJsonArray(root, "intrinsics", new JsonArray());
        for (JsonElement element : intrinsicArray) {
            IntrinsicDefinition definition = parseIntrinsic(GsonHelper.convertToJsonObject(element, "intrinsic"));
            if (intrinsics.putIfAbsent(definition.id(), definition) != null) {
                throw new JsonParseException("Duplicate intrinsic " + definition.id() + " in " + resource);
            }
        }

        JsonArray foodArray = GsonHelper.getAsJsonArray(root, "foods", new JsonArray());
        for (JsonElement element : foodArray) {
            FoodProfile profile = parseFood(GsonHelper.convertToJsonObject(element, "food"));
            if (foods.putIfAbsent(profile.item(), profile) != null) {
                throw new JsonParseException("Duplicate food " + profile.item() + " in " + resource);
            }
        }
    }

    private static IntrinsicDefinition parseIntrinsic(JsonObject json) {
        /*? if >=1.20.5 {*/
        ResourceLocation id = ResourceLocation.parse(GsonHelper.getAsString(json, "id"));
        /*?} else {*/
        /*ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(json, "id"));*/
        /*?}*/
        String translation = GsonHelper.getAsString(json, "translation", "intrinsic." + id.getNamespace() + "." + id.getPath());
        int color = parseColor(GsonHelper.getAsString(json, "color", "#FFFFFF"));
        JsonArray effectArray = GsonHelper.getAsJsonArray(json, "effects");
        var effects = new java.util.ArrayList<IntrinsicDefinition.EffectDefinition>();

        for (JsonElement element : effectArray) {
            JsonObject effect = GsonHelper.convertToJsonObject(element, "effect");
            effects.add(new IntrinsicDefinition.EffectDefinition(
                    /*? if >=1.20.5 {*/
                    ResourceLocation.parse(GsonHelper.getAsString(effect, "id")),
                    /*?} else {*/
                    /*new ResourceLocation(GsonHelper.getAsString(effect, "id")),*/
                    /*?}*/
                    positiveInt(effect, "duration", 1),
                    nonNegativeInt(effect, "amplifier", 0),
                    positiveInt(effect, "maximum_duration", 20 * 60 * 20),
                    nonNegativeInt(effect, "maximum_amplifier", 2),
                    GsonHelper.getAsBoolean(effect, "ambient", false),
                    GsonHelper.getAsBoolean(effect, "show_particles", true)
            ));
        }

        if (effects.isEmpty()) {
            throw new JsonParseException("Intrinsic " + id + " must define at least one effect");
        }
        return new IntrinsicDefinition(id, translation, color, effects);
    }

    private static FoodProfile parseFood(JsonObject json) {
        /*? if >=1.20.5 {*/
        ResourceLocation item = ResourceLocation.parse(GsonHelper.getAsString(json, "item"));
        /*?} else {*/
        /*ResourceLocation item = new ResourceLocation(GsonHelper.getAsString(json, "item"));*/
        /*?}*/
        PreparationTier tier;
        try {
            tier = PreparationTier.parse(GsonHelper.getAsString(json, "tier"));
        } catch (IllegalArgumentException exception) {
            throw new JsonParseException("Unknown preparation tier for " + item, exception);
        }

        float healing = GsonHelper.getAsFloat(json, "healing");
        if (healing < 0.0F || healing > 40.0F) {
            throw new JsonParseException("Healing for " + item + " must be between 0 and 40");
        }

        JsonArray intrinsicArray = GsonHelper.getAsJsonArray(json, "intrinsics", new JsonArray());
        var intrinsicIds = new java.util.ArrayList<ResourceLocation>();
        for (JsonElement element : intrinsicArray) {
            /*? if >=1.20.5 {*/
            intrinsicIds.add(ResourceLocation.parse(GsonHelper.convertToString(element, "intrinsic")));
            /*?} else {*/
            /*intrinsicIds.add(new ResourceLocation(GsonHelper.convertToString(element, "intrinsic")));*/
            /*?}*/
        }
        if (intrinsicIds.isEmpty()) {
            throw new JsonParseException("Food " + item + " must reference at least one intrinsic");
        }

        return new FoodProfile(item, tier, healing, intrinsicIds);
    }

    private static int positiveInt(JsonObject json, String key, int fallback) {
        int value = GsonHelper.getAsInt(json, key, fallback);
        if (value <= 0) {
            throw new JsonParseException(key + " must be positive");
        }
        return value;
    }

    private static int nonNegativeInt(JsonObject json, String key, int fallback) {
        int value = GsonHelper.getAsInt(json, key, fallback);
        if (value < 0) {
            throw new JsonParseException(key + " cannot be negative");
        }
        return value;
    }

    private static int parseColor(String value) {
        String normalized = value.startsWith("#") ? value.substring(1) : value;
        if (normalized.length() != 6) {
            throw new JsonParseException("Color must be a six-digit RGB value");
        }
        try {
            return Integer.parseInt(normalized, 16);
        } catch (NumberFormatException exception) {
            throw new JsonParseException("Invalid RGB color " + value, exception);
        }
    }

    @Override
    protected void apply(CookingDataSnapshot snapshot, ResourceManager resourceManager, ProfilerFiller profiler) {
        manager.replace(snapshot);
        WellSeasoned.LOGGER.info(
                "Loaded {} Well Seasoned intrinsics and {} food profiles",
                snapshot.intrinsics().size(),
                snapshot.foods().size()
        );
    }
}

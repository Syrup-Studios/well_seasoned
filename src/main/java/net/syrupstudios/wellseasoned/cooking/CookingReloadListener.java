package net.syrupstudios.wellseasoned.cooking;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
//? if fabric
/*import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;*/
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.syrupstudios.wellseasoned.WellSeasoned;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads all cooking definitions as one unit. A malformed resource prevents the
 * partial snapshot from replacing the last known-good data.
 */
public final class CookingReloadListener extends SimplePreparableReloadListener<CookingReloadListener.CookingDataDefinitions>
        //? if fabric
        /*implements IdentifiableResourceReloadListener*/
{
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final String DIRECTORY = "well_seasoned/cooking";

    private final CookingDataManager manager;

    public CookingReloadListener(CookingDataManager manager) {
        this.manager = manager;
    }

    @Override
    protected CookingDataDefinitions prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, JsonElement> resources = new HashMap<>();
        SimpleJsonResourceReloadListener.scanDirectory(resourceManager, DIRECTORY, GSON, resources);
        return parseDefinitions(resources);
    }

    static CookingDataDefinitions parseDefinitions(Map<ResourceLocation, JsonElement> resources) {
        Map<ResourceLocation, IntrinsicDefinition> intrinsics = new HashMap<>();
        Map<ResourceLocation, FoodProfile> itemFoods = new HashMap<>();
        Map<ResourceLocation, TaggedFoodProfile> taggedFoods = new HashMap<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : resources.entrySet()) {
            try {
                parseCatalog(
                        entry.getKey(),
                        GsonHelper.convertToJsonObject(entry.getValue(), "catalog"),
                        intrinsics,
                        itemFoods,
                        taggedFoods
                );
            } catch (RuntimeException exception) {
                throw new JsonParseException("Invalid cooking catalog " + entry.getKey(), exception);
            }
        }

        for (FoodProfile food : itemFoods.values()) {
            if (!BuiltInRegistries.ITEM.containsKey(food.item())) {
                throw new JsonParseException("Unknown food item " + food.item());
            }

            ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(food.item()));
            boolean isFood = FoodCompat.isFood(stack) || FoodConsumptionService.isCake(food.item());
            if (!isFood) {
                throw new JsonParseException("Configured item " + food.item() + " is not food");
            }

            validateIntrinsics("Food " + food.item(), food.intrinsics(), intrinsics);
        }

        for (TaggedFoodProfile food : taggedFoods.values()) {
            validateIntrinsics("Food tag " + food.tag(), food.intrinsics(), intrinsics);
        }

        return new CookingDataDefinitions(intrinsics, itemFoods, taggedFoods);
    }

    private static void parseCatalog(
            ResourceLocation resource,
            JsonObject root,
            Map<ResourceLocation, IntrinsicDefinition> intrinsics,
            Map<ResourceLocation, FoodProfile> itemFoods,
            Map<ResourceLocation, TaggedFoodProfile> taggedFoods
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
            ParsedFoodProfile profile = parseFood(GsonHelper.convertToJsonObject(element, "food"));
            if (profile.item() != null) {
                FoodProfile itemProfile = profile.forItem(profile.item());
                if (itemFoods.putIfAbsent(profile.item(), itemProfile) != null) {
                    throw new JsonParseException("Duplicate food " + profile.item() + " in " + resource);
                }
            } else {
                TaggedFoodProfile taggedProfile = profile.forTag(profile.tag());
                if (taggedFoods.putIfAbsent(profile.tag(), taggedProfile) != null) {
                    throw new JsonParseException("Duplicate food tag " + profile.tag() + " in " + resource);
                }
            }
        }
    }

    private static IntrinsicDefinition parseIntrinsic(JsonObject json) {
        ResourceLocation id = parseResource(GsonHelper.getAsString(json, "id"));
        JsonArray effectArray = GsonHelper.getAsJsonArray(json, "effects");
        var effects = new java.util.ArrayList<IntrinsicDefinition.EffectDefinition>();

        for (JsonElement element : effectArray) {
            JsonObject effect = GsonHelper.convertToJsonObject(element, "effect");
            ResourceLocation effectId = parseResource(GsonHelper.getAsString(effect, "id"));
            if (!BuiltInRegistries.MOB_EFFECT.containsKey(effectId)) {
                throw new JsonParseException("Intrinsic " + id + " references unknown effect " + effectId);
            }

            effects.add(new IntrinsicDefinition.EffectDefinition(
                    effectId,
                    positiveInt(effect, "duration", 1),
                    nonNegativeInt(effect, "amplifier", 0),
                    positiveInt(effect, "maximum_duration", 20 * 60 * 20),
                    nonNegativeInt(effect, "maximum_amplifier", 2),
                    GsonHelper.getAsBoolean(effect, "ambient", false),
                    GsonHelper.getAsBoolean(effect, "show_particles", true),
                    chance(effect)
            ));
        }

        if (effects.isEmpty()) {
            throw new JsonParseException("Intrinsic " + id + " must define at least one effect");
        }
        return new IntrinsicDefinition(id, effects);
    }

    private static ParsedFoodProfile parseFood(JsonObject json) {
        boolean hasItem = json.has("item");
        boolean hasTag = json.has("tag");
        if (hasItem == hasTag) {
            throw new JsonParseException("Food must define exactly one of item or tag");
        }

        ResourceLocation item = hasItem
                ? parseResource(GsonHelper.getAsString(json, "item"))
                : null;
        ResourceLocation tag = hasTag
                ? parseResource(GsonHelper.getAsString(json, "tag"))
                : null;
        String selector = item != null ? "item " + item : "tag " + tag;
        PreparationTier tier;
        try {
            tier = PreparationTier.parse(GsonHelper.getAsString(json, "tier"));
        } catch (IllegalArgumentException exception) {
            throw new JsonParseException("Unknown preparation tier for " + selector, exception);
        }

        FoodEffectMode effectMode;
        try {
            effectMode = FoodEffectMode.parse(GsonHelper.getAsString(json, "mode", "append"));
        } catch (IllegalArgumentException exception) {
            throw new JsonParseException("Unknown effect mode for " + selector, exception);
        }

        float healing = GsonHelper.getAsFloat(json, "healing");
        if (!Float.isFinite(healing) || healing < 0.0F || healing > 40.0F) {
            throw new JsonParseException("Healing for " + selector + " must be between 0 and 40");
        }

        JsonArray intrinsicArray = GsonHelper.getAsJsonArray(json, "intrinsics", new JsonArray());
        var intrinsicIds = new java.util.ArrayList<ResourceLocation>();
        for (JsonElement element : intrinsicArray) {
            ResourceLocation intrinsicId = parseResource(GsonHelper.convertToString(element, "intrinsic"));
            if (intrinsicIds.contains(intrinsicId)) {
                throw new JsonParseException("Food " + selector + " references intrinsic " + intrinsicId + " more than once");
            }
            intrinsicIds.add(intrinsicId);
        }

        return new ParsedFoodProfile(item, tag, tier, healing, effectMode, intrinsicIds);
    }

    private static float chance(JsonObject effect) {
        float value = GsonHelper.getAsFloat(effect, "chance", 1.0F);
        if (!Float.isFinite(value) || value < 0.0F || value > 1.0F) {
            throw new JsonParseException("chance must be between 0 and 1");
        }
        return value;
    }

    private static ResourceLocation parseResource(String value) {
        /*? if >=1.20.5 {*/
        return ResourceLocation.parse(value);
        /*?} else {*/
        /*return new ResourceLocation(value);*/
        /*?}*/
    }

    private static void validateIntrinsics(
            String selector,
            Iterable<ResourceLocation> intrinsicIds,
            Map<ResourceLocation, IntrinsicDefinition> intrinsics
    ) {
        for (ResourceLocation intrinsic : intrinsicIds) {
            if (!intrinsics.containsKey(intrinsic)) {
                throw new JsonParseException(selector + " references missing intrinsic " + intrinsic);
            }
        }
    }

    static Map<ResourceLocation, FoodProfile> expandTags(
            Map<ResourceLocation, FoodProfile> itemFoods,
            Map<ResourceLocation, TaggedFoodProfile> taggedFoods
    ) {
        Map<ResourceLocation, FoodProfile> foods = new HashMap<>();
        Map<ResourceLocation, ResourceLocation> matchedTags = new HashMap<>();

        for (TaggedFoodProfile taggedFood : taggedFoods.values()) {
            TagKey<Item> tag = TagKey.create(Registries.ITEM, taggedFood.tag());
            for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
                Item item = holder.value();
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
                if (itemFoods.containsKey(itemId) || !FoodCompat.isFood(new ItemStack(item))) {
                    continue;
                }

                ResourceLocation previousTag = matchedTags.putIfAbsent(itemId, taggedFood.tag());
                if (previousTag != null) {
                    throw new JsonParseException(
                            "Food " + itemId + " matches conflicting tags " + previousTag + " and " + taggedFood.tag()
                    );
                }
                foods.put(itemId, taggedFood.forItem(itemId));
            }
        }

        foods.putAll(itemFoods);
        return foods;
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

    private record ParsedFoodProfile(
            ResourceLocation item,
            ResourceLocation tag,
            PreparationTier tier,
            float healing,
            FoodEffectMode effectMode,
            java.util.List<ResourceLocation> intrinsics
    ) {
        private FoodProfile forItem(ResourceLocation itemId) {
            return new FoodProfile(itemId, tier, healing, effectMode, intrinsics);
        }

        private TaggedFoodProfile forTag(ResourceLocation tagId) {
            return new TaggedFoodProfile(tagId, tier, healing, effectMode, intrinsics);
        }
    }

    private record TaggedFoodProfile(
            ResourceLocation tag,
            PreparationTier tier,
            float healing,
            FoodEffectMode effectMode,
            java.util.List<ResourceLocation> intrinsics
    ) {
        private TaggedFoodProfile {
            intrinsics = java.util.List.copyOf(intrinsics);
        }

        private FoodProfile forItem(ResourceLocation itemId) {
            return new FoodProfile(itemId, tier, healing, effectMode, intrinsics);
        }
    }

    record CookingDataDefinitions(
            Map<ResourceLocation, IntrinsicDefinition> intrinsics,
            Map<ResourceLocation, FoodProfile> itemFoods,
            Map<ResourceLocation, TaggedFoodProfile> taggedFoods
    ) {
        CookingDataDefinitions {
            intrinsics = Map.copyOf(intrinsics);
            itemFoods = Map.copyOf(itemFoods);
            taggedFoods = Map.copyOf(taggedFoods);
        }
    }

    @Override
    protected void apply(CookingDataDefinitions definitions, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, FoodProfile> foods = expandTags(definitions.itemFoods(), definitions.taggedFoods());
        CookingDataSnapshot snapshot = new CookingDataSnapshot(definitions.intrinsics(), foods);
        manager.replace(snapshot);
        WellSeasoned.LOGGER.info(
                "Loaded {} Well Seasoned intrinsics and {} food profiles",
                snapshot.intrinsics().size(),
                snapshot.foods().size()
        );
    }

    //? if fabric {
    /*@Override
    public ResourceLocation getFabricId() {
        return WellSeasoned.id("cooking");
    }
    *///?}
}

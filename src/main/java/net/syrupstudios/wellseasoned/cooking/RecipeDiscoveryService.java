package net.syrupstudios.wellseasoned.cooking;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.syrupstudios.wellseasoned.WellSeasoned;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Unlocks the next authored preparation when its defining ingredient is discovered. */
public final class RecipeDiscoveryService {
    private static final Map<ResourceLocation, List<ResourceLocation>> UNLOCKS = createUnlocks();

    private RecipeDiscoveryService() {
    }

    public static void discover(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        List<ResourceLocation> recipes = UNLOCKS.get(BuiltInRegistries.ITEM.getKey(stack.getItem()));
        if (recipes != null) {
            player.awardRecipesByKey(recipes);
        }
    }

    public static void discoverInventory(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().items) {
            discover(player, stack);
        }
        for (ItemStack stack : player.getInventory().offhand) {
            discover(player, stack);
        }
    }

    private static Map<ResourceLocation, List<ResourceLocation>> createUnlocks() {
        Map<ResourceLocation, List<ResourceLocation>> result = new LinkedHashMap<>();

        chain(result, "minecraft:pumpkin", "roasted_pumpkin", "pumpkin_jam", "pumpkin_empanada");
        chain(result, "minecraft:carrot", "carrot_cupcake", "pickled_carrots", "carrot_stew");
        chain(result, "minecraft:glow_berries", "glow_berry_tart", "glow_berry_preserves", "luminous_risotto");
        chain(result, "minecraft:melon_slice", "melon_sorbet", "melon_jelly", "melon_salad");
        chain(result, "minecraft:apple", "baked_apple", "apple_butter", "apple_pie");
        chain(result, "minecraft:potato", "honeyed_potato", "potato_hash", "miners_breakfast");
        chain(result, "minecraft:beetroot", "beet_chips", "pickled_beets", "beet_borscht");
        chain(result, "minecraft:chorus_fruit", "chorus_bites", "chorus_jam", "chorus_parfait");
        chain(result, "minecraft:cocoa_beans", "cocoa_biscuit", "dark_chocolate", "cocoa_pancakes");
        chain(result, "minecraft:sea_pickle", "sea_pickle_roll", "pickled_sea_greens", "ocean_roll");
        chain(result, "minecraft:brown_mushroom", "mushroom_skewer", "dried_mushrooms", "mushroom_pie");
        chain(result, "minecraft:sweet_berries", "berry_mash", "berry_jam", "berry_danish");

        unlocks(result, "pumpkin_empanada", "verdant_curry_kit");
        unlocks(result, "luminous_risotto", "verdant_curry_kit");
        unlocks(result, "ocean_roll", "verdant_curry_kit");
        unlocks(result, "melon_salad", "ember_curry_kit");
        unlocks(result, "beet_borscht", "ember_curry_kit");
        unlocks(result, "apple_pie", "ember_curry_kit");
        unlocks(result, "carrot_stew", "moonlit_ramen_kit");
        unlocks(result, "chorus_parfait", "moonlit_ramen_kit");
        unlocks(result, "cocoa_pancakes", "moonlit_ramen_kit");
        unlocks(result, "miners_breakfast", "harvest_feast_kit");
        unlocks(result, "mushroom_pie", "harvest_feast_kit");
        unlocks(result, "berry_danish", "harvest_feast_kit");

        unlocks(result, "verdant_curry_kit", "verdant_curry");
        unlocks(result, "ember_curry_kit", "ember_curry");
        unlocks(result, "moonlit_ramen_kit", "moonlit_ramen");
        unlocks(result, "harvest_feast_kit", "harvest_feast");

        return Map.copyOf(result);
    }

    private static void chain(
            Map<ResourceLocation, List<ResourceLocation>> result,
            String raw,
            String simple,
            String preserved,
            String meal
    ) {
        result.put(ResourceLocation.parse(raw), List.of(WellSeasoned.id(simple)));
        unlocks(result, simple, preserved);
        unlocks(result, preserved, meal);
    }

    private static void unlocks(Map<ResourceLocation, List<ResourceLocation>> result, String item, String recipe) {
        result.computeIfAbsent(WellSeasoned.id(item), ignored -> new java.util.ArrayList<>())
                .add(WellSeasoned.id(recipe));
    }
}

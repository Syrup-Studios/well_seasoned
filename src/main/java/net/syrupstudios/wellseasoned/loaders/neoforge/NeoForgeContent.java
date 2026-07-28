package net.syrupstudios.wellseasoned.loaders.neoforge;

//? if neoforge {
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.syrupstudios.wellseasoned.WellSeasoned;
import net.syrupstudios.wellseasoned.item.SeasonedFoodItem;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class NeoForgeContent {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(WellSeasoned.MOD_ID);

    private static final Map<String, DeferredItem<SeasonedFoodItem>> FOODS = new LinkedHashMap<>();

    public static final DeferredItem<Item> VERDANT_CURRY_KIT =
            ITEMS.registerSimpleItem("verdant_curry_kit", new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> EMBER_CURRY_KIT =
            ITEMS.registerSimpleItem("ember_curry_kit", new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> MOONLIT_RAMEN_KIT =
            ITEMS.registerSimpleItem("moonlit_ramen_kit", new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> HARVEST_FEAST_KIT =
            ITEMS.registerSimpleItem("harvest_feast_kit", new Item.Properties().stacksTo(16));

    private static final List<String> BOWL_FOODS = List.of(
            "carrot_stew", "luminous_risotto", "miners_breakfast", "beet_borscht",
            "chorus_parfait", "ocean_roll", "verdant_curry", "ember_curry",
            "moonlit_ramen", "harvest_feast"
    );

    public static final List<String> FOOD_IDS = List.of(
            "roasted_pumpkin", "pumpkin_jam", "pumpkin_empanada",
            "carrot_cupcake", "pickled_carrots", "carrot_stew",
            "glow_berry_tart", "glow_berry_preserves", "luminous_risotto",
            "melon_sorbet", "melon_jelly", "melon_salad",
            "baked_apple", "apple_butter", "apple_pie",
            "honeyed_potato", "potato_hash", "miners_breakfast",
            "beet_chips", "pickled_beets", "beet_borscht",
            "chorus_bites", "chorus_jam", "chorus_parfait",
            "cocoa_biscuit", "dark_chocolate", "cocoa_pancakes",
            "sea_pickle_roll", "pickled_sea_greens", "ocean_roll",
            "mushroom_skewer", "dried_mushrooms", "mushroom_pie",
            "berry_mash", "berry_jam", "berry_danish",
            "verdant_curry", "ember_curry", "moonlit_ramen", "harvest_feast"
    );

    static {
        for (String id : FOOD_IDS) {
            FOODS.put(id, ITEMS.register(id, () -> new SeasonedFoodItem(foodProperties(id))));
        }
    }

    private NeoForgeContent() {
    }

    private static Item.Properties foodProperties(String id) {
        FoodProperties.Builder food = new FoodProperties.Builder()
                .nutrition(0)
                .saturationModifier(0.0F)
                .alwaysEdible();
        if (BOWL_FOODS.contains(id)) {
            food.usingConvertsTo(Items.BOWL);
        }
        return new Item.Properties().stacksTo(16).food(food.build());
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(NeoForgeContent::addCreativeTabContents);
    }

    public static DeferredItem<SeasonedFoodItem> food(String id) {
        DeferredItem<SeasonedFoodItem> item = FOODS.get(id);
        if (item == null) {
            throw new IllegalArgumentException("Unknown food " + id);
        }
        return item;
    }

    public static Iterable<DeferredItem<SeasonedFoodItem>> foods() {
        return FOODS.values();
    }

    private static void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            for (DeferredItem<SeasonedFoodItem> food : FOODS.values()) {
                event.accept(food);
            }
            event.accept(VERDANT_CURRY_KIT);
            event.accept(EMBER_CURRY_KIT);
            event.accept(MOONLIT_RAMEN_KIT);
            event.accept(HARVEST_FEAST_KIT);
        }
    }
}
//?}

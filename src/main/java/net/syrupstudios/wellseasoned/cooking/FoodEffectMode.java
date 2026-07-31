package net.syrupstudios.wellseasoned.cooking;

import java.util.Locale;

/** Controls how configured effects interact with effects stored on an item. */
public enum FoodEffectMode {
    APPEND,
    REPLACE;

    public static FoodEffectMode parse(String value) {
        return valueOf(value.toUpperCase(Locale.ROOT));
    }
}

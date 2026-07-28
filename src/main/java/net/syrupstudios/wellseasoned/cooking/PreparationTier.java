package net.syrupstudios.wellseasoned.cooking;

import java.util.Locale;

/**
 * The four authored preparation stages. Multipliers are deliberately modest:
 * food should support an expedition without replacing equipment progression.
 */
public enum PreparationTier {
    SIMPLE(1.0F, 1.0F, 0),
    PRESERVED(1.35F, 2.5F, 0),
    MEAL(1.75F, 5.0F, 0),
    SPECIAL_MEAL(2.0F, 8.0F, 1);

    private final float healingMultiplier;
    private final float durationMultiplier;
    private final int amplifierBonus;

    PreparationTier(float healingMultiplier, float durationMultiplier, int amplifierBonus) {
        this.healingMultiplier = healingMultiplier;
        this.durationMultiplier = durationMultiplier;
        this.amplifierBonus = amplifierBonus;
    }

    public float healingMultiplier() {
        return healingMultiplier;
    }

    public float durationMultiplier() {
        return durationMultiplier;
    }

    public int amplifierBonus() {
        return amplifierBonus;
    }

    public static PreparationTier parse(String value) {
        return valueOf(value.toUpperCase(Locale.ROOT));
    }
}

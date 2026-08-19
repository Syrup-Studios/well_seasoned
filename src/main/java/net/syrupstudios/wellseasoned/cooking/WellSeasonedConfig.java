package net.syrupstudios.wellseasoned.cooking;

import net.syrupstudios.syruplibrary.config.ConfigSection;
import net.syrupstudios.syruplibrary.config.ConfigSpec;
import net.syrupstudios.syruplibrary.config.RegisteredConfig;
import net.syrupstudios.syruplibrary.config.SyrupConfigManager;
import net.syrupstudios.syruplibrary.config.value.ConfigValue;

/**
 * Global Well Seasoned settings, backed by Syrup Library's typed config
 * system. The file is {@code well_seasoned.json5} in the loader config
 * directory and is created automatically on first load.
 *
 * <p>Example file:
 * <pre>
 * {
 *   effect_duration_stacking: {
 *     mode: "logarithmic",
 *     strength: 1.0
 *   }
 * }
 * </pre>
 *
 * <p>Missing values fall back to the schema defaults ({@code logarithmic}
 * mode and strength {@code 1.0}); out-of-range numbers are clamped and
 * reported as warnings by Syrup Library.
 */
public final class WellSeasonedConfig {
    public static final String CONFIG_ID = "well_seasoned";
    public static final double DEFAULT_STRENGTH = 1.0;
    public static final double MIN_STRENGTH = 0.0;
    public static final double MAX_STRENGTH = 100.0;
    public static final DurationStacking.StackingMode DEFAULT_MODE =
            DurationStacking.StackingMode.LOGARITHMIC;

    private static final String STACKING_SECTION = "effect_duration_stacking";
    private static final String MODE_KEY = "mode";
    private static final String STRENGTH_KEY = "strength";

    private static volatile ConfigValue<DurationStacking.StackingMode> modeValue;
    private static volatile ConfigValue<Double> strengthValue;

    private WellSeasonedConfig() {
    }

    /** Registers the Well Seasoned config with the process-wide manager once at mod init. */
    public static void load() {
        register(SyrupConfigManager.getInstance());
    }

    /** Registers a fresh spec with the supplied manager and loads its file. */
    static RegisteredConfig register(SyrupConfigManager manager) {
        return manager.register(buildSpec());
    }

    static ConfigSpec buildSpec() {
        ConfigSpec spec = ConfigSpec.builder(CONFIG_ID)
                .header(
                        "Well Seasoned global settings.",
                        "Repeated equal-level food effects use diminishing returns."
                )
                .build();
        ConfigSection section = spec.section(
                STACKING_SECTION,
                "Controls how repeated equal-level food effects stack."
        );
        modeValue = section.enumValue(
                MODE_KEY,
                DurationStacking.StackingMode.class,
                DEFAULT_MODE,
                "The stacking curve: \"logarithmic\" reduces additions on a log2 curve; "
                        + "\"linear_half\" adds half of the incoming duration."
        );
        strengthValue = section.doubleValue(
                STRENGTH_KEY,
                DEFAULT_STRENGTH,
                MIN_STRENGTH,
                MAX_STRENGTH,
                "How aggressive the diminishing returns are. 0.0 adds the full incoming duration every time."
        );
        return spec;
    }

    public static DurationStacking.StackingMode stackingMode() {
        ConfigValue<DurationStacking.StackingMode> mode = modeValue;
        return mode == null ? DEFAULT_MODE : mode.get();
    }

    public static double stackingStrength() {
        ConfigValue<Double> strength = strengthValue;
        return strength == null ? DEFAULT_STRENGTH : strength.get();
    }
}

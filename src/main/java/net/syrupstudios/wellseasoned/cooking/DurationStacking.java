package net.syrupstudios.wellseasoned.cooking;

import java.util.Locale;

/**
 * Pure duration-stacking math for equal-amplifier food effects.
 *
 * <p>The logarithmic rule divides the incoming duration by
 * {@code 1 + strength * log2(1 + currentDuration / incomingDuration)}. The
 * ratio keeps the curve scale-independent: short and long food effects behave
 * the same way. At strength 1.0, stacking onto an equal remaining duration
 * adds exactly half of the incoming duration.
 */
public final class DurationStacking {
    /** Legacy behavior: always add half of the incoming duration. */
    public static final int LINEAR_HALF_MINIMUM_ADDITION = 20;

    private DurationStacking() {
    }

    /**
     * Controls how aggressive duration stacking is. {@code LOGARITHMIC} is the
     * default curve; {@code LINEAR_HALF} reproduces the old fixed half-duration
     * rule with its 20-tick minimum addition.
     */
    public enum StackingMode {
        LOGARITHMIC,
        LINEAR_HALF;

        public static StackingMode parse(String value) {
            return valueOf(value.toUpperCase(Locale.ROOT));
        }
    }

    /**
     * The duration added when stacking an incoming effect onto a currently
     * active effect at the same amplifier.
     *
     * <p>Valid inputs require {@code currentDuration >= 0},
     * {@code incomingDuration > 0} and {@code strength >= 0}. For valid inputs
     * the result is always at least 1 tick and never exceeds
     * {@code incomingDuration}. At {@code currentDuration = 0} or
     * {@code strength = 0} it equals {@code incomingDuration}.
     */
    public static int calculateDurationAddition(int currentDuration, int incomingDuration, double strength) {
        if (currentDuration < 0) {
            throw new IllegalArgumentException("Current duration cannot be negative");
        }
        if (incomingDuration <= 0) {
            throw new IllegalArgumentException("Incoming duration must be positive");
        }
        if (!Double.isFinite(strength) || strength < 0.0) {
            throw new IllegalArgumentException("Strength must be a finite non-negative number");
        }

        double ratio = (double) currentDuration / incomingDuration;
        double log2 = Math.log1p(ratio) / Math.log(2.0);
        double divisor = 1.0 + strength * log2;
        return Math.max(1, (int) Math.round(incomingDuration / divisor));
    }

    /**
     * The result of stacking an incoming effect onto an active effect at the
     * same amplifier, respecting the hard {@code maximumDuration} cap.
     */
    public static int calculateStackedDuration(
            int currentDuration,
            int incomingDuration,
            int maximumDuration,
            double strength,
            StackingMode mode
    ) {
        int addition = switch (mode) {
            case LOGARITHMIC -> calculateDurationAddition(currentDuration, incomingDuration, strength);
            case LINEAR_HALF -> Math.max(LINEAR_HALF_MINIMUM_ADDITION, incomingDuration / 2);
        };
        long stacked = (long) currentDuration + addition;
        return (int) Math.min(stacked, maximumDuration);
    }
}

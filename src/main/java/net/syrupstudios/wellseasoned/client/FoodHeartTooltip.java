package net.syrupstudios.wellseasoned.client;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record FoodHeartTooltip(int quarters) implements TooltipComponent {
    public FoodHeartTooltip {
        if (quarters < 0) {
            throw new IllegalArgumentException("Heart quarters cannot be negative");
        }
    }
}

package net.syrupstudios.wellseasoned.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import net.syrupstudios.wellseasoned.WellSeasoned;

public final class ClientFoodHeartTooltip implements ClientTooltipComponent {
    private static final ResourceLocation CONTAINER =
            WellSeasoned.vanillaId("hud/heart/container");
    private static final ResourceLocation FULL =
            WellSeasoned.vanillaId("hud/heart/full");
    private static final int HEART_SIZE = 9;
    private static final int HEART_SPACING = 8;
    private static final int HEARTS_PER_ROW = 10;
    private static final int ROW_HEIGHT = 10;

    private final int quarters;
    private final int slots;

    public ClientFoodHeartTooltip(int quarters) {
        this.quarters = quarters;
        this.slots = Math.max(1, (quarters + 3) / 4);
    }

    @Override
    public int getHeight() {
        return ((slots + HEARTS_PER_ROW - 1) / HEARTS_PER_ROW) * ROW_HEIGHT;
    }

    @Override
    public int getWidth(Font font) {
        return Math.min(slots, HEARTS_PER_ROW) * HEART_SPACING + 1;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        for (int slot = 0; slot < slots; slot++) {
            int slotX = x + (slot % HEARTS_PER_ROW) * HEART_SPACING;
            int slotY = y + (slot / HEARTS_PER_ROW) * ROW_HEIGHT;
            int filledQuarters = Math.min(4, Math.max(0, quarters - slot * 4));

            ClientRenderCompat.blitHeart(graphics, CONTAINER, slotX, slotY, HEART_SIZE);
            if (filledQuarters == 0) {
                continue;
            }
            if (filledQuarters == 4) {
                ClientRenderCompat.blitHeart(graphics, FULL, slotX, slotY, HEART_SIZE);
                continue;
            }

            int filledWidth = (HEART_SIZE * filledQuarters + 3) / 4;
            graphics.enableScissor(slotX, slotY, slotX + filledWidth, slotY + HEART_SIZE);
            ClientRenderCompat.blitHeart(graphics, FULL, slotX, slotY, HEART_SIZE);
            graphics.disableScissor();
        }
    }
}

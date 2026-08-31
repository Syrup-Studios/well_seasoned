package net.syrupstudios.wellseasoned.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.syrupstudios.wellseasoned.cooking.FoodHealingResolver;
import net.syrupstudios.wellseasoned.WellSeasoned;

/**
 * Ghostly heart overlay previewing how much health the held food restores.
 * Runs after vanilla renders its hearts and draws translucent full/half hearts
 * over the slots the missing health would occupy when eaten.
 */
public final class FoodHealthPreview {
    private static final ResourceLocation CONTAINER =
            WellSeasoned.vanillaId("hud/heart/container");
    private static final ResourceLocation CONTAINER_HARDCORE =
            WellSeasoned.vanillaId("hud/heart/container_hardcore");
    private static final ResourceLocation FULL =
            WellSeasoned.vanillaId("hud/heart/full");
    private static final ResourceLocation FULL_HARDCORE =
            WellSeasoned.vanillaId("hud/heart/hardcore_full");
    private static final ResourceLocation HALF =
            WellSeasoned.vanillaId("hud/heart/half");
    private static final ResourceLocation HALF_HARDCORE =
            WellSeasoned.vanillaId("hud/heart/hardcore_half");

    private static final int HEART_SIZE = 9;
    private static final int HEART_SPACING = 8;
    private static final int HEARTS_PER_ROW = 10;
    private static final int MAX_HEART_SLOTS = 1000;
    private static final float MAX_PULSE_ALPHA = 0.8F;
    private static final float CONTAINER_ALPHA_FACTOR = 0.25F;

    private FoodHealthPreview() {
    }

    public static void render(
            GuiGraphics graphics,
            Player player,
            int left,
            int top,
            int rowHeight,
            int regenHeart,
            float maxHealthValue,
            int healthCeil,
            int absorptionCeil,
            int guiTicks,
            RandomSource random
    ) {
        if (player == null) {
            return;
        }

        ItemStack stack = findHeldFood(player);
        if (stack == null) {
            return;
        }

        float healing = FoodHealingResolver.resolve(stack, player);
        float currentHealth = player.getHealth();
        float maxHealth = player.getMaxHealth();
        if (!Float.isFinite(healing) || !Float.isFinite(currentHealth) || !Float.isFinite(maxHealth)) {
            return;
        }
        if (healing <= 0.0F || currentHealth >= maxHealth) {
            return;
        }

        // Absorption is not healable, so cap the preview at maximum regular health.
        float targetHealth = Math.min(currentHealth + healing, maxHealth);
        if (targetHealth <= currentHealth) {
            return;
        }

        int finalHealth = Mth.ceil(targetHealth);
        int firstSlot = Math.max(0, Mth.ceil(currentHealth) / 2);
        int endSlot = Math.max(0, Mth.ceil(targetHealth / 2.0F));
        if (firstSlot >= endSlot || endSlot > MAX_HEART_SLOTS) {
            return;
        }

        // Absorption still contributes to the visible heart rows, so include it in
        // the total slot count used for layout parity with vanilla.
        int healthSlots = Mth.ceil(maxHealthValue / 2.0F);
        int totalSlots = healthSlots + Mth.ceil(absorptionCeil / 2.0F);
        if (totalSlots <= 0 || totalSlots > MAX_HEART_SLOTS) {
            return;
        }

        float pulse = pulseAlpha(guiTicks);
        if (pulse <= 0.0F) {
            return;
        }

        boolean hardcore = player.level().getLevelData().isHardcore();
        float containerAlpha = pulse * CONTAINER_ALPHA_FACTOR;

        // Re-seed exactly like vanilla renderPlayerHealth and consume one jitter
        // call for every heart slot so the preview offsets match the real hearts.
        random.setSeed((long) guiTicks * 312871L);
        boolean lowHealthJitter = healthCeil + absorptionCeil <= 4;
        for (int slot = totalSlots - 1; slot >= 0; slot--) {
            int row = slot / HEARTS_PER_ROW;
            int x = left + (slot % HEARTS_PER_ROW) * HEART_SPACING;
            int y = top - row * rowHeight;
            if (lowHealthJitter) {
                y += random.nextInt(2);
            }
            if (slot < healthSlots && slot == regenHeart) {
                y -= 2;
            }

            if (slot < firstSlot || slot >= endSlot) {
                continue;
            }

            drawSlot(graphics, x, y, slot, finalHealth, hardcore, containerAlpha, pulse);
        }
    }

    private static ItemStack findHeldFood(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (net.syrupstudios.wellseasoned.cooking.FoodCompat.isFood(mainHand, player)) {
            return mainHand;
        }
        ItemStack offHand = player.getOffhandItem();
        if (net.syrupstudios.wellseasoned.cooking.FoodCompat.isFood(offHand, player)) {
            return offHand;
        }
        return null;
    }

    /**
     * Deterministic triangle-wave pulse derived from the GUI tick counter so no
     * per-loader tick state is needed: fade in, hold, fade out, rest, repeat.
     */
    private static float pulseAlpha(int guiTicks) {
        int phase = Math.floorMod(guiTicks, 32);
        float unclamped = phase <= 16
                ? -0.5F + phase * 0.125F
                : 1.5F - (phase - 16) * 0.125F;
        return Mth.clamp(unclamped, 0.0F, 1.0F) * MAX_PULSE_ALPHA;
    }

    private static void drawSlot(
            GuiGraphics graphics,
            int x,
            int y,
            int slot,
            int finalHealth,
            boolean hardcore,
            float containerAlpha,
            float heartAlpha
    ) {
        // The visual output is limited to vanilla half-heart resolution: every
        // preview slot is a full heart except the final slot, which is a half
        // heart only when the target health ends exactly on one.
        boolean halfHeart = slot * 2 + 1 == finalHealth;
        ResourceLocation heart = halfHeart
                ? (hardcore ? HALF_HARDCORE : HALF)
                : (hardcore ? FULL_HARDCORE : FULL);
        ResourceLocation container = hardcore ? CONTAINER_HARDCORE : CONTAINER;

        RenderSystem.enableBlend();
        try {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, containerAlpha);
            ClientRenderCompat.blitHeart(graphics, container, x, y, HEART_SIZE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, heartAlpha);
            ClientRenderCompat.blitHeart(graphics, heart, x, y, HEART_SIZE);
        } finally {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
        }
    }
}

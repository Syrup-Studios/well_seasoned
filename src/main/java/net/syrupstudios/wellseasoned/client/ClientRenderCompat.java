package net.syrupstudios.wellseasoned.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.syrupstudios.wellseasoned.WellSeasoned;

/** Bridges the sprite renderer introduced after Minecraft 1.20.1. */
final class ClientRenderCompat {
    private static final ResourceLocation GUI_ICONS =
            WellSeasoned.vanillaId("textures/gui/icons.png");

    private ClientRenderCompat() {
    }

    static void blitHeart(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int size) {
        /*? if >=1.20.5 {*/
        graphics.blitSprite(sprite, x, y, size, size);
        /*?} else {*/
        String path = sprite.getPath();
        int u = path.contains("container") ? 16 : path.endsWith("half") ? 61 : 52;
        int v = path.contains("hardcore") ? 45 : 0;
        graphics.blit(GUI_ICONS, x, y, (float) u, (float) v, size, size, 256, 256);
        /*?}*/
    }
}

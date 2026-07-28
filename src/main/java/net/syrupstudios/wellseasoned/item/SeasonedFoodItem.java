package net.syrupstudios.wellseasoned.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** A thin item type; all healing and effects come from reloadable food profiles. */
public final class SeasonedFoodItem extends Item {
    public SeasonedFoodItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.well_seasoned.prepared_food").withStyle(ChatFormatting.DARK_GREEN));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}

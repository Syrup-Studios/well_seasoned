package net.syrupstudios.wellseasoned.network;

/*? if >=1.20.5 {*/
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
/*?} else {*/
import net.minecraft.network.FriendlyByteBuf;
/*?}*/
import net.minecraft.resources.ResourceLocation;
import net.syrupstudios.wellseasoned.WellSeasoned;
import net.syrupstudios.wellseasoned.cooking.CookingDataSnapshot;
import net.syrupstudios.wellseasoned.cooking.FoodEffectMode;
import net.syrupstudios.wellseasoned.cooking.FoodProfile;
import net.syrupstudios.wellseasoned.cooking.IntrinsicDefinition;
import net.syrupstudios.wellseasoned.cooking.PreparationTier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public record CookingDataPayload(CookingDataSnapshot snapshot)
        /*? if >=1.20.5 {*/
        implements CustomPacketPayload
        /*?}*/
{
    /*? if >=1.20.5 {*/
    public static final Type<CookingDataPayload> TYPE =
            new Type<>(WellSeasoned.id("cooking_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CookingDataPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public CookingDataPayload decode(RegistryFriendlyByteBuf buffer) {
                    return new CookingDataPayload(readSnapshot(buffer));
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, CookingDataPayload payload) {
                    writeSnapshot(buffer, payload.snapshot);
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    /*?} else {*/
    /*public static void encode(FriendlyByteBuf buffer, CookingDataPayload payload) {
        writeSnapshot(buffer, payload.snapshot);
    }

    public static void encode(CookingDataPayload payload, FriendlyByteBuf buffer) {
        encode(buffer, payload);
    }

    public static CookingDataPayload decode(FriendlyByteBuf buffer) {
        return new CookingDataPayload(readSnapshot(buffer));
    }*/
    /*?}*/

    private static void writeSnapshot(
            /*? if >=1.20.5 {*/
            RegistryFriendlyByteBuf buffer
            /*?} else {*/
            /*FriendlyByteBuf buffer*/
            /*?}*/
            , CookingDataSnapshot snapshot) {
        buffer.writeVarInt(snapshot.intrinsics().size());
        for (IntrinsicDefinition intrinsic : snapshot.intrinsics().values()) {
            buffer.writeResourceLocation(intrinsic.id());
            buffer.writeVarInt(intrinsic.effects().size());
            for (IntrinsicDefinition.EffectDefinition effect : intrinsic.effects()) {
                buffer.writeResourceLocation(effect.effect());
                buffer.writeVarInt(effect.duration());
                buffer.writeVarInt(effect.amplifier());
                buffer.writeVarInt(effect.maximumDuration());
                buffer.writeVarInt(effect.maximumAmplifier());
                buffer.writeBoolean(effect.ambient());
                buffer.writeBoolean(effect.showParticles());
                buffer.writeFloat(effect.chance());
            }
        }

        buffer.writeVarInt(snapshot.foods().size());
        for (FoodProfile food : snapshot.foods().values()) {
            buffer.writeResourceLocation(food.item());
            buffer.writeVarInt(food.tier().ordinal());
            buffer.writeFloat(food.healing());
            buffer.writeVarInt(food.effectMode().ordinal());
            buffer.writeVarInt(food.intrinsics().size());
            for (ResourceLocation intrinsic : food.intrinsics()) {
                buffer.writeResourceLocation(intrinsic);
            }
        }
    }

    private static CookingDataSnapshot readSnapshot(
            /*? if >=1.20.5 {*/
            RegistryFriendlyByteBuf buffer
            /*?} else {*/
            /*FriendlyByteBuf buffer*/
            /*?}*/
    ) {
        Map<ResourceLocation, IntrinsicDefinition> intrinsics = new HashMap<>();
        int intrinsicCount = readCount(buffer);
        for (int index = 0; index < intrinsicCount; index++) {
            ResourceLocation id = buffer.readResourceLocation();
            int effectCount = readCount(buffer);
            var effects = new ArrayList<IntrinsicDefinition.EffectDefinition>(effectCount);
            for (int effectIndex = 0; effectIndex < effectCount; effectIndex++) {
                effects.add(new IntrinsicDefinition.EffectDefinition(
                        buffer.readResourceLocation(),
                        buffer.readVarInt(),
                        buffer.readVarInt(),
                        buffer.readVarInt(),
                        buffer.readVarInt(),
                        buffer.readBoolean(),
                        buffer.readBoolean(),
                        buffer.readFloat()
                ));
            }
            intrinsics.put(id, new IntrinsicDefinition(id, effects));
        }

        Map<ResourceLocation, FoodProfile> foods = new HashMap<>();
        int foodCount = readCount(buffer);
        for (int index = 0; index < foodCount; index++) {
            ResourceLocation item = buffer.readResourceLocation();
            int tierIndex = buffer.readVarInt();
            if (tierIndex < 0 || tierIndex >= PreparationTier.values().length) {
                throw new IllegalArgumentException("Invalid preparation tier index " + tierIndex);
            }
            float healing = buffer.readFloat();
            int effectModeIndex = buffer.readVarInt();
            if (effectModeIndex < 0 || effectModeIndex >= FoodEffectMode.values().length) {
                throw new IllegalArgumentException("Invalid food effect mode index " + effectModeIndex);
            }
            int intrinsicIdCount = readCount(buffer);
            var intrinsicIds = new ArrayList<ResourceLocation>(intrinsicIdCount);
            for (int intrinsicIndex = 0; intrinsicIndex < intrinsicIdCount; intrinsicIndex++) {
                intrinsicIds.add(buffer.readResourceLocation());
            }
            foods.put(item, new FoodProfile(
                    item,
                    PreparationTier.values()[tierIndex],
                    healing,
                    FoodEffectMode.values()[effectModeIndex],
                    intrinsicIds
            ));
        }
        return new CookingDataSnapshot(intrinsics, foods);
    }

    private static int readCount(
            /*? if >=1.20.5 {*/
            RegistryFriendlyByteBuf buffer
            /*?} else {*/
            /*FriendlyByteBuf buffer*/
            /*?}*/
    ) {
        int count = buffer.readVarInt();
        if (count < 0 || count > 65_536) {
            throw new IllegalArgumentException("Invalid cooking data collection size " + count);
        }
        return count;
    }
}

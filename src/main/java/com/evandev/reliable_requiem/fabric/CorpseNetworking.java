package com.evandev.reliable_requiem.fabric;

//? if fabric {
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/** Tells players watching a new corpse that it was just made, so they see its body fall. */
public final class CorpseNetworking {
    public record FreshCorpse(int entityId) implements CustomPacketPayload {
        public static final Type<FreshCorpse> TYPE = new Type<>(Identifier.fromNamespaceAndPath("last_rites", "fresh_corpse"));
        public static final StreamCodec<RegistryFriendlyByteBuf, FreshCorpse> CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, FreshCorpse::entityId, FreshCorpse::new);

        @Override
        public Type<FreshCorpse> type() {
            return TYPE;
        }
    }

    private CorpseNetworking() {
    }

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(FreshCorpse.TYPE, FreshCorpse.CODEC);
    }

    public static void announce(Entity corpse) {
        FreshCorpse payload = new FreshCorpse(corpse.getId());
        for (ServerPlayer player : PlayerLookup.tracking(corpse)) {
            if (ServerPlayNetworking.canSend(player, FreshCorpse.TYPE)) ServerPlayNetworking.send(player, payload);
        }
    }
}
//?}

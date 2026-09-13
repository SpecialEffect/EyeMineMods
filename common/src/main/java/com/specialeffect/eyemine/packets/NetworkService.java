package com.specialeffect.eyemine.packets;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public interface NetworkService {
    <T extends CustomPacketPayload> void registerC2S(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            PacketHandler<T> handler);

    void sendToServer(CustomPacketPayload payload);

    @FunctionalInterface
    interface PacketHandler<T> {
        void handle(T payload, PacketContext context);
    }

    interface PacketContext {
        void queue(Runnable work);
        Player getPlayer();
    }
}

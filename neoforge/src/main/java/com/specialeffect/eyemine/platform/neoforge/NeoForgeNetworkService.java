package com.specialeffect.eyemine.platform.neoforge;

import com.specialeffect.eyemine.packets.NetworkService;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public class NeoForgeNetworkService implements NetworkService {
    private static final Map<CustomPacketPayload.Type<?>, RegistrationEntry<?>> pendingRegistrations = new HashMap<>();

    @SuppressWarnings("unchecked")
    public record RegistrationEntry<T extends CustomPacketPayload>(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            PacketHandler<T> handler) {

        public void handlePayload(CustomPacketPayload payload, IPayloadContext context) {
            T typed = (T) payload;
            handler.handle(typed, new NeoForgePacketContext(context));
        }
    }

    @Override
    public <T extends CustomPacketPayload> void registerC2S(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            PacketHandler<T> handler) {
        pendingRegistrations.put(type, new RegistrationEntry<>(type, codec, handler));
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }

    public static Map<CustomPacketPayload.Type<?>, RegistrationEntry<?>> getPendingRegistrations() {
        return pendingRegistrations;
    }

    private record NeoForgePacketContext(IPayloadContext neoContext) implements PacketContext {
        @Override
        public void queue(Runnable work) {
            neoContext.enqueueWork(work);
        }

        @Override
        public Player getPlayer() {
            return neoContext.player();
        }
    }
}

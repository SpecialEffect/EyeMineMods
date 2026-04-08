package com.specialeffect.eyemine.platform.fabric;

import com.specialeffect.eyemine.packets.NetworkService;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public class FabricNetworkService implements NetworkService {

    @Override
    public <T extends CustomPacketPayload> void registerC2S(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            PacketHandler<T> handler) {
        PayloadTypeRegistry.serverboundPlay().register(type, codec);
        ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
            handler.handle(payload, new FabricPacketContext(context));
        });
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    private record FabricPacketContext(ServerPlayNetworking.Context fabricContext) implements PacketContext {
        @Override
        public void queue(Runnable work) {
            fabricContext.player().level().getServer().execute(work);
        }

        @Override
        public Player getPlayer() {
            return fabricContext.player();
        }
    }
}

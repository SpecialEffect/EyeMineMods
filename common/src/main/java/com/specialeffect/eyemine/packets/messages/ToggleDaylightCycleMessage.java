package com.specialeffect.eyemine.packets.messages;

import com.specialeffect.eyemine.EyeMine;
import com.specialeffect.eyemine.packets.NetworkService;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.level.gamerules.GameRules;

public record ToggleDaylightCycleMessage() implements CustomPacketPayload {
    public static final Type<ToggleDaylightCycleMessage> ID = new Type<>(
            Identifier.fromNamespaceAndPath(EyeMine.MOD_ID, "toggle_daylight_cycle"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleDaylightCycleMessage> CODEC =
            StreamCodec.unit(new ToggleDaylightCycleMessage());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static class Handler {
        public static void handle(ToggleDaylightCycleMessage packet, NetworkService.PacketContext context) {
            context.queue(() -> {
                if (!(context.getPlayer() instanceof ServerPlayer player)
                        || !player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
                    return;
                }

                var level = player.level();
                GameRules rules = level.getGameRules();
                rules.set(GameRules.ADVANCE_TIME, !rules.get(GameRules.ADVANCE_TIME), level.getServer());
            });
        }
    }
}

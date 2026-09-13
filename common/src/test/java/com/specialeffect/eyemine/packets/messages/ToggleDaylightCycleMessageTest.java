package com.specialeffect.eyemine.packets.messages;

import com.specialeffect.eyemine.packets.NetworkService;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.level.gamerules.GameRules;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ToggleDaylightCycleMessageTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void togglesCurrentServerRuleInsteadOfRememberingClientState(boolean initial) {
        var player = mock(ServerPlayer.class);
        var level = mock(ServerLevel.class);
        var server = mock(MinecraftServer.class);
        PermissionSet permissions = permission -> permission.equals(Permissions.COMMANDS_GAMEMASTER);
        var rules = new GameRules(List.of(GameRules.ADVANCE_TIME));
        rules.set(GameRules.ADVANCE_TIME, initial, null);
        when(player.permissions()).thenReturn(permissions);
        when(player.level()).thenReturn(level);
        when(level.getGameRules()).thenReturn(rules);
        when(level.getServer()).thenReturn(server);

        var context = mock(NetworkService.PacketContext.class);
        when(context.getPlayer()).thenReturn(player);
        ToggleDaylightCycleMessage.Handler.handle(new ToggleDaylightCycleMessage(), context);
        var work = ArgumentCaptor.forClass(Runnable.class);
        verify(context).queue(work.capture());
        verifyNoInteractions(player);
        work.getValue().run();

        assertEquals(!initial, rules.get(GameRules.ADVANCE_TIME));
        verify(server).onGameRuleChanged(GameRules.ADVANCE_TIME, !initial);

        // Another actor changes the rule between key presses.
        rules.set(GameRules.ADVANCE_TIME, initial, null);
        work.getValue().run();
        assertEquals(!initial, rules.get(GameRules.ADVANCE_TIME));
    }

    @Test
    void rejectsPlayersWithoutGamerulePermission() {
        var player = mock(ServerPlayer.class);
        when(player.permissions()).thenReturn(permission -> false);
        var context = mock(NetworkService.PacketContext.class);
        when(context.getPlayer()).thenReturn(player);
        doAnswer(call -> {
            call.<Runnable>getArgument(0).run();
            return null;
        }).when(context).queue(any());

        ToggleDaylightCycleMessage.Handler.handle(new ToggleDaylightCycleMessage(), context);

        verify(player, never()).level();
    }

    @Test
    void ignoresMissingPlayer() {
        var context = mock(NetworkService.PacketContext.class);
        doAnswer(call -> {
            call.<Runnable>getArgument(0).run();
            return null;
        }).when(context).queue(any());

        ToggleDaylightCycleMessage.Handler.handle(new ToggleDaylightCycleMessage(), context);

        verify(context).getPlayer();
    }
}

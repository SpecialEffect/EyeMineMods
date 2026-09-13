package com.specialeffect.eyemine.packets.messages;

import com.specialeffect.eyemine.packets.NetworkService;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AddItemToHotbarTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        // Item components normally bind during resource loading, outside this unit test.
        var components = DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE, 64).build();
        Items.STONE.builtInRegistryHolder().bindComponents(components);
        Items.DIAMOND.builtInRegistryHolder().bindComponents(components);
    }

    @Test
    void rejectsSurvivalPlayerEvenWithValidStackAndSlot() {
        var player = mock(ServerPlayer.class);

        dispatch(player, new AddItemToHotbar(new ItemStack(Items.DIAMOND), 0));

        verify(player, never()).getInventory();
    }

    @ParameterizedTest
    @ValueSource(ints = {-2, 9, 36, Integer.MAX_VALUE, Integer.MIN_VALUE})
    void rejectsSlotsOutsideTheHotbar(int slot) {
        var player = mock(ServerPlayer.class);
        when(player.isCreative()).thenReturn(true);

        dispatch(player, new AddItemToHotbar(new ItemStack(Items.STONE), slot));

        verify(player, never()).getInventory();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 65})
    void rejectsEmptyAndOversizedStacks(int count) {
        var player = mock(ServerPlayer.class);
        when(player.isCreative()).thenReturn(true);

        dispatch(player, new AddItemToHotbar(new ItemStack(Items.STONE, count), 0));

        verify(player, never()).getInventory();
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 8})
    void grantsValidCreativeStackAndSelectsRequestedSlot(int requestedSlot) {
        var player = mock(ServerPlayer.class);
        var inventory = mock(Inventory.class);
        when(player.isCreative()).thenReturn(true);
        when(player.getInventory()).thenReturn(inventory);
        when(inventory.getSuitableHotbarSlot()).thenReturn(3);
        var requestedStack = new ItemStack(Items.STONE, 64);

        dispatch(player, new AddItemToHotbar(requestedStack, requestedSlot));

        int expectedSlot = requestedSlot == -1 ? 3 : requestedSlot;
        var granted = ArgumentCaptor.forClass(ItemStack.class);
        verify(inventory).setItem(eq(expectedSlot), granted.capture());
        verify(inventory).setSelectedSlot(expectedSlot);
        assertNotSame(requestedStack, granted.getValue());
        assertTrue(ItemStack.isSameItemSameComponents(requestedStack, granted.getValue()));
        assertEquals(64, granted.getValue().getCount());
    }

    @Test
    void ignoresMissingPlayer() {
        assertDoesNotThrow(() -> dispatch(null, new AddItemToHotbar(new ItemStack(Items.STONE), 0)));
    }

    private static void dispatch(ServerPlayer player, AddItemToHotbar packet) {
        var context = mock(NetworkService.PacketContext.class);
        when(context.getPlayer()).thenReturn(player);
        AddItemToHotbar.Handler.handle(packet, context);
        var work = ArgumentCaptor.forClass(Runnable.class);
        verify(context).queue(work.capture());
        if (player != null) {
            verifyNoInteractions(player);
        }
        work.getValue().run();
    }
}

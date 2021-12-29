/**
 * Copyright (C) 2016-2020 Kirsty McNaught
 * 
 * Developed for SpecialEffect, www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.eyemine.packets.messages;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class AddItemToHotbar {
    
    private ItemStack item;
    private int slotId = -1;

    public AddItemToHotbar() { }

    public AddItemToHotbar(ItemStack item, int id) {
        this.item = item;
        this.slotId = id;
    }

    public AddItemToHotbar(ItemStack item) {
        this.item = item;
    }
    
	public static AddItemToHotbar decode(FriendlyByteBuf buf) {
        ItemStack item = buf.readItem();
        int slotId = buf.readInt();
        return new AddItemToHotbar(item, slotId);
    }

    public static void encode(AddItemToHotbar pkt, FriendlyByteBuf buf) {
    	buf.writeItem(pkt.item);
    	buf.writeInt(pkt.slotId);
    }
    
    public static class Handler {
		public static void handle(final AddItemToHotbar pkt, Supplier<NetworkManager.PacketContext> context) {
			context.get().queue(() -> {
				Player player = context.get().getPlayer();
				if (player == null) {
					return;
				}

				Inventory inventory = player.getInventory();

				if (pkt.slotId < 0) {
					pkt.slotId = inventory.getSuitableHotbarSlot();
				}
				inventory.setItem(pkt.slotId, pkt.item);
				inventory.selected = pkt.slotId;
			});
		}
	}   
}

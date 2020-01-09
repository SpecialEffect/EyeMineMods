/**
 * Copyright (C) 2016 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.messages;

import java.util.function.Supplier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

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
    
	public static AddItemToHotbar decode(PacketBuffer buf) {    	
        ItemStack item = buf.readItemStack();
        int slotId = buf.readInt();
        return new AddItemToHotbar(item, slotId);
    }

    public static void encode(AddItemToHotbar pkt, PacketBuffer buf) {
    	buf.writeItemStack(pkt.item);
    	buf.writeInt(pkt.slotId);       
    }
    
    public static class Handler {
		public static void handle(final AddItemToHotbar pkt, Supplier<NetworkEvent.Context> ctx) {
			PlayerEntity player = ctx.get().getSender();
	        if (player == null) {
	            return;
	        }       
	        
            PlayerInventory inventory = player.inventory;
            
            if (pkt.slotId < 0) {
            	pkt.slotId = inventory.getBestHotbarSlot();
            }
    		inventory.setInventorySlotContents(pkt.slotId, pkt.item);
    		inventory.currentItem = pkt.slotId;
	            
			ctx.get().setPacketHandled(true);
		}
	}   
}

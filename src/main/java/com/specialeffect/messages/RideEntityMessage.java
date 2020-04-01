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

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;

public class RideEntityMessage {
    
    private int entityId;
    private Hand hand;
    
    public RideEntityMessage(int entityId, Hand hand) { 
    	this.entityId = entityId;
    	this.hand = hand;
    }
    
	public static RideEntityMessage decode(PacketBuffer buf) {		
        return new RideEntityMessage(buf.readInt(), buf.readEnumValue(Hand.class));
    }

    public static void encode(RideEntityMessage pkt, PacketBuffer buf) {
    	buf.writeInt(pkt.entityId);
    	buf.writeEnumValue(pkt.hand);
    }    

    public static class Handler {
		public static void handle(final RideEntityMessage pkt, Supplier<NetworkEvent.Context> ctx) {
			PlayerEntity player = ctx.get().getSender();
	        if (player == null) {
	            return;
	        }       

	        Entity entity = player.world.getEntityByID(pkt.entityId);
	        if (entity != null) {
				entity.processInitialInteract(player, pkt.hand);
	        }
		}
	}       
}

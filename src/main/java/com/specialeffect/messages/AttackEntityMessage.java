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

package com.specialeffect.messages;

import java.util.function.Supplier;


import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class AttackEntityMessage {
    
    private int entityId = -1;

    public AttackEntityMessage() { }

    public AttackEntityMessage(Entity entity) {
    	this.entityId = entity.getEntityId();
    }    
    
    public AttackEntityMessage(int entityId) {
    	this.entityId = entityId;
    }    

	public static AttackEntityMessage decode(PacketBuffer buf) {    	
		int entityId = buf.readInt();
        return new AttackEntityMessage(entityId);
    }

    public static void encode(AttackEntityMessage pkt, PacketBuffer buf) {
    	buf.writeInt(pkt.entityId);       
    }

    public static class Handler {
		public static void handle(final AttackEntityMessage pkt, Supplier<NetworkEvent.Context> ctx) {
			PlayerEntity player = ctx.get().getSender();
	        if (player == null) {
	            return;
	        }       
	        
	        Entity targetEntity = player.world.
                    getEntityByID(pkt.entityId);
	        
            if (null != targetEntity) {
            	player.attackTargetEntityWithCurrentItem(targetEntity);
            }
		}
	}       
}

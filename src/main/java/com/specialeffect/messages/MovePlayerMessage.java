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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent;

public class MovePlayerMessage {
    
	private double x;
	private double y;
	private double z;
	
    public MovePlayerMessage(Vector3d newMotion) {
    	x = newMotion.x;
    	y = newMotion.y;
    	z = newMotion.z;
    }
    
	public static MovePlayerMessage decode(PacketBuffer buf) {   
		double x = buf.readDouble();
		double y = buf.readDouble();
		double z = buf.readDouble();
        return new MovePlayerMessage(new Vector3d(x, y, z));
    }

    public static void encode(MovePlayerMessage pkt, PacketBuffer buf) {
    	buf.writeDouble(pkt.x);
    	buf.writeDouble(pkt.y);
    	buf.writeDouble(pkt.z);
    }    

    public static class Handler {
		public static void handle(final MovePlayerMessage pkt, Supplier<NetworkEvent.Context> ctx) {
			PlayerEntity player = ctx.get().getSender();
	        if (player == null) {
	            return;
	        }       
	        	        
	        player.setMotion(new Vector3d(pkt.x, pkt.y, pkt.z));
		}
	}       
}

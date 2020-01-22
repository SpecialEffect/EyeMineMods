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
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class SetPositionAndRotationMessage {
    
	private String playerName;
	private double x;
	private double y;
	private double z;
	private float yaw;
	private float pitch;
	
    public SetPositionAndRotationMessage(String playerName, 
    									 double x, double y, double z,
    									 float yaw, float pitch) {
    	this.playerName = playerName;
    	this.x = x;
    	this.y = y;
    	this.z = z;
    	this.yaw = yaw;
    	this.pitch = pitch;
    }

    
    public static SetPositionAndRotationMessage decode(PacketBuffer buf) {
    	String playerName = buf.readString();
    	double x = buf.readDouble();
    	double y = buf.readDouble();
    	double z = buf.readDouble();
    	float yaw = buf.readFloat();
    	float pitch = buf.readFloat();
    	return new SetPositionAndRotationMessage(playerName, x, y, z, yaw, pitch);
    }
    
    public static void encode(SetPositionAndRotationMessage pkt, PacketBuffer buf) {
    	buf.writeString(pkt.playerName);
        buf.writeDouble(pkt.x);
        buf.writeDouble(pkt.y);
        buf.writeDouble(pkt.z);
        buf.writeFloat(pkt.yaw);
        buf.writeFloat(pkt.pitch);
    }

    public static class Handler {
		public static void handle(final SetPositionAndRotationMessage pkt, Supplier<NetworkEvent.Context> ctx) {
			PlayerEntity player = ctx.get().getSender();
	        if (player == null) {
	            return;
	        }       
	        System.out.println("SetPositionAndRotationMessage start");
	        
	        //FIXME: test this, not currrently using playername, is redundant?
        	player.setPositionAndRotation(pkt.x, pkt.y, pkt.z,
        			pkt.yaw, pkt.pitch);
            player.setPositionAndUpdate(pkt.x, pkt.y, pkt.z);
            
            System.out.println("SetPositionAndRotationMessage end");
            
		}
	}
        
}

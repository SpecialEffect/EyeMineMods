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
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class TeleportPlayerMessage {
    
	private BlockPos pos;
	
    public TeleportPlayerMessage(BlockPos pos) { 
    	this.pos = pos;
    }
    
	public static TeleportPlayerMessage decode(PacketBuffer buf) {   
    	BlockPos blockPos = buf.readBlockPos();
    	return new TeleportPlayerMessage(blockPos);
    }

    public static void encode(TeleportPlayerMessage pkt, PacketBuffer buf) {
    	buf.writeBlockPos(pkt.pos);
    }    

    public static class Handler {
		public static void handle(final TeleportPlayerMessage pkt, Supplier<NetworkEvent.Context> ctx) {
			PlayerEntity player = ctx.get().getSender();
	        if (player == null) {
	            return;
	        }       
	        	                     
            BlockPos pos = pkt.pos;
            player.setPositionAndUpdate(pos.getX(), pos.getY(), pos.getZ());
       }
	}       
}

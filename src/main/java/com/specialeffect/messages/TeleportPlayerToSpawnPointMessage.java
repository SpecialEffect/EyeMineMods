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
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class TeleportPlayerToSpawnPointMessage {
    
    public TeleportPlayerToSpawnPointMessage() { 
    }
    
	public static TeleportPlayerToSpawnPointMessage decode(PacketBuffer buf) {   
    	return new TeleportPlayerToSpawnPointMessage();
    }

    public static void encode(TeleportPlayerToSpawnPointMessage pkt, PacketBuffer buf) {
    }    

    public static class Handler {
		public static void handle(final TeleportPlayerToSpawnPointMessage pkt, Supplier<NetworkEvent.Context> ctx) {
			PlayerEntity player = ctx.get().getSender();
	        if (player == null) {
	            return;
	        }       

            World world = player.getEntityWorld();
            
            BlockPos pos = player.getBedLocation(player.dimension);
            if (null == pos) {
            	pos = world.getSpawnPoint();
            }
            
            player.setPositionAndUpdate(pos.getX(), pos.getY(), pos.getZ());
       }
	}       
}

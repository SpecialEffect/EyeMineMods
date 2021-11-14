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

import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import java.util.function.Supplier;

public class TeleportPlayerToSpawnPointMessage {
    
    public TeleportPlayerToSpawnPointMessage() { 
    }
    
	public static TeleportPlayerToSpawnPointMessage decode(FriendlyByteBuf buf) {
    	return new TeleportPlayerToSpawnPointMessage();
    }

    public static void encode(TeleportPlayerToSpawnPointMessage pkt, FriendlyByteBuf buf) {
    }    

    public static class Handler {
		public static void handle(final TeleportPlayerToSpawnPointMessage pkt, Supplier<NetworkManager.PacketContext> context) {
			context.get().queue(() -> {
				Player player = context.get().getPlayer();
				if (player == null) {
					return;
				}

				ServerLevel world = (ServerLevel)player.level;

				BlockPos pos; //TODO: Find out if there's a replacement for the dimension sensitive getBedLocation since it's gone
				if (player.getSleepingPos().isPresent()) {
					pos = player.getSleepingPos().get();
				} else {
					pos = world.getSharedSpawnPos();
				}
//            BlockPos pos = player.getBedLocation(player.dimension);
//            if (null == pos) {
//            	pos = world.getSpawnPoint();
//            }

				player.setPos(pos.getX(), pos.getY(), pos.getZ());
			});
       }
	}       
}

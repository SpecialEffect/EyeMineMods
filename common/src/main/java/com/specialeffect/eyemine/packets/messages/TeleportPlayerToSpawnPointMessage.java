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
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
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

				if(!player.level.isClientSide) {
					MinecraftServer server = player.getServer();
					ServerPlayer serverPlayer = (ServerPlayer) player;
					ServerLevel respawnDimension = server.getLevel(serverPlayer.getRespawnDimension());
					BlockPos respawnPos = serverPlayer.getRespawnPosition();
					float respawnAngle = serverPlayer.getRespawnAngle();
					Optional<Vec3> optional;
					if (serverPlayer != null && respawnPos != null) {
						optional = Player.findRespawnPositionAndUseSpawnBlock(respawnDimension, respawnPos, respawnAngle, false, false);
					} else {
						optional = Optional.empty();
					}

					if (optional.isPresent()) {
						BlockState state = respawnDimension.getBlockState(respawnPos);
						boolean blockIsRespawnAnchor = state.is(Blocks.RESPAWN_ANCHOR);
						Vec3 vector3d = optional.get();
						float f1;
						if (!state.is(BlockTags.BEDS) && !blockIsRespawnAnchor) {
							f1 = respawnAngle;
						} else {
							Vec3 vector3d1 = Vec3.atBottomCenterOf(respawnPos).subtract(vector3d).normalize();
							f1 = (float) Mth.wrapDegrees(Mth.atan2(vector3d1.z, vector3d1.x) * (double) (180F / (float) Math.PI) - 90.0D);
						}
						serverPlayer.moveTo(vector3d.x, vector3d.y, vector3d.z, f1, 0.0F);
						serverPlayer.setRespawnPosition(respawnDimension.dimension(), new BlockPos(vector3d), respawnAngle, false, false);
					}
				}
			});
       }
	}       
}

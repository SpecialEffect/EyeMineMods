/**
 * Copyright (C) 2016-2020 Kirsty McNaught
 * <p>
 * Developed for SpecialEffect, www.specialeffect.org.uk
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.eyemine.packets.messages;

import com.specialeffect.eyemine.EyeMine;
import com.specialeffect.eyemine.packets.NetworkService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;

public record TeleportPlayerToSpawnPointMessage() implements CustomPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, TeleportPlayerToSpawnPointMessage> CODEC = CustomPacketPayload.codec(
			TeleportPlayerToSpawnPointMessage::write,
			TeleportPlayerToSpawnPointMessage::new);
	public static final CustomPacketPayload.Type<TeleportPlayerToSpawnPointMessage> ID = new CustomPacketPayload.Type<>(
			Identifier.fromNamespaceAndPath(EyeMine.MOD_ID, "teleport_to_spawn_point"));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}


	public TeleportPlayerToSpawnPointMessage(FriendlyByteBuf buf) {
		this();
	}

	public void write(FriendlyByteBuf buf) {
	}

	public static class Handler {
		public static void handle(final TeleportPlayerToSpawnPointMessage pkt, NetworkService.PacketContext context) {
			context.queue(() -> {
				Player player = context.getPlayer();
				if (player == null) {
					return;
				}

				if (!player.level().isClientSide()) {
					MinecraftServer server = player.level().getServer();
					ServerPlayer serverPlayer = (ServerPlayer) player;
					ServerPlayer.RespawnConfig respawnConfig = serverPlayer.getRespawnConfig();
					TeleportTransition transition;
					if (respawnConfig != null) {
						LevelData.RespawnData respawnData = respawnConfig.respawnData();
						BlockPos respawnPos = respawnData.pos();
						float respawnAngle = respawnData.yaw();
						ServerLevel respawnDimension = server.getLevel(respawnData.dimension());
						transition = serverPlayer.findRespawnPositionAndUseSpawnBlock(false, TeleportTransition.DO_NOTHING);

						if (transition != null && respawnDimension != null) {
							BlockState state = respawnDimension.getBlockState(respawnPos);
							boolean blockIsRespawnAnchor = state.is(Blocks.RESPAWN_ANCHOR);
							Vec3 vector3d = transition.position();
							float f1;
							if (!state.is(BlockTags.BEDS) && !blockIsRespawnAnchor) {
								f1 = respawnAngle;
							} else {
								Vec3 vector3d1 = Vec3.atBottomCenterOf(respawnPos).subtract(vector3d).normalize();
								f1 = (float) Mth.wrapDegrees(Mth.atan2(vector3d1.z, vector3d1.x) * (double) (180F / (float) Math.PI) - 90.0D);
							}
							serverPlayer.teleportTo(respawnDimension, vector3d.x, vector3d.y, vector3d.z, java.util.Set.of(), f1, 0.0F, false);
							serverPlayer.setRespawnPosition(
								new ServerPlayer.RespawnConfig(
									LevelData.RespawnData.of(respawnDimension.dimension(), BlockPos.containing(vector3d), respawnAngle, 0.0F),
									false
								), false);
						}
					}
				}
			});
		}
	}
}

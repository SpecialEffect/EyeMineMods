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
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public record GatherBlockMessage(int entityId) implements CustomPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, GatherBlockMessage> CODEC = StreamCodec.composite(
			ByteBufCodecs.INT,
			p -> p.entityId,
			GatherBlockMessage::new
	);
	public static final CustomPacketPayload.Type<GatherBlockMessage> ID = new CustomPacketPayload.Type<>(
			Identifier.fromNamespaceAndPath(EyeMine.MOD_ID, "gather_block"));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}

	public static class Handler {
		public static void handle(final GatherBlockMessage pkt, NetworkService.PacketContext context) {
			context.queue(() -> {
				Player player = context.getPlayer();
				if (player == null) {
					return;
				}

				Level level = player.level();
				Entity target = level.getEntity(pkt.entityId);
				if (target != null && target instanceof ItemEntity) {
					// Move item next to player to be picked up automatically
					target.setPos(player.getX(), player.getY() + 0.5, player.getZ());
				}
			});
		}
	}
}

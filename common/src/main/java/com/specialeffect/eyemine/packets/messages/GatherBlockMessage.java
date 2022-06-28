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

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class GatherBlockMessage {

	private int entityId = 0;

	public GatherBlockMessage() {
	}

	public GatherBlockMessage(int id) {
		this.entityId = id;
	}

	public static GatherBlockMessage decode(FriendlyByteBuf buf) {
		int entityId = buf.readInt();
		return new GatherBlockMessage(entityId);
	}

	public static void encode(GatherBlockMessage pkt, FriendlyByteBuf buf) {
		buf.writeInt(pkt.entityId);
	}

	public static class Handler {
		public static void handle(final GatherBlockMessage pkt, Supplier<NetworkManager.PacketContext> context) {
			context.get().queue(() -> {
				Player player = context.get().getPlayer();
				if (player == null) {
					return;
				}

				Level level = player.getCommandSenderWorld();
				Entity target = level.getEntity(pkt.entityId);
				if (target != null && target instanceof ItemEntity) {
					// Move item next to player to be picked up automatically
					target.setPos(player.getX(), player.getY() + 0.5, player.getZ());
				}
			});
		}
	}
}

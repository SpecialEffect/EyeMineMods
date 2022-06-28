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
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

public class ChangeFlyingStateMessage {

	private boolean shouldBeFlying;
	private int flyHeight;

	public ChangeFlyingStateMessage() {
	}

	public ChangeFlyingStateMessage(boolean shouldBeFlying,
									int flyHeight) {
		this.shouldBeFlying = shouldBeFlying;
		this.flyHeight = flyHeight;
	}


	public static ChangeFlyingStateMessage decode(FriendlyByteBuf buf) {
		boolean shouldBeFlying = buf.readBoolean();
		int flyHeight = buf.readInt();
		return new ChangeFlyingStateMessage(shouldBeFlying, flyHeight);
	}

	public static void encode(ChangeFlyingStateMessage pkt, FriendlyByteBuf buf) {
		buf.writeBoolean(pkt.shouldBeFlying);
		buf.writeInt(pkt.flyHeight);
	}

	public static class Handler {
		public static void handle(final ChangeFlyingStateMessage pkt, Supplier<NetworkManager.PacketContext> context) {
			context.get().queue(() -> {
				Player player = context.get().getPlayer();
				if (player == null) {
					return;
				}

				if (player.getAbilities().flying) {
					if (pkt.shouldBeFlying) {
						player.getAbilities().flying = true;
						Vec3 motion = player.getDeltaMovement();
						Vec3 addMotion = new Vec3(0, pkt.flyHeight, 0);
						player.setDeltaMovement(motion.add(addMotion));
						player.move(MoverType.SELF, new Vec3(0, pkt.flyHeight, 0));
					} else {
						player.getAbilities().flying = false;
					}
				}
			});
		}
	}
}

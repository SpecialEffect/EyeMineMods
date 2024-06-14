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
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public record ChangeFlyingStateMessage(boolean shouldBeFlying, int flyHeight) implements CustomPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, ChangeFlyingStateMessage> CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL,
			p -> p.shouldBeFlying,
			ByteBufCodecs.INT,
			p -> p.flyHeight,
			ChangeFlyingStateMessage::new
	);
	public static final CustomPacketPayload.Type<ChangeFlyingStateMessage> ID = new CustomPacketPayload.Type<>(
			ResourceLocation.fromNamespaceAndPath(EyeMine.MOD_ID, "change_flying_state"));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}

	public static class Handler {
		public static void handle(final ChangeFlyingStateMessage pkt, NetworkManager.PacketContext context) {
			context.queue(() -> {
				Player player = context.getPlayer();
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

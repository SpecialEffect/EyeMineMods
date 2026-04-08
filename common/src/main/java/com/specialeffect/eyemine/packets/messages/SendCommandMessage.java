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
import net.minecraft.commands.Commands;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;

public record SendCommandMessage(String command) implements CustomPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, SendCommandMessage> CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8,
			p -> p.command,
			SendCommandMessage::new
	);
	public static final CustomPacketPayload.Type<SendCommandMessage> ID = new CustomPacketPayload.Type<>(
			Identifier.fromNamespaceAndPath(EyeMine.MOD_ID, "send_command"));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
	
	public static class Handler {
		public static void handle(final SendCommandMessage pkt, NetworkService.PacketContext context) {
			context.queue(() -> {
				MinecraftServer server = context.getPlayer().level().getServer();
				if (server == null) {
					EyeMine.LOGGER.warn("Server is null, cannot send command");
				} else {
					Commands mgr = server.getCommands();
					if (null == mgr) {
						EyeMine.LOGGER.warn("CommandManager is null, cannot send command");
					} else {
						mgr.performPrefixedCommand(server.createCommandSourceStack(), pkt.command);
					}
				}
			});
		}
	}
}


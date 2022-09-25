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
import net.minecraft.commands.Commands;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;

import java.util.function.Supplier;

public class SendCommandMessage {
	private String command;

	public SendCommandMessage() {
	}

	public SendCommandMessage(String command) {
		this.command = command;
	}

	public static SendCommandMessage decode(FriendlyByteBuf buf) {
		String command = buf.readUtf();
		return new SendCommandMessage(command);
	}

	public static void encode(SendCommandMessage pkt, FriendlyByteBuf buf) {
		buf.writeUtf(pkt.command);
	}

	public static class Handler {
		public static void handle(final SendCommandMessage pkt, Supplier<NetworkManager.PacketContext> context) {
			context.get().queue(() -> {
				MinecraftServer server = context.get().getPlayer().getServer();
				if (server == null) {
					System.out.println("Server is null, cannot send command");
				} else {
					Commands mgr = server.getCommands();
					if (null == mgr) {
						System.out.println("CommandManager is null, cannot send command");
					} else {
						mgr.performPrefixedCommand(server.createCommandSourceStack(), pkt.command);
					}
				}
			});
		}
	}
}


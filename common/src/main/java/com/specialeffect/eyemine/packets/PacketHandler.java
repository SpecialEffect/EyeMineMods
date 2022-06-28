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

package com.specialeffect.eyemine.packets;

import com.specialeffect.eyemine.EyeMine;
import com.specialeffect.eyemine.packets.messages.ActivateBlockAtPosition;
import com.specialeffect.eyemine.packets.messages.AddItemToHotbar;
import com.specialeffect.eyemine.packets.messages.ChangeFlyingStateMessage;
import com.specialeffect.eyemine.packets.messages.GatherBlockMessage;
import com.specialeffect.eyemine.packets.messages.SendCommandMessage;
import com.specialeffect.eyemine.packets.messages.TeleportPlayerToSpawnPointMessage;
import dev.architectury.networking.NetworkChannel;
import net.minecraft.resources.ResourceLocation;

public class PacketHandler {
	private static final String PROTOCOL_VERSION = "1";
//	public static final Predicate<String> validator = v -> PROTOCOL_VERSION.equals(v) || NetworkRegistry.ABSENT.equals(v) || NetworkRegistry.ACCEPTVANILLA.equals(v);
//	public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder.named(new ResourceLocation("specialeffect", EyeMine.MOD_ID))
//			.clientAcceptedVersions(validator)
//			.serverAcceptedVersions(validator)
//			.networkProtocolVersion(() -> PROTOCOL_VERSION)
//			.simpleChannel();


	public static final NetworkChannel CHANNEL = NetworkChannel.create(new ResourceLocation("specialeffect", EyeMine.MOD_ID));

	public static void init() {
		CHANNEL.register(AddItemToHotbar.class, AddItemToHotbar::encode, AddItemToHotbar::decode,
				AddItemToHotbar.Handler::handle);

		CHANNEL.register(SendCommandMessage.class, SendCommandMessage::encode,
				SendCommandMessage::decode, SendCommandMessage.Handler::handle);

		CHANNEL.register(TeleportPlayerToSpawnPointMessage.class, TeleportPlayerToSpawnPointMessage::encode,
				TeleportPlayerToSpawnPointMessage::decode, TeleportPlayerToSpawnPointMessage.Handler::handle);

		CHANNEL.register(GatherBlockMessage.class, GatherBlockMessage::encode,
				GatherBlockMessage::decode, GatherBlockMessage.Handler::handle);

		CHANNEL.register(ActivateBlockAtPosition.class, ActivateBlockAtPosition::encode,
				ActivateBlockAtPosition::decode, ActivateBlockAtPosition.Handler::handle);

		CHANNEL.register(ChangeFlyingStateMessage.class, ChangeFlyingStateMessage::encode,
				ChangeFlyingStateMessage::decode, ChangeFlyingStateMessage.Handler::handle);
	}
}

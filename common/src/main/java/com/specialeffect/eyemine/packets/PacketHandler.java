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

import com.specialeffect.eyemine.packets.messages.ActivateBlockAtPosition;
import com.specialeffect.eyemine.packets.messages.AddItemToHotbar;
import com.specialeffect.eyemine.packets.messages.ChangeFlyingStateMessage;
import com.specialeffect.eyemine.packets.messages.GatherBlockMessage;
import com.specialeffect.eyemine.packets.messages.SendCommandMessage;
import com.specialeffect.eyemine.packets.messages.TeleportPlayerToSpawnPointMessage;
import dev.architectury.networking.NetworkManager;

public class PacketHandler {
	private static final String PROTOCOL_VERSION = "1";
//	public static final Predicate<String> validator = v -> PROTOCOL_VERSION.equals(v) || NetworkRegistry.ABSENT.equals(v) || NetworkRegistry.ACCEPTVANILLA.equals(v);
//	public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder.named(ResourceLocation.fromNamespaceAndPath("specialeffect", EyeMine.MOD_ID))
//			.clientAcceptedVersions(validator)
//			.serverAcceptedVersions(validator)
//			.networkProtocolVersion(() -> PROTOCOL_VERSION)
//			.simpleChannel();


//	public static final NetworkChannel CHANNEL = NetworkChannel.create(ResourceLocation.fromNamespaceAndPath("specialeffect", EyeMine.MOD_ID));

	public static void init() {
		NetworkManager.registerReceiver(NetworkManager.c2s(), AddItemToHotbar.ID, AddItemToHotbar.CODEC, AddItemToHotbar.Handler::handle);
//		CHANNEL.register(AddItemToHotbar.class, AddItemToHotbar::write, AddItemToHotbar::decode,
//				AddItemToHotbar.Handler::handle);

		NetworkManager.registerReceiver(NetworkManager.c2s(), SendCommandMessage.ID, SendCommandMessage.CODEC, SendCommandMessage.Handler::handle);
//		CHANNEL.register(SendCommandMessage.class, SendCommandMessage::encode,
//				SendCommandMessage::decode, SendCommandMessage.Handler::handle);

		NetworkManager.registerReceiver(NetworkManager.c2s(), TeleportPlayerToSpawnPointMessage.ID, TeleportPlayerToSpawnPointMessage.CODEC, TeleportPlayerToSpawnPointMessage.Handler::handle);
//		CHANNEL.register(TeleportPlayerToSpawnPointMessage.class, TeleportPlayerToSpawnPointMessage::encode,
//				TeleportPlayerToSpawnPointMessage::decode, TeleportPlayerToSpawnPointMessage.Handler::handle);

		NetworkManager.registerReceiver(NetworkManager.c2s(), GatherBlockMessage.ID, GatherBlockMessage.CODEC, GatherBlockMessage.Handler::handle);
//		CHANNEL.register(GatherBlockMessage.class, GatherBlockMessage::encode,
//				GatherBlockMessage::decode, GatherBlockMessage.Handler::handle);

		NetworkManager.registerReceiver(NetworkManager.c2s(), ActivateBlockAtPosition.ID, ActivateBlockAtPosition.CODEC, ActivateBlockAtPosition.Handler::handle);
//		CHANNEL.register(ActivateBlockAtPosition.class, ActivateBlockAtPosition::encode,
//				ActivateBlockAtPosition::decode, ActivateBlockAtPosition.Handler::handle);

		NetworkManager.registerReceiver(NetworkManager.c2s(), ChangeFlyingStateMessage.ID, ChangeFlyingStateMessage.CODEC, ChangeFlyingStateMessage.Handler::handle);
//		CHANNEL.register(ChangeFlyingStateMessage.class, ChangeFlyingStateMessage::encode,
//				ChangeFlyingStateMessage::decode, ChangeFlyingStateMessage.Handler::handle);
	}
}

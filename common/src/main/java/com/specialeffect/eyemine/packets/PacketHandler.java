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
import com.specialeffect.eyemine.platform.Services;

public class PacketHandler {
	public static void init() {
		NetworkService net = Services.NETWORK;

		net.registerC2S(AddItemToHotbar.ID, AddItemToHotbar.CODEC, AddItemToHotbar.Handler::handle);
		net.registerC2S(SendCommandMessage.ID, SendCommandMessage.CODEC, SendCommandMessage.Handler::handle);
		net.registerC2S(TeleportPlayerToSpawnPointMessage.ID, TeleportPlayerToSpawnPointMessage.CODEC, TeleportPlayerToSpawnPointMessage.Handler::handle);
		net.registerC2S(GatherBlockMessage.ID, GatherBlockMessage.CODEC, GatherBlockMessage.Handler::handle);
		net.registerC2S(ActivateBlockAtPosition.ID, ActivateBlockAtPosition.CODEC, ActivateBlockAtPosition.Handler::handle);
		net.registerC2S(ChangeFlyingStateMessage.ID, ChangeFlyingStateMessage.CODEC, ChangeFlyingStateMessage.Handler::handle);
	}
}

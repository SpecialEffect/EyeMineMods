/**
 * Copyright (C) 2016-2020 Kirsty McNaught
 * 
 * Developed for SpecialEffect, www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.mods;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Predicate;

public class ChildMod {
	
    // Directly reference a log4j logger.	
    @SuppressWarnings("unused")
	protected static final Logger LOGGER = LogManager.getLogger();
    protected SimpleChannel channel;

	public void setup(final FMLCommonSetupEvent event) {
		// can be overridden if mod has setup to do
	}
	
	protected void setupChannel(String modid, int protocolVersion) {
		String PROTOCOL_VERSION = Integer.toString(protocolVersion);
		Predicate<String> validator = v -> PROTOCOL_VERSION.equals(v) || NetworkRegistry.ABSENT.equals(v) || NetworkRegistry.ACCEPTVANILLA.equals(v);

		channel = NetworkRegistry.ChannelBuilder.named(new ResourceLocation("specialeffect", modid))
				.clientAcceptedVersions(validator)
				.serverAcceptedVersions(validator)
				.networkProtocolVersion(() -> PROTOCOL_VERSION)
				.simpleChannel();
	}
	
//	public void onKeyInput(KeyInputEvent event);    	
//	public void onLiving(LivingUpdateEvent event);
//	public void updateIcons();
//	public void syncConfig();
}

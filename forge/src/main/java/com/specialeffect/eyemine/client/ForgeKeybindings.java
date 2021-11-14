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

package com.specialeffect.eyemine.client;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ForgeKeybindings {
	public static void registerKeybinds(final FMLClientSetupEvent event) {
		if(!Keybindings.keybindings.isEmpty()) {
			for(KeyMapping keyBinding : Keybindings.keybindings) {
				ClientRegistry.registerKeyBinding(keyBinding);
			}
		}
	}
}

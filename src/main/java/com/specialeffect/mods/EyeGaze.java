/**
 * Copyright (C) 2018 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.mods;

import java.util.ArrayList;
import java.util.List;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.utils.ModUtils;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.config.ModConfig;

// This mod is purely a wrapper to cluster all our smaller mods.
// The parent (this mod) handles configuration so that it can all
// be changed by the user in one place.

@Mod(EyeGaze.MODID)
public class EyeGaze {
	/*
	 * In v1.11.2, this mod was a wrapper that contained user-exposed config, and
	 * acted as the 'parent' mod to all other mods This allowed us to expose config
	 * UI in one place, and not clutter up the Mods List (while simultaneously
	 * letting us have small self- contained mods for different features). In v1.14,
	 * the parent->child relationship doesn't exist any more, so this is a bit of a
	 * ghost town. We might consider updating it as our own uber-mod, with other
	 * mods registered to it.
	 * 
	 * FIXME: figure out what to do with it architecturally :-)
	 */

	public static final String MODID = "eyemine";
	public static final String VERSION = ModUtils.VERSION;
	public static final String NAME = "Eye Mine";

	public static EyeMineConfig mConfig;

	// Category names for clustering config options in different UIs
	private static List<ChildModWithConfig> childrenWithConfig = new ArrayList<ChildModWithConfig>();

	public EyeGaze() {

		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);

		// Config setup
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, EyeMineConfig.CLIENT_CONFIG);
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, EyeMineConfig.COMMON_CONFIG);

		EyeMineConfig.loadConfig(EyeMineConfig.CLIENT_CONFIG,
				FMLPaths.CONFIGDIR.get().resolve("eyegaze-client.toml"));
		EyeMineConfig.loadConfig(EyeMineConfig.COMMON_CONFIG,
				FMLPaths.CONFIGDIR.get().resolve("eyegaze-common.toml"));
	}

	public synchronized static void registerForConfigUpdates(ChildModWithConfig mod) {

		// Make sure it gets any changes thus far
		mod.syncConfig();

		// Make sure it gets future changes
		childrenWithConfig.add(mod);

	}

	public static void refresh() {
		// TODO: should we use IMC for synchronous comms?
		for (ChildModWithConfig child : childrenWithConfig) {
			child.syncConfig();
		}
	}

	public static void saveWalkingSpeed(float speed) {
		// FIXME: put this on EyeMineConfig class?
		EyeMineConfig.customSpeedFactor.set((double) speed);

		EyeGaze.refresh();
	}

}

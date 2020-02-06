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
import com.specialeffect.inventory.CreativeTabs;
import com.specialeffect.mods.mining.ContinuouslyMine;
import com.specialeffect.mods.mining.GatherDrops;
import com.specialeffect.mods.mining.MineOne;
import com.specialeffect.mods.misc.AutoOpenDoors;
import com.specialeffect.mods.misc.AutoPillar;
import com.specialeffect.mods.misc.ContinuouslyAttack;
import com.specialeffect.mods.misc.OpenChat;
import com.specialeffect.mods.misc.OpenTablesChests;
import com.specialeffect.mods.misc.PickBlock;
import com.specialeffect.mods.misc.QuickCommands;
import com.specialeffect.mods.misc.SwapMinePlace;
import com.specialeffect.mods.misc.UseItem;
import com.specialeffect.mods.mousehandling.MouseHandler;
import com.specialeffect.mods.moving.AutoFly;
import com.specialeffect.mods.moving.AutoJump;
import com.specialeffect.mods.moving.Dismount;
import com.specialeffect.mods.moving.EasyLadderClimb;
import com.specialeffect.mods.moving.MoveWithGaze;
import com.specialeffect.mods.moving.Sneak;
import com.specialeffect.mods.moving.Swim;
import com.specialeffect.mods.utils.DebugAverageFps;
import com.specialeffect.mods.utils.ModStateGui;
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
	private static List<ChildMod> children = new ArrayList<ChildMod>();
	
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
		
		// Setup all other mods
		this.instantiateChildren();
	}
	
	private void instantiateChildren() {
		// In older versions of forge we had child/parent mods, here we have to set this up manually
		//children.add((ChildMod) new CreativeTabs()); // will be own mod, because it requires config
		children.add((ChildMod) new ContinuouslyMine());
		children.add((ChildMod) new GatherDrops());
		children.add((ChildMod) new MineOne());
		children.add((ChildMod) new AutoOpenDoors());
		children.add((ChildMod) new AutoPillar());
		children.add((ChildMod) new ContinuouslyAttack());
		children.add((ChildMod) new OpenChat());
		children.add((ChildMod) new OpenTablesChests());
		children.add((ChildMod) new PickBlock());
		children.add((ChildMod) new QuickCommands());
		children.add((ChildMod) new SwapMinePlace());
		children.add((ChildMod) new UseItem());
		children.add((ChildMod) new MouseHandler());
		children.add((ChildMod) new AutoFly());
		children.add((ChildMod) new AutoJump());
		children.add((ChildMod) new Dismount());
		children.add((ChildMod) new EasyLadderClimb());
		children.add((ChildMod) new MoveWithGaze());
		children.add((ChildMod) new Sneak());
		children.add((ChildMod) new Swim());
		children.add((ChildMod) new DebugAverageFps());
		children.add((ChildMod) new ModStateGui());		
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

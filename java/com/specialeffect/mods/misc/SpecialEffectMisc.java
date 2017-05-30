/**
 * Copyright (C) 2016 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.mods.misc;

import java.util.ArrayList;
import java.util.List;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.utils.ModUtils;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

// This mod is purely a wrapper to cluster a few
// child mods. The parent (this) handles config
// so that it can be changed all in one place.
@Mod(modid = SpecialEffectMisc.MODID, 
  	 version = ModUtils.VERSION, 
	 name = SpecialEffectMisc.NAME, 
	 guiFactory = "com.specialeffect.gui.GuiFactoryMisc")
public class SpecialEffectMisc extends BaseClassWithCallbacks {

	public static final String MODID = "specialeffect.misc";	
	public static final String NAME = "SpecialEffectsMisc";

	public static Configuration mConfig;	

	// AutoOpenDoors
    public static int mRadiusDoors = 3;
	
    // OpenTablesChests
    public static int mRadiusChests = 5;
    
    private static List<ChildModWithConfig> childrenWithConfig = new ArrayList<ChildModWithConfig>();
    
    public static void registerForConfigUpdates(ChildModWithConfig mod) {
    	
    	// Make sure it gets any changes thus far
    	mod.syncConfig();
    	
    	// Make sure it gets future changes
    	childrenWithConfig.add(mod);
    }
    
    
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(this);

		ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"A selection of utilities required for other SpecialEffect mods");

		// Set up config
		mConfig = new Configuration(event.getSuggestedConfigurationFile());
		this.syncConfig();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// Subscribe to event buses
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if (eventArgs.modID.equals(this.MODID)) {
			syncConfig();
		}
		for (ChildModWithConfig child : childrenWithConfig) {
			child.syncConfig();
		}
	}
	
	public static void syncConfig() {
		// AutoOpenDoors
		mRadiusDoors = mConfig.getInt("Distance to open doors", Configuration.CATEGORY_GENERAL, mRadiusDoors, 
				1, 20, "How far away a player needs to be from a door to automatically open/close");

		// OpenTablesChests
		// This is limited to 6 blocks since the gui doesn't appear if block is too far away
		mRadiusChests = mConfig.getInt("Distance to open chests/crafting tables", Configuration.CATEGORY_GENERAL, mRadiusChests, 
				1, 6, "How far away a player needs to be from a chest/table to be able to open it");
        
		if (mConfig.hasChanged()) {
			mConfig.save();
		}
	}

}

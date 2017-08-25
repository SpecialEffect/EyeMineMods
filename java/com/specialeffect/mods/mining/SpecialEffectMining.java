/**
 * Copyright (C) 2016 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.mods.mining;

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
// child mods. It doesn't even have any config.
@Mod(modid = SpecialEffectMining.MODID, 
  	 version = ModUtils.VERSION, 
	 name = SpecialEffectMining.NAME, 
	 guiFactory = "com.specialeffect.gui.GuiFactoryMining")
public class SpecialEffectMining extends BaseClassWithCallbacks {

	public static final String MODID = "specialeffect.mining";	
	public static final String NAME = "SpecialEffectMining";

	public static Configuration mConfig;	

	public static boolean mAutoSelectTool;
	
    private static List<ChildModWithConfig> childrenWithConfig = new ArrayList<ChildModWithConfig>();
    
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(this);
		
		ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"A selection of mods for accessible mining methods");

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
	
    public static void registerForConfigUpdates(ChildModWithConfig mod) {
    	
    	// Make sure it gets any changes thus far
    	mod.syncConfig();
    	
    	// Make sure it gets future changes
    	childrenWithConfig.add(mod);
    }
    
    @SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if (eventArgs.getModID().equals(this.MODID)) {
			syncConfig();
		}
		for (ChildModWithConfig child : childrenWithConfig) {
			child.syncConfig();
		}
	}
	
    public static void syncConfig() {
    	// MineOne/ContinuouslyMine
    	mAutoSelectTool = mConfig.getBoolean("Auto-select pickaxe", Configuration.CATEGORY_GENERAL, 
    			true, "When mining, do you want tool selected automatically?");
    	
		if (mConfig.hasChanged()) {
			mConfig.save();
		}
	}
}

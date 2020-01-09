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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.utils.ModUtils;

import net.minecraft.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.config.ModConfig;

// This mod is purely a wrapper to cluster all our smaller mods.
// The parent (this mod) handles configuration so that it can all
// be changed by the user in one place.

@Mod(EyeGaze.MODID)
public class EyeGaze extends BaseClassWithCallbacks {
	/* In v1.11.2, this mod was a wrapper that contained user-exposed
	 * config, and acted as the 'parent' mod to all other mods
	 * This allowed us to expose config UI in one place, and not clutter 
	 * up the Mods List (while simultaneously letting us have small self-
	 * contained mods for different features). 
	 * In v1.14, the parent->child relationship doesn't exist any more, 
	 * so this is a bit of a ghost town. We might consider updating 
	 * it as our own uber-mod, with other mods registered to it.
	 * 
	 *  FIXME: figure out what to do with it architecturally :-)
	 */

    
	public static final String MODID = "eyegaze";	
	public static final String VERSION = ModUtils.VERSION;	
	public static final String NAME = "Eye Gaze";

	public static Configuration mConfig;	
	
	// Category names for clustering config options in different UIs
	public static final String CATEGORY_BASIC = "Basic options";
	public static final String CATEGORY_ADVANCED = "Advanced options";
	public static final String CATEGORY_EXPERT = "Expert options";
    
    private static List<ChildModWithConfig> childrenWithConfig = new ArrayList<ChildModWithConfig>();
    
    public EyeGaze() {
    	// Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        
        // Config setup
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Configuration.CLIENT_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Configuration.COMMON_CONFIG);

        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        Configuration.loadConfig(Configuration.CLIENT_CONFIG, FMLPaths.CONFIGDIR.get().resolve("mytutorial-client.toml"));
        Configuration.loadConfig(Configuration.COMMON_CONFIG, FMLPaths.CONFIGDIR.get().resolve("mytutorial-common.toml"));
    }
    
    public static void registerForConfigUpdates(ChildModWithConfig mod) {
    	//FIXME: ? 
    	/*
    	// Make sure it gets any changes thus far
    	mod.syncConfig();
    	
    	// Make sure it gets future changes
    	childrenWithConfig.add(mod);
    	*/
    }
    
//  TODO: 
// Reinstate this somewhere, somehow
//	@SubscribeEvent
//	@SuppressWarnings("static-access")
//	public void preInit(FMLPreInitializationEvent event) {
//		MinecraftForge.EVENT_BUS.register(this);
//
//		ModUtils.setupModInfo(event, this.MODID, this.NAME,
//				"A selection of mods which increase accessibility and support eye gaze input");
//
//		// Set up config
//		mConfig = new Configuration(event.getSuggestedConfigurationFile());
//		this.syncConfig();
//	}

	@SubscribeEvent
	@SuppressWarnings("static-access")
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		/* FIXME if (eventArgs.getModID().equals(this.MODID)) {
			syncConfig();
		}
		for (ChildModWithConfig child : childrenWithConfig) {
			child.syncConfig();
		}*/
	}
	
	public static void saveWalkingSpeed(float speed) {
		
		Configuration.customSpeedFactor.set((double) speed);
		
		//FIXME: notify other apps about changes?? probs for some, but not this one.
		// but could use old syncConfig methods if required.
		
//		Configuration.customSpeedFactor.save();		
	    //mConfig.get(CATEGORY_BASIC,  "Walking speed", customSpeedFactor).set(customSpeedFactor);
		/* FIXME
		if (mConfig.hasChanged()) {
			mConfig.save();
			
			for (ChildModWithConfig child : childrenWithConfig) {
				child.syncConfig();
			}
		}		*/	
	}
	
	public static void syncConfig() {

	}

}

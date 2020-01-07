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
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// This mod is purely a wrapper to cluster all our smaller mods.
// The parent (this mod) handles configuration so that it can all
// be changed by the user in one place.

@Mod(EyeGaze.MODID)
public class EyeGaze extends BaseClassWithCallbacks {

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    
	public static final String MODID = "eyegaze";	
	public static final String VERSION = ModUtils.VERSION;	
	public static final String NAME = "Eye Gaze";

	public static Configuration mConfig;	
	
	// Category names for clustering config options in different UIs
	public static final String CATEGORY_BASIC = "Basic options";
	public static final String CATEGORY_ADVANCED = "Advanced options";
	public static final String CATEGORY_EXPERT = "Expert options";

	// Anything in this list gets its own UI for all associated config
	public static List<String> userfacingCategories = 
			new ArrayList<String>(Arrays.asList(
					CATEGORY_BASIC, 
					CATEGORY_ADVANCED,
					CATEGORY_EXPERT));

	// Flying options
	public static int flyHeightManual = 2;
	public static int flyHeightAuto = 6;
	
	// Walking options -> walk with gaze
	public static int filterLength = 50;
	public static boolean moveWhenMouseStationary = false;
    public static float customSpeedFactor = 0.8f;
	
    // OptiKey adjustments
    public static int viewIncrement = 2;
    public static double moveIncrement = 2;

	// AutoJump
    public static boolean defaultDoAutoJump = true;
    
    // MouseHandler options
 	public static float mDeadBorder = 0.1f;
 	public static boolean usingMouseEmulation = false;
 	
 	// Mining
	public static boolean mAutoSelectTool;

	// AutoOpenDoors
    public static int mRadiusDoors = 3;
	
    // OpenTablesChests
    public static int mRadiusChests = 5;
    
    // ContinuouslyAttack
    public static boolean mAutoSelectSword;
    
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
    }
    
    private void setup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("HELLO FROM PREINIT");
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
    }

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("examplemod", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event)
    {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC", event.getIMCStream().
                map(m->m.getMessageSupplier().get()).
                collect(Collectors.toList()));
    }
    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // register a new block here
            LOGGER.info("HELLO from Register Block");
        }
    }
    
    public static void registerForConfigUpdates(ChildModWithConfig mod) {
    	
    	// Make sure it gets any changes thus far
    	mod.syncConfig();
    	
    	// Make sure it gets future changes
    	childrenWithConfig.add(mod);
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
		if (eventArgs.getModID().equals(this.MODID)) {
			syncConfig();
		}
		for (ChildModWithConfig child : childrenWithConfig) {
			child.syncConfig();
		}
	}
	
	public static void setWalkingSpeed(float speed) {
		
		customSpeedFactor = speed;
	    //mConfig.get(CATEGORY_BASIC,  "Walking speed", customSpeedFactor).set(customSpeedFactor);
                
		if (mConfig.hasChanged()) {
			mConfig.save();
			
			for (ChildModWithConfig child : childrenWithConfig) {
				child.syncConfig();
			}
		}			
	}
	
	public static void syncConfig() {

//		for (String category : userfacingCategories) {
//			mConfig.getCategory(category);
//		}		
		
		// Flying
		flyHeightManual = mConfig.getInt( "Fly height (manual)", 
										CATEGORY_ADVANCED, 
				flyHeightManual, 1, 20, "How high to fly in manual mode");
		flyHeightAuto = mConfig.getInt( "Fly height (auto)", 
										CATEGORY_ADVANCED, 
				flyHeightAuto, 1, 20, "How high to fly in auto mode");
		
		// Move with gaze
		filterLength = mConfig.getInt( "Smoothness filter for walking", CATEGORY_EXPERT, filterLength, 
									  1, 200, "How many ticks to take into account for slowing down while looking around. (smaller number = faster)");
        moveWhenMouseStationary = mConfig.getBoolean( "Move when mouse is stationary", CATEGORY_ADVANCED, 
        									moveWhenMouseStationary, "Continue walking forward when the mouse is stationary. Recommended to be turned off for eye gaze control, on for joysticks.");
        customSpeedFactor = mConfig.getFloat( "Walking speed", CATEGORY_BASIC, customSpeedFactor, 0.0f, 1.0f, 
        						"A scaling factor for speed of walk-with-gaze. 1.0 = maximum."); 
        
        // OptiKey adjustments
        viewIncrement = mConfig.getInt( "View adjustment (degrees)", CATEGORY_EXPERT, 
        								viewIncrement, 1, 45, 
        								"Fixed rotation for small view adjustments");
        moveIncrement = (double)mConfig.getFloat( "Move adjustment", CATEGORY_EXPERT, (float)moveIncrement, 
        										 0.1f, 10.0f,
        										 "Fixed distance for small movement adjustments");
        
        // AutoJump
        defaultDoAutoJump = mConfig.getBoolean( "Auto-jump switched on by default?", CATEGORY_BASIC, defaultDoAutoJump,
        									   "Whether auto-jump is on at the beginning of a new game.");
       
        // MouseHandler
        mDeadBorder = mConfig.getFloat( "Mousehandler dead border size", CATEGORY_EXPERT, mDeadBorder, 0.001f, 0.25f, 
        		"Fraction of screen in which mouse movements are ignored. Increase this if you find your view being dragged toward your eyegaze keyboard.");
        usingMouseEmulation = mConfig.getBoolean( "Enable mouse-emulation compatibility mode", CATEGORY_BASIC, false, 
        		"Turn this on if you're using mouse position as an input to EyeMine");
        
        // AutoOpenDoors
  		mRadiusDoors = mConfig.getInt( "Distance to open doors", CATEGORY_ADVANCED, mRadiusDoors, 
  				1, 20, "How far away a player needs to be from a door to automatically open/close");

  		// OpenTablesChests
  		// This is limited to 6 blocks since the gui doesn't appear if block is too far away
  		mRadiusChests = mConfig.getInt( "Distance to open chests/crafting tables", CATEGORY_ADVANCED, mRadiusChests, 
  				1, 6, "How far away a player needs to be from a chest/table to be able to open it");
          
  		// ContinuouslyAttack
  		mAutoSelectSword = mConfig.getBoolean( "Auto-select weapon when attacking", CATEGORY_BASIC, 
  				true, "When attacking, do you want a sword selected automatically?");
  		
         // MineOne/ContinuouslyMine
     	mAutoSelectTool = mConfig.getBoolean( "Auto-select tool for mining", CATEGORY_BASIC, 
     			true, "When mining, do you want a pickaxe selected automatically?");
     	
 		
		if (mConfig.hasChanged()) {
			mConfig.save();
		}
	}

}

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

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.utils.ModUtils;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

// This mod is purely a wrapper to cluster all our smaller mods.
// The parent (this mod) handles configuration so that it can all
// be changed by the user in one place.

@Mod(modid = EyeGaze.MODID, 
  	 version = ModUtils.VERSION, 
	 name = EyeGaze.NAME, 
	 guiFactory = "com.specialeffect.gui.GuiFactoryEyeGaze")
public class EyeGaze extends BaseClassWithCallbacks {

	public static final String MODID = "specialeffect.eyegaze";	
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
    
    public static void registerForConfigUpdates(ChildModWithConfig mod) {
    	
    	// Make sure it gets any changes thus far
    	mod.syncConfig();
    	
    	// Make sure it gets future changes
    	childrenWithConfig.add(mod);
    }
    
    
	@EventHandler
	@SuppressWarnings("static-access")
	public void preInit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);

		ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"A selection of mods which increase accessibility and support eye gaze input");

		// Set up config
		mConfig = new Configuration(event.getSuggestedConfigurationFile());
		this.syncConfig();
	}

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
	    mConfig.get(CATEGORY_BASIC,  "Walking speed", customSpeedFactor).set(customSpeedFactor);
                
		if (mConfig.hasChanged()) {
			mConfig.save();
			
			for (ChildModWithConfig child : childrenWithConfig) {
				child.syncConfig();
			}
		}			
	}
	
	public static void syncConfig() {

		for (String category : userfacingCategories) {
			mConfig.getCategory(category);
		}		
		
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

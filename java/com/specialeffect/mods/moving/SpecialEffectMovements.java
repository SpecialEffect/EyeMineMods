/**
 * Copyright (C) 2016 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.mods.moving;

import java.util.ArrayList;
import java.util.List;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.utils.ModUtils;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
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
@Mod(modid = SpecialEffectMovements.MODID, 
  	 version = ModUtils.VERSION, 
	 name = SpecialEffectMovements.NAME, 
	 guiFactory = "com.specialeffect.gui.GuiFactoryMovements")
public class SpecialEffectMovements extends BaseClassWithCallbacks {

	public static final String MODID = "specialeffect.movements";	
	public static final String NAME = "SpecialEffectsMovements";

	public static Configuration mConfig;	

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
				"A selection of mods for accessible moving methods");

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
		if (eventArgs.getModID().equals(this.MODID)) {
			syncConfig();
		}
		for (ChildModWithConfig child : childrenWithConfig) {
			child.syncConfig();
		}
	}
	
	public static void saveConfig() {
		
		// EVERYTHING IN HERE SHOULD BE REFLECTED IN SYNCCONFIG
		
		// Flying
		mConfig.get(Configuration.CATEGORY_GENERAL, "Fly height manual", flyHeightManual).set(flyHeightManual);				
		mConfig.get(Configuration.CATEGORY_GENERAL, "Fly height auto",  flyHeightAuto).set(flyHeightAuto);
		
		// Move with gaze
		mConfig.get(Configuration.CATEGORY_GENERAL, "Smoothness filter", filterLength).set(filterLength);
        mConfig.get(Configuration.CATEGORY_GENERAL, "Move when mouse stationary", moveWhenMouseStationary).set(moveWhenMouseStationary);
        mConfig.get(Configuration.CATEGORY_GENERAL, "Speed factor", customSpeedFactor).set(customSpeedFactor);
        
        // OptiKey adjustments
        mConfig.get(Configuration.CATEGORY_GENERAL, "View adjustment (degrees)", viewIncrement).set(viewIncrement);
        mConfig.get(Configuration.CATEGORY_GENERAL, "Move adjustment", (float)moveIncrement).set((float)moveIncrement);
        
        // AutoJump
        mConfig.get(Configuration.CATEGORY_GENERAL, "Auto-jump switched on by default?", defaultDoAutoJump).set(defaultDoAutoJump);
       
        // MouseHandler
        mConfig.get(Configuration.CATEGORY_GENERAL, "Mousehandler dead border size", mDeadBorder).set(mDeadBorder); 
        mConfig.get(Configuration.CATEGORY_GENERAL, "Enable mouse-emulation compatibility mode", usingMouseEmulation).set(usingMouseEmulation);
        
		if (mConfig.hasChanged()) {
			mConfig.save();
			
			for (ChildModWithConfig child : childrenWithConfig) {
				child.syncConfig();
			}
		}			
	}
	
	public static void syncConfig() {

		// EVERYTHING IN HERE SHOULD BE REFLECTED IN SAVECONFIG
		
		// Flying
		flyHeightManual = mConfig.getInt("Fly height manual", Configuration.CATEGORY_GENERAL, 
				flyHeightManual, 1, 20, "How high to fly in manual mode");
		flyHeightAuto = mConfig.getInt("Fly height auto", Configuration.CATEGORY_GENERAL, 
				flyHeightAuto, 1, 20, "How high to fly in auto mode");
		
		// Move with gaze
		filterLength = mConfig.getInt("Smoothness filter", Configuration.CATEGORY_GENERAL, filterLength, 
									  1, 200, "How many ticks to take into account for slowing down while looking around. (smaller number = faster)");
        moveWhenMouseStationary = mConfig.getBoolean("Move when mouse stationary", Configuration.CATEGORY_GENERAL, 
        									moveWhenMouseStationary, "Continue walking forward when the mouse is stationary. Recommended to be turned off for eye gaze control, on for joysticks.");
        customSpeedFactor = mConfig.getFloat("Speed factor", Configuration.CATEGORY_GENERAL, customSpeedFactor, 0.0f, 1.0f, 
        						"A scaling factor for speed of walk-with-gaze. 1.0 = maximum."); 
        
        // OptiKey adjustments
        viewIncrement = mConfig.getInt("View adjustment (degrees)", Configuration.CATEGORY_GENERAL, 
        								viewIncrement, 1, 45, 
        								"Fixed rotation for small view adjustments");
        moveIncrement = (double)mConfig.getFloat("Move adjustment", Configuration.CATEGORY_GENERAL, (float)moveIncrement, 
        										 0.1f, 10.0f,
        										 "Fixed distance for small movement adjustments");
        
        // AutoJump
        defaultDoAutoJump = mConfig.getBoolean("Auto-jump switched on by default?", Configuration.CATEGORY_GENERAL, defaultDoAutoJump,
        									   "Whether auto-jump is on at the beginning of a new game.");
       
        // MouseHandler
        mDeadBorder = mConfig.getFloat("Mousehandler dead border size", Configuration.CATEGORY_GENERAL, mDeadBorder, 0.001f, 0.25f, 
        		"Fraction of screen in which mouse movements are ignored. Increase this if you find your view being dragged toward your eyegaze keyboard.");
        usingMouseEmulation = mConfig.getBoolean("Enable mouse-emulation compatibility mode", Configuration.CATEGORY_GENERAL, false, 
        		"Turn this on if you're using mouse position as an input to OptiKey");
        
		if (mConfig.hasChanged()) {
			mConfig.save();
		}
	}

}

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

import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.core.io.WritingMode;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.api.distmarker.Dist;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Bus.MOD)
public class EyeMineConfig {
	// Directly reference a log4j logger.
	private static final Logger LOGGER = LogManager.getLogger();

	// Based on McJty/YouTubeModding14 tutorial, MIT license:
	// https://github.com/McJty/YouTubeModding14/blob/master/LICENSE

	// Category names for clustering config options in different UIs
	public static final String CATEGORY_BASIC = "basic";
	public static final String CATEGORY_ADVANCED = "advanced";
	public static final String CATEGORY_MOVING = "moving";
	public static final String CATEGORY_DWELLING = "dwelling";

	public static final String CATEGORY_BASIC_USERSTRING = "Basic options";
	public static final String CATEGORY_ADVANCED_USERSTRING = "Advanced options";
	public static final String CATEGORY_MOVING_USERSTRING = "Moving options";
	public static final String CATEGORY_DWELLING_USERSTRING = "Dwelling options";

	private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
	public static ForgeConfigSpec CLIENT_CONFIG;

	// Flying options
	public static ForgeConfigSpec.IntValue flyHeightManual;
	public static ForgeConfigSpec.IntValue flyHeightAuto;

	// Walking options -> walk with gaze
	public static ForgeConfigSpec.IntValue filterLength;
	public static ForgeConfigSpec.BooleanValue moveWhenMouseStationary;
	public static ForgeConfigSpec.DoubleValue customSpeedFactor;

	public static ForgeConfigSpec.BooleanValue slowdownOnCorners;
	public static ForgeConfigSpec.BooleanValue slowdownOnAttack;
	
	public static ForgeConfigSpec.BooleanValue allowLadderDescent;

	// Boats
	public static ForgeConfigSpec.DoubleValue boatSlowdown;
	public static ForgeConfigSpec.IntValue boatMaxTurnAtSpeed;
	
	// AutoJump
	public static ForgeConfigSpec.BooleanValue defaultDoAutoJump;

	// MouseHandler options	
	public static ForgeConfigSpec.BooleanValue usingMouseEmulation;

	// Mining
	public static ForgeConfigSpec.BooleanValue autoSelectTool;
	public static ForgeConfigSpec.IntValue ticksBetweenMining;
	public static ForgeConfigSpec.BooleanValue serverCompatibilityMode;

	// AutoOpenDoors
	public static ForgeConfigSpec.IntValue radiusDoors;

	// OpenTablesChests
	public static ForgeConfigSpec.IntValue radiusChests;

	// ContinuouslyAttack
	public static ForgeConfigSpec.BooleanValue autoSelectSword;

	// Dwelling options
	public static ForgeConfigSpec.DoubleValue dwellTimeSeconds;
	public static ForgeConfigSpec.DoubleValue dwellLockonTimeSeconds;
	public static ForgeConfigSpec.BooleanValue dwellShowExpanding;
	public static ForgeConfigSpec.BooleanValue dwellShowWithTransparency;
	public static ForgeConfigSpec.BooleanValue useDwellForSingleMine;
	public static ForgeConfigSpec.BooleanValue useDwellForSingleUseItem;

	// Ironsights
	public static ForgeConfigSpec.DoubleValue ironsightsSensitivityReduction;
	public static ForgeConfigSpec.IntValue ironsightsFovReduction;

	// Bow and arrow
	public static ForgeConfigSpec.DoubleValue bowDrawTime;
	
	// Graphics
	public static ForgeConfigSpec.DoubleValue fullscreenOverlayAlpha;

	static {

		CLIENT_BUILDER.comment(CATEGORY_BASIC_USERSTRING).push(CATEGORY_BASIC);
		setupBasicConfig();
		CLIENT_BUILDER.pop();

		CLIENT_BUILDER.comment(CATEGORY_ADVANCED_USERSTRING).push(CATEGORY_ADVANCED);
		setupAdvancedConfig();
		CLIENT_BUILDER.pop();

		CLIENT_BUILDER.comment(CATEGORY_MOVING_USERSTRING).push(CATEGORY_MOVING);
		setupMovingConfig();
		CLIENT_BUILDER.pop();

		CLIENT_BUILDER.comment(CATEGORY_DWELLING_USERSTRING).push(CATEGORY_DWELLING);
		setupDwellConfig();
		CLIENT_BUILDER.pop();

		CLIENT_CONFIG = CLIENT_BUILDER.build();
	}

	private static void setupBasicConfig() {
		customSpeedFactor = CLIENT_BUILDER.comment("Walking speed for walk-with-gaze").defineInRange("walkingSpeed",
				0.6f, 0.25f, 2.0f);

		defaultDoAutoJump = CLIENT_BUILDER.comment("Auto-jump switched on by default?").define("defaultDoAutoJump",
				true);

		usingMouseEmulation = CLIENT_BUILDER.comment(
				"Enable mouse-emulation compatibility mode?.\nTurn this on if you're using mouse position as an input to EyeMine")
				.define("usingMouseEmulation", false);

		autoSelectSword = CLIENT_BUILDER.comment(
				"When attacking, do you want a sword selected automatically?\nThis only applies in Creative Mode.")
				.define("autoSelectSword", true);

		autoSelectTool = CLIENT_BUILDER
				.comment(
						"When mining, do you want pickaxe selected automatically?\nThis only applies in Creative Mode.")
				.define("autoSelectTool", true);

	}

	private static void setupAdvancedConfig() {

		// This is limited to 6 blocks since the gui doesn't appear if block is too far
		// away
		radiusChests = CLIENT_BUILDER
				.comment("How far away a player needs to be from a chest/table to be able to open it")
				.defineInRange("radiusChests", 5, 1, 6);

	    radiusDoors = CLIENT_BUILDER.comment(
	        "How far away a player needs to be from a door to automatically open/close.\nSet to zero to turn off automatic door-opening")
	        .defineInRange("radiusDoors", 2, 0, 10);
	
			ticksBetweenMining = CLIENT_BUILDER
					.comment("How many ticks to wait before mining again\nOnly affects creative mode")
					.defineInRange("ticksBetweenMining", 15, 0, 50);
	
	    // Ironsights
	    ironsightsFovReduction = CLIENT_BUILDER
	        .comment("How much to reduce field of view (degrees) when using ironsights")
	        .defineInRange("ironsightsFovReduction", 20, 0, 40);
	
	    ironsightsSensitivityReduction = CLIENT_BUILDER
	        .comment("How much to reduce sensitivity (%) when using ironsights")
	        .defineInRange("ironsightsSensitivityReduction", 13.0, 0.0, 30.0);
	
	    // Bow-firing
	    bowDrawTime = CLIENT_BUILDER.comment("How long (seconds) to keep bow drawn for when firing with 'Use Item'")
	        .defineInRange("bowDrawTime", 1.0, 0.5, 5.0);	        
	    
	    fullscreenOverlayAlpha = CLIENT_BUILDER
	            .comment("Opacity of full-screen overlays (look, careful walk)")
	            .defineInRange("fullscreenOverlayAlpha", 0.1, 0.0, 0.2);

	    serverCompatibilityMode = CLIENT_BUILDER
        .comment("Use simpler mining/placing logic to play on servers without EyeMine installed")
        .define("serverCompatibilityMode", false);

	}

	private static void setupMovingConfig() {

    slowdownOnCorners = CLIENT_BUILDER
        .comment("Slow down auto-walk when going round a corner\nYou may want to turn this off for survival")
        .define("slowdownOnCorners", true);

		filterLength = CLIENT_BUILDER.comment(
				"How many ticks to take into account for slowing down while looking around / turning corners.\n(smaller number = faster)")
				.defineInRange("walkingSlowdownFilter", 30, 1, 200);

    moveWhenMouseStationary = CLIENT_BUILDER.comment(
        "Continue walking forward when the mouse is stationary?\nRecommended to be turned off for eye gaze control, or turned on for joysticks.")
        .define("moveWhenMouseStationary", false);

    slowdownOnAttack = CLIENT_BUILDER.comment(
        "Slow down auto-walk when attacking an entity\nThis only applies when your crosshair is over an entity, and makes\nit easier to chase mobs")
        .define("slowdownOnAttack", true);
        
    flyHeightManual = CLIENT_BUILDER.comment("How high to fly (up/down) in manual mode")
        .defineInRange("flyHeightManual", 2, 1, 20);

    flyHeightAuto = CLIENT_BUILDER.comment("How high to fly in auto mode").defineInRange("flyHeightAuto", 6, 1, 10);

    allowLadderDescent = CLIENT_BUILDER.comment(
            "Descend ladders by looking down while moving. \n Experimental; may cause problems getting on/off ladders.")
            .define("allowLadderDescent", false);
    
    // Boats
    boatSlowdown = CLIENT_BUILDER
            .comment("Slowdown applied to forward motion of boats (lower is slower)")
            .defineInRange("boatSlowdown", 0.5, 0.01, 1.0);

    boatMaxTurnAtSpeed = CLIENT_BUILDER
            .comment("Maximum angle (degrees) at which boat will still travel forwards while turning")
            .defineInRange("boatMaxTurnAtSpeed", 30, 1, 90);

    
	}

	private static void setupDwellConfig() {

		dwellTimeSeconds = CLIENT_BUILDER.comment("Time for dwell to complete (seconds)")
				.defineInRange("dwellTimeSeconds", 1.2, 0.2, 5.0);

		dwellLockonTimeSeconds = CLIENT_BUILDER
				.comment("Time for dwell to lock on (seconds)\n Must be lower than dwellTimeSeconds")
				.defineInRange("dwellLockonTimeSeconds", 0.2, 0.0, 1.0);

		dwellShowExpanding = CLIENT_BUILDER.comment("Show dwell expanding instead of shrinking")
				.define("dwellShowExpanding", false);

		dwellShowWithTransparency = CLIENT_BUILDER.comment(
				"Show dwell by changing transparency instead of growing/shrinking\nThis option overrides dwellShowExpanding")
				.define("dwellShowWithTransparency", false);
		
		useDwellForSingleMine = CLIENT_BUILDER.comment("Use dwell for 'mine once' (creative only)")
				.define("useDwellForSingleMine", false);
		
		useDwellForSingleUseItem = CLIENT_BUILDER.comment("Use dwell for single 'use item'")
				.define("useDwellForSingleUseItem", false);			

	}

	public static void loadConfig(ForgeConfigSpec spec, Path path) {

		final CommentedFileConfig configData = CommentedFileConfig.builder(path).preserveInsertionOrder().sync()
				.autosave().writingMode(WritingMode.REPLACE).parsingMode(ParsingMode.ADD).concurrent().build();

		configData.load();
		spec.setConfig(configData);
	}

	@SubscribeEvent
	public static void onLoad(final ModConfig.Loading configEvent) {
		LOGGER.debug("ModConfig onLoad");
	}

	@SubscribeEvent
	public static void onReload(final ModConfig.ConfigReloading configEvent) {
		// the configspec values are updated for us, but we may want to hook into
		// here to notify other mods?
		LOGGER.debug("ModConfig onReload");

		ForgeConfigSpec loadSpec = configEvent.getConfig().getSpec();
		if (loadSpec == CLIENT_CONFIG) {
			EyeGaze.refresh();
		}
	}

}
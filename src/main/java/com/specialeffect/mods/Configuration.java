package com.specialeffect.mods;

import java.nio.file.Path;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(bus = Bus.MOD)
public class Configuration {
	// Based on McJty/YouTubeModding14 tutorial, MIT license:
	// https://github.com/McJty/YouTubeModding14/blob/master/LICENSE
	
    public static final String CATEGORY_GENERAL = "general";
    public static final String CATEGORY_POWER = "power";
    public static final String SUBCATEGORY_FIRSTBLOCK = "firstblock";
    
 // Category names for clustering config options in different UIs
    public static final String CATEGORY_BASIC = "basic";
 	public static final String CATEGORY_ADVANCED = "advanced";
 	public static final String CATEGORY_EXPERT = "expert";
 	
 	public static final String CATEGORY_BASIC_USERSTRING = "Basic options";
 	public static final String CATEGORY_ADVANCED_USERSTRING = "Advanced options";
 	public static final String CATEGORY_EXPERT_USERSTRING = "Expert options";

    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

    public static ForgeConfigSpec COMMON_CONFIG;
    public static ForgeConfigSpec CLIENT_CONFIG;

    public static ForgeConfigSpec.IntValue FIRSTBLOCK_MAXPOWER;
    public static ForgeConfigSpec.IntValue FIRSTBLOCK_GENERATE;
    public static ForgeConfigSpec.IntValue FIRSTBLOCK_SEND;
    public static ForgeConfigSpec.IntValue FIRSTBLOCK_TICKS;
    
    // Flying options
 	public static ForgeConfigSpec.IntValue flyHeightManual;
 	public static ForgeConfigSpec.IntValue flyHeightAuto;
 	
 	// Walking options -> walk with gaze
 	public static ForgeConfigSpec.IntValue filterLength;
 	public static ForgeConfigSpec.BooleanValue moveWhenMouseStationary;
	public static ForgeConfigSpec.DoubleValue customSpeedFactor;
	
	// OptiKey adjustments
	public static ForgeConfigSpec.IntValue viewIncrement;
	public static ForgeConfigSpec.DoubleValue moveIncrement;
	
	// AutoJump
	public static ForgeConfigSpec.BooleanValue defaultDoAutoJump;
	 
     // MouseHandler options
  	public static ForgeConfigSpec.DoubleValue mDeadBorder;
  	public static ForgeConfigSpec.BooleanValue usingMouseEmulation;
  	
  	// Mining
 	public static ForgeConfigSpec.BooleanValue mAutoSelectTool;

 	// AutoOpenDoors
     public static ForgeConfigSpec.IntValue mRadiusDoors;
 	
     // OpenTablesChests
     public static ForgeConfigSpec.IntValue mRadiusChests;
     
     // ContinuouslyAttack
     public static ForgeConfigSpec.BooleanValue mAutoSelectSword;

    static {
    	//FIXME: can all these live in client config?
        COMMON_BUILDER.comment(CATEGORY_BASIC_USERSTRING).push(CATEGORY_BASIC);
        setupBasicConfig();        
        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment(CATEGORY_ADVANCED_USERSTRING).push(CATEGORY_ADVANCED);
        setupAdvancedConfig();
        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment(CATEGORY_EXPERT_USERSTRING).push(CATEGORY_EXPERT);
        setupExpertConfig();
        COMMON_BUILDER.pop();

        COMMON_CONFIG = COMMON_BUILDER.build();
        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }


    private static void setupBasicConfig() {
    	// TODO: how to manage tooltips on own UI?
    	// previously had short comment, and tooltips
    	// in text file there's no limit to comment lenght, but if we make a GUI we'll need
    	// to split up again
    	
		
    	customSpeedFactor = COMMON_BUILDER.comment("Walking speed for walk-with-gaze")
      		.defineInRange("customSpeedFactor", 0.8f, 0.25f, 2.0f);                
      
      	defaultDoAutoJump = COMMON_BUILDER.comment("Auto-jump switched on by default?")
      		.define("defaultDoAutoJump", true);

      	usingMouseEmulation = COMMON_BUILDER.comment("Enable mouse-emulation compatibility mode. Turn this on if you're using mouse position as an input to EyeMine").
      		define("usingMouseEmulation", false);

		mAutoSelectSword = COMMON_BUILDER.comment("When attacking, do you want a sword selected automatically?")
				.define("autoSelectSword", true);
		
		mAutoSelectTool = COMMON_BUILDER.comment("Auto-select tool for mining")
   			.define("autoSelectTool",  true);
		
    }
    
    private static void setupAdvancedConfig() {    			
		
    	// Flying
		flyHeightManual = COMMON_BUILDER.comment("How high to fly in manual mode")
				.defineInRange("flyHeightManual", 2, 1, 20);
		
		flyHeightAuto = COMMON_BUILDER.comment("How high to fly in auto mode") 
				.defineInRange("flyHeightAuto", 6, 1, 10);

  		mRadiusDoors = COMMON_BUILDER.comment("How far away a player needs to be from a door to automatically open/close")
  				.defineInRange("radiusDoors", 3,  1, 20);

  		// This is limited to 6 blocks since the gui doesn't appear if block is too far away
  		mRadiusChests = COMMON_BUILDER.comment("How far away a player needs to be from a chest/table to be able to open it")
  				.defineInRange("radiusChests", 5, 1, 6);  
  		
        moveWhenMouseStationary = COMMON_BUILDER.comment("Continue walking forward when the mouse is stationary? Recommended to be turned off for eye gaze control, on for joysticks.")
        		.define("moveWhenMouseStationary", false);
        
    }
    
    private static void setupExpertConfig() {    			
		
		// Move with gaze
		filterLength = COMMON_BUILDER.comment("How many ticks to take into account for slowing down while looking around. (smaller number = faster)")
				.defineInRange("walkingFilterLength", 50, 1, 200);

        // MouseHandler
        mDeadBorder = COMMON_BUILDER.comment("Fraction of screen in which mouse movements are ignored. Increase this if you find your view being dragged toward your eyegaze keyboard.")
        		.defineInRange("deadBorder", 0.1, 0.0, 0.25);
    	     	
    }
    
    public static void loadConfig(ForgeConfigSpec spec, Path path) {

        final CommentedFileConfig configData = CommentedFileConfig.builder(path)
                .sync()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .build();

        configData.load();
        spec.setConfig(configData);
    }

    @SubscribeEvent
    public static void onLoad(final ModConfig.Loading configEvent) {    	
    	System.out.println("ModConfig onLoad");
    }

    @SubscribeEvent
    public static void onReload(final ModConfig.ConfigReloading configEvent) {
    	// the configspec values are updated for us, but we may want to hook into 
    	// here to notify other mods?
    	System.out.println("ModConfig onReload");    	
    }
    

}
package com.specialeffect.mods;

import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
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
 	public static final String CATEGORY_EXPERT = "expert";
 	public static final String CATEGORY_DWELLING = "dwelling";
 	
 	public static final String CATEGORY_BASIC_USERSTRING = "Basic options";
 	public static final String CATEGORY_ADVANCED_USERSTRING = "Advanced options";
 	public static final String CATEGORY_EXPERT_USERSTRING = "Expert options";
 	public static final String CATEGORY_DWELLING_USERSTRING = "Dwelling options";

    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

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
	
	public static ForgeConfigSpec.BooleanValue mSlowdownOnCorners;
	public static ForgeConfigSpec.BooleanValue mSlowdownOnAttack;
	
	// AutoJump
	public static ForgeConfigSpec.BooleanValue defaultDoAutoJump;
	 
     // MouseHandler options
  	public static ForgeConfigSpec.DoubleValue mDeadBorder;
  	public static ForgeConfigSpec.BooleanValue usingMouseEmulation;
  	
  	// Mining
 	public static ForgeConfigSpec.BooleanValue mAutoSelectTool;
 	public static ForgeConfigSpec.IntValue mTicksBetweenMining;

 	// AutoOpenDoors
     public static ForgeConfigSpec.IntValue mRadiusDoors;
 	
     // OpenTablesChests
     public static ForgeConfigSpec.IntValue mRadiusChests;
     
     // ContinuouslyAttack
     public static ForgeConfigSpec.BooleanValue mAutoSelectSword;
     
     // Dwelling options
     public static ForgeConfigSpec.DoubleValue dwellTimeSeconds;
     public static ForgeConfigSpec.DoubleValue dwellLockonTimeSeconds;
     public static ForgeConfigSpec.BooleanValue dwellShowExpanding;
     public static ForgeConfigSpec.BooleanValue dwellShowWithTransparency;    

    static {
        CLIENT_BUILDER.comment(CATEGORY_BASIC_USERSTRING).push(CATEGORY_BASIC);
        setupBasicConfig();        
        CLIENT_BUILDER.pop();

        CLIENT_BUILDER.comment(CATEGORY_ADVANCED_USERSTRING).push(CATEGORY_ADVANCED);
        setupAdvancedConfig();
        CLIENT_BUILDER.pop();

        CLIENT_BUILDER.comment(CATEGORY_EXPERT_USERSTRING).push(CATEGORY_EXPERT);
        setupExpertConfig();
        CLIENT_BUILDER.pop();
        
        CLIENT_BUILDER.comment(CATEGORY_DWELLING_USERSTRING).push(CATEGORY_DWELLING);
        setupDwellConfig();
        CLIENT_BUILDER.pop();


        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }


    private static void setupBasicConfig() {
    	// TODO: how to manage tooltips on own UI?
    	// previously had short comment, and tooltips
    	// in text file there's no limit to comment lenght, but if we make a GUI we'll need
    	// to split up again    
		
    	customSpeedFactor = CLIENT_BUILDER.comment("Walking speed for walk-with-gaze")
      		.defineInRange("walkingSpeed", 0.8f, 0.25f, 2.0f);                
      
      	defaultDoAutoJump = CLIENT_BUILDER.comment("Auto-jump switched on by default?")
      		.define("defaultDoAutoJump", true);

      	usingMouseEmulation = CLIENT_BUILDER.comment("Enable mouse-emulation compatibility mode?.\nTurn this on if you're using mouse position as an input to EyeMine").
      		define("usingMouseEmulation", false);

		mAutoSelectSword = CLIENT_BUILDER.comment("When attacking, do you want a sword selected automatically?\nThis only applies in Creative Mode.")
				.define("autoSelectSword", true);
		
		mAutoSelectTool = CLIENT_BUILDER.comment("When mining, do you want pickaxe selected automatically?\nThis only applies in Creative Mode.")
   			.define("autoSelectTool",  true);
		
    }
    
    private static void setupAdvancedConfig() {    			
		
    	// Flying
		flyHeightManual = CLIENT_BUILDER.comment("How high to fly (up/down) in manual mode")
				.defineInRange("flyHeightManual", 2, 1, 20);
		
		flyHeightAuto = CLIENT_BUILDER.comment("How high to fly in auto mode") 
				.defineInRange("flyHeightAuto", 6, 1, 10);

  		mRadiusDoors = CLIENT_BUILDER.comment("How far away a player needs to be from a door to automatically open/close.\nSet to zero to turn off automatic door-opening")
  				.defineInRange("radiusDoors", 2,  0, 10);

  		// This is limited to 6 blocks since the gui doesn't appear if block is too far away
  		mRadiusChests = CLIENT_BUILDER.comment("How far away a player needs to be from a chest/table to be able to open it")
  				.defineInRange("radiusChests", 5, 1, 6);  
  		
        moveWhenMouseStationary = CLIENT_BUILDER.comment("Continue walking forward when the mouse is stationary?\nRecommended to be turned off for eye gaze control, or turned on for joysticks.")
        		.define("moveWhenMouseStationary", false);
        
        mSlowdownOnCorners = CLIENT_BUILDER.comment("Slow down auto-walk when going round a corner\nYou may want to turn this off for survival")
        	.define("slowdownOnCorners", true);
        
        mSlowdownOnAttack = CLIENT_BUILDER.comment("Slow down auto-walk when attacking an entity\nThis only applies when your crosshair is over an entity, and makes\nit easier to chase mobs")
            	.define("slowdownOnAttack", true);
        
        mTicksBetweenMining = CLIENT_BUILDER.comment("How many ticks to wait before mining again\nOnly affects creative mode")
  				.defineInRange("ticksBetweenMining", 15,  0, 50);

        
    }
    
    private static void setupExpertConfig() {    			
		
		    // Move with gaze
		    filterLength = CLIENT_BUILDER.comment("How many ticks to take into account for slowing down while looking around / turning corners.\n(smaller number = faster)")
				.defineInRange("walkingSlowdownFilter", 30, 1, 200);

        // MouseHandler
        mDeadBorder = CLIENT_BUILDER.comment("Fraction of screen in which mouse movements are ignored. Increase this if you find your view being dragged toward your eyegaze keyboard.")
        		.defineInRange("deadBorder", 0.1, 0.0, 0.25);
    	     	
    }
    
    private static void setupDwellConfig() {
    	
    	dwellTimeSeconds = CLIENT_BUILDER.comment("Time for dwell to complete (seconds)")
    			.defineInRange("dwellTimeSeconds", 1.2, 0.2, 5.0);
    	
    	dwellLockonTimeSeconds = CLIENT_BUILDER.comment("Time for dwell to lock on (seconds)\n Must be lower than dwellTimeSeconds")
    			.defineInRange("dwellLockonTimeSeconds", 0.2, 0.0, 1.0);
    	
    	dwellShowExpanding = CLIENT_BUILDER.comment("Show dwell expanding instead of shrinking")
    			.define("dwellShowExpanding", false);
    	
    	dwellShowWithTransparency = CLIENT_BUILDER.comment("Show dwell by changing transparency instead of growing/shrinking\nThis option overrides dwellShowExpanding")
    			.define("dwellShowWithTransparency", false);
    	
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
    	LOGGER.debug("ModConfig onLoad");
    }

    @SubscribeEvent
    public static void onReload(final ModConfig.ConfigReloading configEvent) {
    	// the configspec values are updated for us, but we may want to hook into 
    	// here to notify other mods?
    	LOGGER.debug("ModConfig onReload");    

    	//FIXME: this architecture is a bit circular, think about responsibilities

    	ForgeConfigSpec loadSpec = configEvent.getConfig().getSpec();
    	if (loadSpec == CLIENT_CONFIG) {
    		EyeGaze.refresh();
    	}
    }
    

}
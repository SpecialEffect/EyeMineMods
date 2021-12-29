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

package com.inventory.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;

public class InventoryConfig {
	// Based on McJty/YouTubeModding14 tutorial, MIT license:
	// https://github.com/McJty/YouTubeModding14/blob/master/LICENSE
	
	// Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String CATEGORY_GENERAL = "general";
    
    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

    public static ForgeConfigSpec CLIENT_CONFIG;

    public static ConfigValue<Integer> key0, key1, key2, key3, key4,
										    key5, key6, key7, key8, key9, 
										    keyNext, keyPrev, keySearch,
										    keyScrollUp, keyScrollDown,
										    keyNextItemRow, keyNextItemCol, keyDrop;
    
    public static ConfigValue<Integer> keySurvNextTab, keySurvPrevTab, keySurvRecipes, keySurvCraftable, 
    								   keySurvPrevPage, keySurvNextPage, keySurvOutput; 
    
    
    static {
        CLIENT_BUILDER.comment("Inventory shortcut keys").push("Keys");
        setupConfigKeys();        
        CLIENT_BUILDER.pop();

        CLIENT_BUILDER.comment("Navigation keys").push("Keys");
        setupNavKeys();
        CLIENT_BUILDER.pop();

        CLIENT_BUILDER.comment("Survival inventory keys").push("Keys");
        setupSurvivalKeys();
        CLIENT_BUILDER.pop();

        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }


    private static void setupSurvivalKeys() {

    	keySurvPrevTab  = CLIENT_BUILDER.comment("recipes: prev tab").define("keySurvPrevTab", GLFW.GLFW_KEY_KP_0);
    	keySurvNextTab = CLIENT_BUILDER.comment("recipes: next tab").define("keySurvNextTab", GLFW.GLFW_KEY_KP_1);
    	
    	keySurvRecipes  = CLIENT_BUILDER.comment("open/close recipe book").define("keySurvRecipes", GLFW.GLFW_KEY_KP_2);
    	keySurvCraftable = CLIENT_BUILDER.comment("toggle all/craftable").define("keySurvCraftable", GLFW.GLFW_KEY_KP_3);  
    	
    	keySurvPrevPage  = CLIENT_BUILDER.comment("recipes: prev page").define("keySurvPrevPage", GLFW.GLFW_KEY_KP_4);
    	keySurvNextPage = CLIENT_BUILDER.comment("recipes: next page").define("keySurvNextPage", GLFW.GLFW_KEY_KP_5);
    	
    	keySurvOutput = CLIENT_BUILDER.comment("hover output").define("keySurvOutput", GLFW.GLFW_KEY_KP_6);
    			
    }
    
    private static void setupConfigKeys() {
    	        	
    	key0 = CLIENT_BUILDER.comment("key0").define("key0", GLFW.GLFW_KEY_KP_0);
    	key1 = CLIENT_BUILDER.comment("key1").define("key1", GLFW.GLFW_KEY_KP_1);
    	key2 = CLIENT_BUILDER.comment("key2").define("key2", GLFW.GLFW_KEY_KP_2);
    	key3 = CLIENT_BUILDER.comment("key3").define("key3", GLFW.GLFW_KEY_KP_3);
    	key4 = CLIENT_BUILDER.comment("key4").define("key4", GLFW.GLFW_KEY_KP_4);
    	key5 = CLIENT_BUILDER.comment("key5").define("key5", GLFW.GLFW_KEY_KP_5);
    	key6 = CLIENT_BUILDER.comment("key6").define("key6", GLFW.GLFW_KEY_KP_6);
    	key7 = CLIENT_BUILDER.comment("key7").define("key7", GLFW.GLFW_KEY_KP_7);
    	key8 = CLIENT_BUILDER.comment("key8").define("key8", GLFW.GLFW_KEY_KP_8);
    	key9 = CLIENT_BUILDER.comment("key9").define("key9", GLFW.GLFW_KEY_KP_9);
    			
    }
    
    private static void setupNavKeys() {    			

    	keyPrev = CLIENT_BUILDER.comment("keyPrev").define("keyPrev", GLFW.GLFW_KEY_LEFT);
    	keyNext = CLIENT_BUILDER.comment("keyNext").define("keyNext", GLFW.GLFW_KEY_RIGHT);
    	keyNextItemRow = CLIENT_BUILDER.comment("keyNextItemRow").define("keyNextItemRow", GLFW.GLFW_KEY_F6);
    	keyNextItemCol = CLIENT_BUILDER.comment("keyNextItemCol").define("keyNextItemCol", GLFW.GLFW_KEY_F7);
    	
    	keyScrollUp = CLIENT_BUILDER.comment("keyScrollUp").define("keyScrollUp", GLFW.GLFW_KEY_F8);
    	keyScrollDown = CLIENT_BUILDER.comment("keyScrollDown").define("keyScrollDown", GLFW.GLFW_KEY_F9);
    	
    	keySearch = CLIENT_BUILDER.comment("keySearch").define("keySearch", GLFW.GLFW_KEY_DOWN);
    	keyDrop = CLIENT_BUILDER.comment("keyDrop2").define("keyDrop2", GLFW.GLFW_KEY_MINUS);
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
    public static void onLoad(final ModConfigEvent.Loading configEvent) {
    	LOGGER.info("Inventory config onLoad");
    }

    @SubscribeEvent
    public static void onReload(final ModConfigEvent.Reloading configEvent) {
    	LOGGER.info("Inventory config onReload");
    	
		if (configEvent.getConfig() != null && configEvent.getConfig().getSpec() == CLIENT_CONFIG) {
        	// the configspec values are updated for us, but we may want to hook into here too?
    	}
    }
       
}
package com.specialeffect.inventory;

import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(bus = Bus.MOD)
public class InventoryConfig {
	// Based on McJty/YouTubeModding14 tutorial, MIT license:
	// https://github.com/McJty/YouTubeModding14/blob/master/LICENSE
	
	// Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String CATEGORY_GENERAL = "general";
    
    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

    public static ForgeConfigSpec COMMON_CONFIG;
    public static ForgeConfigSpec CLIENT_CONFIG;

    public static ConfigValue<Integer> key0, key1, key2, key3, key4,
										    key5, key6, key7, key8, key9, 
										    keyNext, keyPrev, keySearch,
										    keyScrollUp, keyScrollDown,
										    keyNextItemRow, keyNextItemCol, keyDrop;
    
    static {
    	//FIXME: can all these live in client config?
        COMMON_BUILDER.comment("Inventory shortcut keys").push("Keys");
        setupConfigKeys();        
        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Navigation keys").push("Keys");
        setupNavKeys();
        COMMON_BUILDER.pop();

        COMMON_CONFIG = COMMON_BUILDER.build();
        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }


    private static void setupConfigKeys() {
    	    
    	key0 = COMMON_BUILDER.comment("How high to fly in manual mode")
				.defineInRange("flyHeightManual", 2, 1, 20);
    	
    	key0 = COMMON_BUILDER.comment("key0").define("key0", GLFW.GLFW_KEY_KP_0);
    	key1 = COMMON_BUILDER.comment("key1").define("key1", GLFW.GLFW_KEY_KP_1);
    	key2 = COMMON_BUILDER.comment("key2").define("key2", GLFW.GLFW_KEY_KP_2);
    	key3 = COMMON_BUILDER.comment("key3").define("key3", GLFW.GLFW_KEY_KP_3);
    	key4 = COMMON_BUILDER.comment("key4").define("key4", GLFW.GLFW_KEY_KP_4);
    	key5 = COMMON_BUILDER.comment("key5").define("key5", GLFW.GLFW_KEY_KP_5);
    	key6 = COMMON_BUILDER.comment("key6").define("key6", GLFW.GLFW_KEY_KP_6);
    	key7 = COMMON_BUILDER.comment("key7").define("key7", GLFW.GLFW_KEY_KP_7);
    	key8 = COMMON_BUILDER.comment("key8").define("key8", GLFW.GLFW_KEY_KP_8);
    	key9 = COMMON_BUILDER.comment("key9").define("key9", GLFW.GLFW_KEY_KP_9);
    			
    }
    
    private static void setupNavKeys() {    			

    	keyPrev = COMMON_BUILDER.comment("keyPrev").define("keyPrev", GLFW.GLFW_KEY_LEFT);
    	keyNext = COMMON_BUILDER.comment("keyNext").define("keyNext", GLFW.GLFW_KEY_RIGHT);
    	keyNextItemRow = COMMON_BUILDER.comment("keyNextItemRow").define("keyNextItemRow", GLFW.GLFW_KEY_F6);
    	keyNextItemCol = COMMON_BUILDER.comment("keyNextItemCol").define("keyNextItemCol", GLFW.GLFW_KEY_F7);
    	
    	keyScrollUp = COMMON_BUILDER.comment("keyScrollUp").define("keyScrollUp", GLFW.GLFW_KEY_F8);
    	keyScrollDown = COMMON_BUILDER.comment("keyScrollDown").define("keyScrollDown", GLFW.GLFW_KEY_F9);
    	
    	keySearch = COMMON_BUILDER.comment("keySearch").define("keySearch", GLFW.GLFW_KEY_DOWN);
    	keyDrop = COMMON_BUILDER.comment("keyDrop").define("keyDrop", GLFW.GLFW_KEY_HOME);
    	
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
    	LOGGER.debug("InventoryConfig onLoad");
    }

    @SubscribeEvent
    public static void onReload(final ModConfig.ConfigReloading configEvent) {
    	// the configspec values are updated for us, but we may want to hook into here too?
    	LOGGER.debug("InventoryConfig onReload");        	
    }
    
    //     * Standard Return Key if there is a problem reading the config.
    private static final int retDefKey = -1;
    
}
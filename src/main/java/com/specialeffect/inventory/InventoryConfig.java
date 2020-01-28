package com.specialeffect.inventory;

import java.nio.file.Path;

import org.lwjgl.glfw.GLFW;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(bus = Bus.MOD)
public class InventoryConfig {
	// Based on McJty/YouTubeModding14 tutorial, MIT license:
	// https://github.com/McJty/YouTubeModding14/blob/master/LICENSE
	
    public static final String CATEGORY_GENERAL = "general";
    
    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

    public static ForgeConfigSpec COMMON_CONFIG;
    public static ForgeConfigSpec CLIENT_CONFIG;

    public static ForgeConfigSpec.IntValue  key0, key1, key2, key3, key4,
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
    	
    	key0 = (IntValue) COMMON_BUILDER.comment("key0").define("key0", GLFW.GLFW_KEY_KP_0);
    	key1 = (IntValue) COMMON_BUILDER.comment("key0").define("key1", GLFW.GLFW_KEY_KP_1);
    	key2 = (IntValue) COMMON_BUILDER.comment("key0").define("key2", GLFW.GLFW_KEY_KP_2);
    	key3 = (IntValue) COMMON_BUILDER.comment("key0").define("key3", GLFW.GLFW_KEY_KP_3);
    	key4 = (IntValue) COMMON_BUILDER.comment("key0").define("key4", GLFW.GLFW_KEY_KP_4);
    	key5 = (IntValue) COMMON_BUILDER.comment("key0").define("key5", GLFW.GLFW_KEY_KP_5);
    	key6 = (IntValue) COMMON_BUILDER.comment("key0").define("key6", GLFW.GLFW_KEY_KP_6);
    	key7 = (IntValue) COMMON_BUILDER.comment("key0").define("key7", GLFW.GLFW_KEY_KP_7);
    	key8 = (IntValue) COMMON_BUILDER.comment("key0").define("key8", GLFW.GLFW_KEY_KP_8);
    	key9 = (IntValue) COMMON_BUILDER.comment("key0").define("key9", GLFW.GLFW_KEY_KP_9);
    			
    }
    
    private static void setupNavKeys() {    			

    	keyPrev = (IntValue) COMMON_BUILDER.comment("key0").define("keyPrev", GLFW.GLFW_KEY_LEFT);
    	keyNext = (IntValue) COMMON_BUILDER.comment("key0").define("keyNext", GLFW.GLFW_KEY_RIGHT);
    	keyNextItemRow = (IntValue) COMMON_BUILDER.comment("key0").define("keyNextItemRow", GLFW.GLFW_KEY_F6);
    	keyNextItemCol = (IntValue) COMMON_BUILDER.comment("key0").define("keyNextItemCol", GLFW.GLFW_KEY_F7);
    	
    	keyScrollUp = (IntValue) COMMON_BUILDER.comment("key0").define("keyScrollUp", GLFW.GLFW_KEY_F8);
    	keyScrollDown = (IntValue) COMMON_BUILDER.comment("key0").define("keyScrollDown", GLFW.GLFW_KEY_F9);
    	
    	keySearch = (IntValue) COMMON_BUILDER.comment("key0").define("keySearch", GLFW.GLFW_KEY_DOWN);
    	keyDrop = (IntValue) COMMON_BUILDER.comment("key0").define("keyDrop", GLFW.GLFW_KEY_HOME);
    	
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
    	System.out.println("InventoryConfig onLoad");
    }

    @SubscribeEvent
    public static void onReload(final ModConfig.ConfigReloading configEvent) {
    	// the configspec values are updated for us, but we may want to hook into here too?
    	System.out.println("InventoryConfig onReload");        	
    }
    

}
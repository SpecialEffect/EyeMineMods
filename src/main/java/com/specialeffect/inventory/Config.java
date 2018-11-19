package com.specialeffect.inventory;


import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import de.skate702.craftingkeys.CraftingKeys;
import de.skate702.craftingkeys.util.Logger;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.lwjgl.input.Keyboard;

import com.specialeffect.mods.EyeGaze;

/**
 * Configuration Management for Keys, etc.
 * Heavily based on CraftingKeys.Config
 */
public class Config {


    /**
     * Defines the config string for the keys-category.
     */
    private static final String categoryInventory = "inventory";
    /**
     * Standard Return Key if there is a problem reading the config.
     */
    private static final int retDefKey = -1;
    
    /**
     * Defines all 11 Keys you can use with Crafting Keys.
     */
    static Property key0, key1, key2, key3, key4,
    			    key5, key6, key7, key8, key9, 
    			    keyNext, keyPrev, keySearch,
    			    keyScrollUp, keyScrollDown,
    			    keyNextItemRow, keyNextItemCol, keyDrop;
    
    /**
     * Provides the Suggested Config File.
     */
    private static Configuration configFile = null;
    /**
     * Defines, if NumPad is always active for crafting.
     */
    private static Property enableNumPad;

    private static Map<Property, Boolean> keyStates = new HashMap<Property, Boolean>();
    
    private static void flagKeyIfMatches(Property keyProp) {
    	if (Keyboard.getEventKey() == keyProp.getInt(retDefKey)) {
			keyStates.put(keyProp, true);
		}
    }
     
    public static void pollKeyPresses() {
    	// Reset states
    	for (Map.Entry<Property, Boolean> entry : keyStates.entrySet()) {
    	    entry.setValue(false);
    	}
    	
    	// Query keyboard events since last polled. 
    	// NOTE: This is a little bit fiddly. We need to make sure we iterate over all key presses
    	// since last poll, otherwise we'll miss the 'instantaneous' key presses that come 
    	// from OptiKey. However, we don't want to consume events that others are
    	// listening to, so we need to make sure the current guiscreen gets first access
    	// to every event.
    	// A better approach would be to subscribe to GuiScreenEvent.KeyboardInputEvent, but
    	// I'm not sure how to do this with a vanilla (not custom) GUI screen.
    	while (Keyboard.next()) {
	    	try {
				// First offer the key event to the vanilla GUI
				Minecraft.getMinecraft().currentScreen.handleKeyboardInput();
				
				// Now flag appropriate key if it's relevant to us.
				if (Keyboard.getEventKeyState()) {
					flagKeyIfMatches(key0); 
					flagKeyIfMatches(key1); 
					flagKeyIfMatches(key2); 
					flagKeyIfMatches(key3); 
					flagKeyIfMatches(key4); 
					flagKeyIfMatches(key5); 
					flagKeyIfMatches(key6); 
					flagKeyIfMatches(key7); 
					flagKeyIfMatches(key8); 
					flagKeyIfMatches(key9); 
					flagKeyIfMatches(keyPrev); 
					flagKeyIfMatches(keyNext); 
					flagKeyIfMatches(keyNextItemRow); 
					flagKeyIfMatches(keyNextItemCol); 
					flagKeyIfMatches(keySearch);
					flagKeyIfMatches(keyDrop);
					flagKeyIfMatches(keyScrollUp);
					flagKeyIfMatches(keyScrollDown);				
				}
			} catch (IOException e) {
			} catch (NullPointerException e) {
			}
    	}
    }
    
    private static boolean isKeyPressed(Property keyProp) {
    	return keyStates.containsKey(keyProp) && keyStates.get(keyProp);
    }
     
    public static boolean isKey0Pressed() {
    	return isKeyPressed(key0);
    }

    public static boolean isKey1Pressed() {
    	return isKeyPressed(key1);
    }

    public static boolean isKey2Pressed() {
    	return isKeyPressed(key2);
    }

    public static boolean isKey3Pressed() {
    	return isKeyPressed(key3);
    }

    public static boolean isKey4Pressed() {
    	return isKeyPressed(key4);
    }

    public static boolean isKey5Pressed() {
    	return isKeyPressed(key5);
    }

    public static boolean isKey6Pressed() {
    	return isKeyPressed(key6);
    }

    public static boolean isKey7Pressed() {
    	return isKeyPressed(key7);
    }

    public static boolean isKey8Pressed() {
    	return isKeyPressed(key8);
    }

    public static boolean isKey9Pressed() {
    	return isKeyPressed(key9);
    }   
    
    public static boolean isKeyNextRowPressed() {
    	return isKeyPressed(keyNextItemRow);
    }    
    
    public static boolean isKeyNextColPressed() {
    	return isKeyPressed(keyNextItemCol);
    }    

    public static boolean isKeyDropPressed() {
    	return isKeyPressed(keyDrop);
    }    
    
    public static boolean isKeyPrevPressed() {
    	return isKeyPressed(keyPrev);
    }
    
    public static boolean isKeyNextPressed() {
    	return isKeyPressed(keyNext);
    }
    
	public static boolean isKeySearchPressed() {
    	return isKeyPressed(keySearch);
    }
	
	public static boolean isScrollUpPressed() {
    	return isKeyPressed(keyScrollUp);
    }
	
	public static boolean isScrollDownPressed() {
    	return isKeyPressed(keyScrollDown);
    }
    
    /**
     * Initializes the configFile Files, loads all values (or sets them to default).
     *
     * @param event PreInitEvent from Main Class
     */
    public static void loadConfig(FMLPreInitializationEvent event) {

        configFile = new Configuration(event.getSuggestedConfigurationFile(), EyeGaze.VERSION);

        configFile.load();

        syncConfig();

    }

    /**
     * Syncs and saves the configFile file.
     */
    public static void syncConfig() {

        if (configFile == null) {
            Logger.error("syncConfig()", "Unable to read config file!");
            return;
        }

        syncProperties();

        if (configFile.hasChanged())
            configFile.save();

    }

    /**
     * Loads all properties from the configFile file.
     */
    private static void syncProperties() {
    	// It's important any keys here don't get captured by the search box,
    	// otherwise you get stuck in search tab.
    	// This limits us to non-character keys, including numpad numbers.
    	
        // Standard Keys
        key0 = configFile.get(categoryInventory, "key0", Keyboard.KEY_NUMPAD0);
        key1 = configFile.get(categoryInventory, "key1", Keyboard.KEY_NUMPAD1);
        key2 = configFile.get(categoryInventory, "key2", Keyboard.KEY_NUMPAD2);
        key3 = configFile.get(categoryInventory, "key3", Keyboard.KEY_NUMPAD3);
        key4 = configFile.get(categoryInventory, "key4", Keyboard.KEY_NUMPAD4);
       
        key5 = configFile.get(categoryInventory, "key5", Keyboard.KEY_NUMPAD5);
        key6 = configFile.get(categoryInventory, "key6", Keyboard.KEY_NUMPAD6);
        key7 = configFile.get(categoryInventory, "key7", Keyboard.KEY_NUMPAD7);
        key8 = configFile.get(categoryInventory, "key8", Keyboard.KEY_NUMPAD8);
        key9 = configFile.get(categoryInventory, "key9", Keyboard.KEY_NUMPAD9);

        keyPrev = configFile.get(categoryInventory, "keyPrev", Keyboard.KEY_LEFT);
        keyNext = configFile.get(categoryInventory, "keyNext", Keyboard.KEY_RIGHT);
        keyNextItemRow = configFile.get(categoryInventory, "keyNextItemRow", Keyboard.KEY_F6);
        keyNextItemCol = configFile.get(categoryInventory, "keyNextItemCol", Keyboard.KEY_F7);
        
        keyScrollUp = configFile.get(categoryInventory, "keyScrollUp", Keyboard.KEY_F8);
        keyScrollDown = configFile.get(categoryInventory, "keyScrollDown", Keyboard.KEY_F9);
        
        keySearch = configFile.get(categoryInventory, "keySearch", Keyboard.KEY_DOWN);
        keyDrop = configFile.get(categoryInventory, "keyDrop", Keyboard.KEY_HOME);
   }

}

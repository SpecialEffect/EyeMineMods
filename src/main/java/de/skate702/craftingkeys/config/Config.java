package de.skate702.craftingkeys.config;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import de.skate702.craftingkeys.CraftingKeys;
import de.skate702.craftingkeys.util.Logger;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Configuration Management for Keys, etc.
 */
public class Config {


    /**
     * Defines the config string for the keys-category.
     */
    private static final String categoryKeys = "keys";

    /**
     * Defines the config string for the other-category.
     */
    private static final String categoryOther = "other";

    /**
     * Standard Return Key if there is a problem reading the config.
     */
    private static final int retDefKey = -1;
    /**
     * Defines all 11 Keys you can use with Crafting Keys.
     */
    static Property keyTopLeft, keyTopCenter, keyTopRight,
            keyCenterLeft, keyCenterCenter, keyCenterRight,
            keyLowerLeft, keyLowerCenter, keyLowerRight,
            keyStack, keyInteract, keyDrop;
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
					flagKeyIfMatches(keyTopLeft); 
					flagKeyIfMatches(keyTopCenter); 
					flagKeyIfMatches(keyTopRight);
					flagKeyIfMatches(keyCenterLeft); 
					flagKeyIfMatches(keyCenterCenter); 
					flagKeyIfMatches(keyCenterRight);
					flagKeyIfMatches(keyLowerLeft); 
					flagKeyIfMatches(keyLowerCenter); 
					flagKeyIfMatches(keyLowerRight);
					flagKeyIfMatches(keyStack); 
					flagKeyIfMatches(keyInteract); 
					flagKeyIfMatches(keyDrop);
				}
			} catch (IOException e) {
			} catch (NullPointerException e) {
			}
    	}
    }
    
    private static boolean isKeyPressed(Property keyProp) {
    	return keyStates.containsKey(keyProp) && keyStates.get(keyProp);
    }
     
    public static boolean isKeyTopLeftPressed() {   	
    	return isKeyPressed(keyTopLeft);
    }

    public static boolean isKeyTopCenterPressed() {
    	return isKeyPressed(keyTopCenter);
    }

    public static boolean isKeyTopRightPressed() {
    	return isKeyPressed(keyTopRight);
    }

    public static boolean isKeyCenterLeftPressed() {
    	return isKeyPressed(keyCenterLeft);
    }

    public static boolean isKeyCenterCenterPressed() {
    	return isKeyPressed(keyCenterCenter);
    }

    public static boolean isKeyCenterRightPressed() {
    	return isKeyPressed(keyCenterRight);
    }

    public static boolean isKeyLowerLeftPressed() {
    	return isKeyPressed(keyLowerLeft);
    }

    public static boolean isKeyLowerCenterPressed() {
    	return isKeyPressed(keyLowerCenter);
    }

    public static boolean isKeyLowerRightPressed() {
    	return isKeyPressed(keyLowerRight);
    }

    public static boolean isKeyStackPressed() {
    	return isKeyPressed(keyStack);
    }

    public static boolean isKeyInteractPressed() {
    	return isKeyPressed(keyInteract);
    }

    public static boolean isKeyDropPressed() {
    	return isKeyPressed(keyDrop);
    }

    @SuppressWarnings("unused")
	private static boolean isNumPadEnabled() {
        return enableNumPad.getBoolean(true);
    }

    /**
     * Initializes the configFile Files, loads all values (or sets them to default).
     *
     * @param event PreInitEvent from Main Class
     */
    public static void loadConfig(FMLPreInitializationEvent event) {

        configFile = new Configuration(event.getSuggestedConfigurationFile(), CraftingKeys.VERSION);

        configFile.load();

        genComments();

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
     * Generates comments for easier understanding of the categories.
     */
    private static void genComments() {
        configFile.addCustomCategoryComment(categoryKeys, "Keyboard codes based on http://minecraft.gamepedia.com/Key_codes");
        configFile.addCustomCategoryComment(categoryOther, "Other settings which have effects @ crafting keys");
    }

    /**
     * Loads all properties from the configFile file.
     */
    private static void syncProperties() {
        // Standard Keys

        keyTopLeft = configFile.get(categoryKeys, "keyTopLeft", Keyboard.KEY_R);
        keyTopCenter = configFile.get(categoryKeys, "keyTopCenter", Keyboard.KEY_T);
        keyTopRight = configFile.get(categoryKeys, "keyTopRight", Keyboard.KEY_Y);

        keyCenterLeft = configFile.get(categoryKeys, "keyCenterLeft", Keyboard.KEY_F);
        keyCenterCenter = configFile.get(categoryKeys, "keyCenterCenter", Keyboard.KEY_G);
        keyCenterRight = configFile.get(categoryKeys, "keyCenterRight", Keyboard.KEY_H);

        keyLowerLeft = configFile.get(categoryKeys, "keyLowerLeft", Keyboard.KEY_V);
        keyLowerCenter = configFile.get(categoryKeys, "keyLowerCenter", Keyboard.KEY_B);
        keyLowerRight = configFile.get(categoryKeys, "keyLowerRight", Keyboard.KEY_N);

        // Special Keys

        keyStack = configFile.get(categoryKeys, "keyStack", Keyboard.KEY_LSHIFT);
        keyInteract = configFile.get(categoryKeys, "keyInteract", Keyboard.KEY_U);
        keyDrop = configFile.get(categoryKeys, "keyDrop", Keyboard.KEY_SPACE);

        // Other Settings

        enableNumPad = configFile.get(categoryOther, "numPadEnabled", true, "Activates the NumPad for crafting");
    }

}

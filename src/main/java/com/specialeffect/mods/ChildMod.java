package com.specialeffect.mods;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ChildMod {
	
    // Directly reference a log4j logger.	
    @SuppressWarnings("unused")
	public static final Logger LOGGER = LogManager.getLogger();

	public void setup(final FMLCommonSetupEvent event) {
		// can be overridden if mod has setup to do
	}
//	public void onKeyInput(KeyInputEvent event);    	
//	public void onLiving(LivingUpdateEvent event);
//	public void updateIcons();
//	public void syncConfig();
}

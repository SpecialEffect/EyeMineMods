/**
 * Copyright (C) 2016 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.mods.misc;


import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.utils.CommonStrings;

//import 
//import net.java.games.input.Keyboard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings.Input;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lwjgl.glfw.GLFW;



@Mod(PickBlock.MODID)
public class PickBlock extends BaseClassWithCallbacks {
	public static final String MODID = "pickblock";
	public static final String NAME = "PickBlock";
    private static final Logger LOGGER = LogManager.getLogger();

	public static KeyBinding mPickBlockKB;

	public PickBlock() {
	    
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);      
        
        // Register key bindings
		mPickBlockKB = new KeyBinding("Pick block", GLFW.GLFW_KEY_KP_2, CommonStrings.EYEGAZE_COMMON);
		ClientRegistry.registerKeyBinding(mPickBlockKB);
    }
	
	@SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {   
		if (mPickBlockKB.isPressed()) {
	        LOGGER.info("HELLO Key pressed");
	        final Input pickBlockKey = Minecraft.getInstance().gameSettings.keyBindPickBlock.getKey();
			System.out.println("pickblock onKeykey is pressed");
			KeyBinding.onTick(pickBlockKey);
		}
    }
	
}

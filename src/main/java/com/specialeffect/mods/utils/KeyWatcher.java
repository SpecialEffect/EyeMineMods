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

package com.specialeffect.mods.utils;


import org.lwjgl.glfw.GLFW;

import com.specialeffect.mods.ChildMod;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class KeyWatcher extends ChildMod
{

	public final String MODID = "keyWatcher";

	public static boolean f3Pressed = false;
	public static long f3PressedTime = 0;
	
	// Since we can't be sure we'll get all matching press/releases, (for instance a 
	// key might be released while Minecraft has lost focus), any "key down" state will 
	// time-out relatively quickly
	private long timeoutMillis = 2000;
	
	public KeyWatcher() {
	}
	
	public void setup(final FMLCommonSetupEvent event) {
		
	}

	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {
		if (f3Pressed) {
			if (System.currentTimeMillis() - f3PressedTime > timeoutMillis) {
				f3Pressed = false;
			}
		}
	}
	
	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {		    		

		if (event.getKey() == GLFW.GLFW_KEY_F3) {
			if (event.getAction() == GLFW.GLFW_PRESS) { 					
				f3Pressed = true;
				f3PressedTime = System.currentTimeMillis();
			}
			else if (event.getAction() == GLFW.GLFW_RELEASE) {				
				f3Pressed = false;
			}
		}	    
	}
	
}

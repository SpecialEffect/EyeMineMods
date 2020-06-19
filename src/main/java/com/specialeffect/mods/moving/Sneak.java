/**
 * Copyright (C) 2016 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.mods.moving;

import org.lwjgl.glfw.GLFW;

import com.specialeffect.gui.StateOverlay;
import com.specialeffect.mods.ChildMod;
import com.specialeffect.mods.utils.KeyWatcher;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;


public class Sneak extends ChildMod {

	public final String MODID = "sneaktoggle";

	private static KeyBinding mSneakKB;
	private static boolean mIsSneaking = false;
	
	private static int mIconIndex;
	
	public Sneak() {
	}
	
	public void setup(final FMLCommonSetupEvent event) {
		// Register key bindings
		mSneakKB = new KeyBinding("Start/stop sneaking", GLFW.GLFW_KEY_Z, CommonStrings.EYEGAZE_EXTRA);
		ClientRegistry.registerKeyBinding(mSneakKB);
		
		// Register an icon for the overlay
		mIconIndex = StateOverlay.registerTextureLeft("specialeffect:icons/sneak.png");
	}
	
	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {
		PlayerEntity player = Minecraft.getInstance().player;
    	if (null != player && event.phase == TickEvent.Phase.START) {			
			player.setSneaking(mIsSneaking);
			// Make sure icon up to date
			StateOverlay.setStateLeftIcon(mIconIndex, mIsSneaking);    		
		}
	}
	
	public static void stop() {		
		updateSneak(false);
	}	
	
	private static void updateSneak(boolean bSneak) {
		
		if ( bSneak != mIsSneaking ) {
			ModUtils.sendPlayerMessage("Sneaking: " + (bSneak ? "ON" : "OFF"));
		}
		
		mIsSneaking = bSneak;

		// TODO: is there any reason we don't want to just hold down the key here? 
		// this also helps with mod tooltips
		
		// FIXME: remove movement override stuff if not using for Sneak  

		final KeyBinding useItemKeyBinding = Minecraft.getInstance().gameSettings.keyBindSneak;			
		KeyBinding.setKeyBindState(useItemKeyBinding.getKey(), bSneak);			
		
		// Make sure icon up to date?
		StateOverlay.setStateLeftIcon(mIconIndex, mIsSneaking);    		
	}

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {
	    
	    if (ModUtils.hasActiveGui()) { return; }	    
	    if (event.getAction() != GLFW.GLFW_PRESS) { return; }

	    if (KeyWatcher.f3Pressed) { return; }

		if(mSneakKB.getKey().getKeyCode() == event.getKey()) {			
			updateSneak(!mIsSneaking);
		}
	}

}

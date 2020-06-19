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
import com.specialeffect.mods.EyeMineConfig;
import com.specialeffect.mods.utils.KeyWatcher;
import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class AutoJump  extends ChildMod implements ChildModWithConfig {
	public final String MODID = "autojump";

	public static KeyBinding autoJumpKeyBinding;
	// FIXME public static Configuration mConfig;

	private boolean mDoingAutoJump = true;
	private int mIconIndex;

	public void setup(final FMLCommonSetupEvent event) {

		// Register key bindings
		autoJumpKeyBinding = new KeyBinding("Turn auto-jump on/off", GLFW.GLFW_KEY_J, CommonStrings.EYEGAZE_COMMON);
		ClientRegistry.registerKeyBinding(autoJumpKeyBinding);

		// Register an icon for the overlay
		mIconIndex = StateOverlay.registerTextureLeft("specialeffect:icons/jump.png");

		//FIXME: there was previously an ordering requirement for config syncing and 
		// texture registering
	}
	
	private void updateSettings(boolean autoJump) {
		Minecraft.getInstance().gameSettings.autoJump = autoJump;
		Minecraft.getInstance().gameSettings.saveOptions();
		Minecraft.getInstance().gameSettings.loadOptions();
	}

	public void syncConfig() {
		mDoingAutoJump = EyeMineConfig.defaultDoAutoJump.get();
		this.updateSettings(mDoingAutoJump);
		StateOverlay.setStateLeftIcon(mIconIndex, mDoingAutoJump);
	}

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {	
		
		if (ModUtils.hasActiveGui()) { return; }	    
	    if (event.getAction() != GLFW.GLFW_PRESS) { return; }

	    if (KeyWatcher.f3Pressed) { return; }

		if (autoJumpKeyBinding.getKey().getKeyCode() == event.getKey()) {
			mDoingAutoJump = !mDoingAutoJump;
			this.updateSettings(mDoingAutoJump);
			StateOverlay.setStateLeftIcon(mIconIndex, mDoingAutoJump);			
			ModUtils.sendPlayerMessage("Auto jump: " + (mDoingAutoJump ? "ON" : "OFF"));
		}
	}
}

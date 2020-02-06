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
	public static final String MODID = "autojump";
	public static final String NAME = "AutoJump";

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

	public void syncConfig() {
		mDoingAutoJump = EyeMineConfig.defaultDoAutoJump.get();
		// Turn off vanilla autojump since it doesn't play nicely with
		// our gaze-based walking methods.
		Minecraft.getInstance().gameSettings.autoJump = mDoingAutoJump;
		StateOverlay.setStateLeftIcon(mIconIndex, mDoingAutoJump);
	}

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {	
		if (ModUtils.hasActiveGui()) { return; }
		
		if (autoJumpKeyBinding.isPressed()) {
			mDoingAutoJump = !mDoingAutoJump;
			StateOverlay.setStateLeftIcon(mIconIndex, mDoingAutoJump);			
			ModUtils.sendPlayerMessage("Auto jump: " + (mDoingAutoJump ? "ON" : "OFF"));
		}
	}
}

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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class AutoJump  extends ChildMod implements ChildModWithConfig {
	public final String MODID = "autojump";

	public static KeyBinding autoJumpKeyBinding;

	private boolean mDoingAutoJump = true;
	private int mIconIndex;

	public void setup(final FMLCommonSetupEvent event) {

		// Register key bindings
		autoJumpKeyBinding = new KeyBinding("Turn auto-jump on/off", GLFW.GLFW_KEY_J, CommonStrings.EYEGAZE_COMMON);
		ClientRegistry.registerKeyBinding(autoJumpKeyBinding);

		// Register an icon for the overlay
		mIconIndex = StateOverlay.registerTextureLeft("specialeffect:icons/jump.png");
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
    public void onClientTick(ClientTickEvent event) {
		PlayerEntity player = Minecraft.getInstance().player;
    	if (null != player && event.phase == TickEvent.Phase.START) {

			// We can't rely solely on the vanilla autojump implementation,
			// since there are a few scenarios where it doesn't work correctly, see
			// https://bugs.mojang.com/browse/MC-102043
			// 
			// We'll keep it in sync though so that keyboard-play is consistent
			// with our autojump state (if you're moving with the keyboard you
			// get visually-nicer autojump behaviour).
			if (mDoingAutoJump) {
				player.stepHeight = 1.0f;
			}
			else {
				player.stepHeight = 0.6f;
			}	    		
    	}
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

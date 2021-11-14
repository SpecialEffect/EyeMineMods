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

package com.specialeffect.eyemine.submod.movement;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;
import com.specialeffect.eyemine.client.Keybindings;
import com.specialeffect.eyemine.client.gui.crosshair.StateOverlay;
import com.specialeffect.eyemine.submod.SubMod;
import com.specialeffect.eyemine.utils.KeyboardInputHelper;
import com.specialeffect.utils.ModUtils;
import me.shedaniel.architectury.event.events.client.ClientRawInputEvent;
import me.shedaniel.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionResult;
import org.lwjgl.glfw.GLFW;

public class Sneak extends SubMod {
	public final String MODID = "sneaktoggle";

	private static KeyMapping mSneakKB;
	private static boolean mIsSneaking = false;
	
	private static int mIconIndex;
	
	public Sneak() {
	}

	public void onInitializeClient() {
		// Register key bindings
		Keybindings.keybindings.add(mSneakKB = new KeyMapping(
				"key.eyemine.toggle_sneaking",
				Type.KEYSYM,
				GLFW.GLFW_KEY_Z,
				"category.eyemine.category.eyegaze_extra" // The translation key of the keybinding's category.
		));
		
		// Register an icon for the overlay
		mIconIndex = StateOverlay.registerTextureLeft("eyemine:textures/icons/sneak.png");

		ClientTickEvent.CLIENT_PRE.register(this::onClientTick);
		ClientRawInputEvent.KEY_PRESSED.register(this::onKeyInput);
	}

	public void onClientTick(Minecraft minecraft) {
		LocalPlayer player = minecraft.player;
    	if (player != null) {
    		// Sneaking is handled by the MovementInput class these days
       		KeyboardInputHelper.instance().setSneakOverride(mIsSneaking);
       		
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

		// we both hold down the key here (helps with mod tooltips) *and*
		// override the movementInput each tick (more robust to lost focus etc)
		final KeyMapping useItemKeyBinding = Minecraft.getInstance().options.keyShift;
		KeyMapping.set(useItemKeyBinding.getDefaultKey(), bSneak);

		// Make sure icon up to date?
		StateOverlay.setStateLeftIcon(mIconIndex, mIsSneaking);
	}

	private InteractionResult onKeyInput(Minecraft minecraft, int keyCode, int scanCode, int action, int modifiers) {
		if (ModUtils.hasActiveGui()) { return InteractionResult.PASS; }

		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 292)) { return InteractionResult.PASS; }

		if(mSneakKB.matches(keyCode, scanCode) && mSneakKB.consumeClick()) {
			updateSneak(!mIsSneaking);
		}
		return InteractionResult.PASS;
	}

}

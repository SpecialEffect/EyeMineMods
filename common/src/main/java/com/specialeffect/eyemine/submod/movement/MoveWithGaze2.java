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
import com.specialeffect.eyemine.client.MainClientHandler;
import com.specialeffect.eyemine.client.gui.crosshair.JoystickControlOverlay;
import com.specialeffect.eyemine.client.gui.crosshair.StateOverlay;
import com.specialeffect.eyemine.platform.EyeMineConfig;
import com.specialeffect.eyemine.submod.IConfigListener;
import com.specialeffect.eyemine.submod.SubMod;
import com.specialeffect.eyemine.submod.mouse.MouseHandlerMod;
import com.specialeffect.eyemine.utils.KeyboardInputHelper;
import com.specialeffect.eyemine.utils.MouseHelper;
import com.specialeffect.utils.ModUtils;
import me.shedaniel.architectury.event.events.client.ClientRawInputEvent;
import me.shedaniel.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionResult;
import org.lwjgl.glfw.GLFW;

public class MoveWithGaze2 extends SubMod implements IConfigListener {
	public static final String MODID = "specialeffect.movewithgaze2";
    public static final String NAME = "MoveWithGaze2";

    private static KeyMapping mToggleAutoWalkKB;
    
    private static boolean mMoveWhenMouseStationary = false;
    private static float mCustomSpeedFactor = 0.8f;

	public void onInitializeClient() {
    	mOverlay = new JoystickControlOverlay();
		MainClientHandler.addOverlayToRender(mOverlay);

    	// Register key bindings	
		Keybindings.keybindings.add(mToggleAutoWalkKB = new KeyMapping(
				"key.eyemine.toggle_walking", //Careful walk
				Type.KEYSYM,
				GLFW.GLFW_KEY_B,
				"category.eyemine.category.eyegaze_common" // The translation key of the keybinding's category.
		));

		// Register an icon for the overlay
		mIconIndex = StateOverlay.registerTextureLeft("eyemine:textures/icons/legacy-mode.png");

		//Initialize from Config
		syncConfig();

		ClientTickEvent.CLIENT_PRE.register(this::onClientTick);
		ClientRawInputEvent.KEY_PRESSED.register(this::onKeyInput);
    }
    
    private static int mIconIndex;
    
	private static JoystickControlOverlay mOverlay;

	public static void stop() {
		if (mDoingAutoWalk) {
			mDoingAutoWalk = false;
			StateOverlay.setStateLeftIcon(mIconIndex, false);        	
    		mOverlay.setVisible(false);
		}
    }

    @Override
	public void syncConfig() {
        mMoveWhenMouseStationary = EyeMineConfig.getMoveWhenMouseStationary();
        mCustomSpeedFactor = EyeMineConfig.getCustomSpeedFactor();
        // We need to scale the alpha since the texture here gets stretched a lot so it's quite soft already
        mOverlay.setAlpha(2.5f * EyeMineConfig.getFullscreenOverlayAlpha());
	}
	
	// Some hard-coded fudge factors for maximums.
	// TODO: make configurable?
	private float mMaxForward = 1.5f;
	private float mMaxBackward = 0.5f;
	
	private int mTicksToLockOn = 5; // wait this # of ticks before acting in a new region
    private int ticksForward = 0;
    private int ticksBackward = 0;

    public void onClientTick(Minecraft minecraft) {
		LocalPlayer player = Minecraft.getInstance().player;
    	if (player != null) {
			if (mDoingAutoWalk && minecraft.screen == null && // no gui visible
	    		(mMoveWhenMouseStationary || MouseHandlerMod.hasPendingEvent()) ) {

				double lastMouseY = MouseHelper.lastYVelocity;
				
				// Y gives distance to walk forward/back.
				float walkForwardAmount = 0.0f;
				float h = (float)minecraft.getWindow().getGuiScaledHeight();
				float h6 = h/6.0f;
				
				if (lastMouseY < -h6) {
					// top of screen: forward!
					if (ticksForward > mTicksToLockOn) {    				
						walkForwardAmount = (float) (mMaxForward*(-lastMouseY-h6)/h6);
						ticksBackward = 0;
					} else {
						ticksForward++;	    					
					}
				} else if (lastMouseY > h6) {
					// backward
					
					if (ticksBackward > mTicksToLockOn) {    				
						// backward!
						walkForwardAmount = (float) (-mMaxBackward*(lastMouseY - h6)/h6);
						ticksForward = 0;
					} else {
						ticksBackward++;
					}
				}
				
				// scaled by mCustomSpeedFactor 
				walkForwardAmount *= 0.15;
				walkForwardAmount *= mCustomSpeedFactor;
				KeyboardInputHelper.setWalkOverride(true, walkForwardAmount);
			}	
    	}
    }
    
	private static boolean mDoingAutoWalk = false;

	private InteractionResult onKeyInput(Minecraft minecraft, int keyCode, int scanCode, int action, int modifiers) {
		if (ModUtils.hasActiveGui()) { return InteractionResult.PASS; }

		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 292)) { return InteractionResult.PASS; }

        if(mToggleAutoWalkKB.matches(keyCode, scanCode) && mToggleAutoWalkKB.consumeClick()) {
        	mDoingAutoWalk = !mDoingAutoWalk;        	
        	MouseHandlerMod.setLegacyWalking(mDoingAutoWalk);
        	
        	mOverlay.setVisible(mDoingAutoWalk);
			StateOverlay.setStateLeftIcon(mIconIndex, mDoingAutoWalk);
			ModUtils.sendPlayerMessage("Auto walk: " + (mDoingAutoWalk ? "ON" : "OFF"));
        }
		return InteractionResult.PASS;
    }
}
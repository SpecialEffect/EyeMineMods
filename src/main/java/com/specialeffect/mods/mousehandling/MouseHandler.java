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

package com.specialeffect.mods.mousehandling;


import java.text.DecimalFormat;

import org.lwjgl.glfw.GLFW;

import com.specialeffect.gui.IconOverlay;
import com.specialeffect.mods.ChildMod;
import com.specialeffect.mods.EyeMineConfig;
import com.specialeffect.mods.mousehandling.MouseHelperOwn.PlayerMovement;
import com.specialeffect.mods.moving.MoveWithGaze;
import com.specialeffect.mods.moving.MoveWithGaze2;
import com.specialeffect.mods.utils.KeyWatcher;
import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import at.feldim2425.moreoverlays.gui.ConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class MouseHandler  extends ChildMod implements ChildModWithConfig {
	public final String MODID = "mousehandler";

	//public static Configuration mConfig;

	public enum InteractionState {
	    EYETRACKER_NORMAL, EYETRACKER_WALK, EYETRACKER_LEGACY, 
	    MOUSE_NOTHING, MOUSE_LOOK, MOUSE_WALK, MOUSE_LEGACY 
	}
	
	public static InteractionState mInteractionState;
	private static InputSource mInputSource = InputSource.EyeTracker;
	
	private static MouseHelperOwn ownMouseHelper;

	private static KeyBinding mSensitivityUpKB;
	private static KeyBinding mSensitivityDownKB;
	private static KeyBinding mToggleMouseViewControlKB;

	private static IconOverlay mIconEye;
	
	private static int mTicksSinceMouseEvent = 1000;
	
	private boolean hasPendingConfigChange = false;

	public void setup(final FMLCommonSetupEvent event) {
		
		// Set up icon rendering		
		mIconEye = new IconOverlay(Minecraft.getInstance(), "specialeffect:icons/eye.png");
		mIconEye.setPosition(0.5f,  0.5f, 0.1f, 1.9f);
		mIconEye.setAlpha(EyeMineConfig.fullscreenOverlayAlpha.get().floatValue());
		mIconEye.setVisible(false);								

		// Register key bindings
		mSensitivityUpKB = new KeyBinding("Turn mouse sensitivity up", GLFW.GLFW_KEY_RIGHT, CommonStrings.EYEGAZE_SETTINGS);
		ClientRegistry.registerKeyBinding(mSensitivityUpKB);

		mSensitivityDownKB = new KeyBinding("Turn mouse sensitivity down", GLFW.GLFW_KEY_LEFT, CommonStrings.EYEGAZE_SETTINGS);
		ClientRegistry.registerKeyBinding(mSensitivityDownKB);

		// Used to turn 'look with gaze' on and off when using mouse emulation
		// instead of an
		// eyetracker
		mToggleMouseViewControlKB = new KeyBinding("Toggle look with gaze (using mouse)", GLFW.GLFW_KEY_Y, CommonStrings.EYEGAZE_EXTRA);
		ClientRegistry.registerKeyBinding(mToggleMouseViewControlKB);

		MinecraftForge.EVENT_BUS.register(mIconEye);
		
		// Set up mouse helper to handle view control
		ownMouseHelper = new MouseHelperOwn(Minecraft.getInstance());
		Minecraft.getInstance().mouseHelper = (MouseHelper)ownMouseHelper;
		
		// Re-bind GLFW callbacks to new helper		
		long window = Minecraft.getInstance().mainWindow.getHandle();
		ownMouseHelper.registerCallbacks(window);
		
		// Turn off raw mouse input: this wreaks havoc with gaze-provided cursor movements! 
		GLFW.glfwSetInputMode(window, GLFW.GLFW_RAW_MOUSE_MOTION, GLFW.GLFW_FALSE);
		
		// Rejig the state after mouse helper has been created
		setupInitialState(); 
	}

	
	public static void setWalking(boolean doWalk) {
		
		if (mInputSource == InputSource.EyeTracker) {		
			if (doWalk) updateState(InteractionState.EYETRACKER_WALK);
			else updateState(InteractionState.EYETRACKER_NORMAL);
		}
		else {
			if (doWalk) updateState(InteractionState.MOUSE_WALK);
			else updateState(InteractionState.MOUSE_LOOK);
		}
	}
	
	public static void setLegacyWalking(boolean doWalk) {
		if (mInputSource == InputSource.EyeTracker) {
			if (doWalk) updateState(InteractionState.EYETRACKER_LEGACY);
			else updateState(InteractionState.EYETRACKER_NORMAL);
		}
		else {
			if (doWalk) updateState(InteractionState.MOUSE_LEGACY);
			else updateState(InteractionState.MOUSE_LOOK);
		}
	}
	
	private static void updateIconForState(InteractionState state) {
		boolean showIcon = (state == InteractionState.MOUSE_LOOK ||
							state == InteractionState.MOUSE_WALK);
		mIconEye.setVisible(showIcon);								
	}
	
	private static void updateMouseForState(InteractionState state) {
		switch (state) {
		case EYETRACKER_LEGACY:
		case MOUSE_LEGACY:
			ownMouseHelper.setMovementState(PlayerMovement.LEGACY);
			break;
		case MOUSE_NOTHING:
			ownMouseHelper.setMovementState(PlayerMovement.NONE);
			break;
		case EYETRACKER_NORMAL:
		case EYETRACKER_WALK:	
		case MOUSE_LOOK:
		case MOUSE_WALK:
			ownMouseHelper.setMovementState(PlayerMovement.VANILLA);
			break;		
		}
	}
	
	private static void updateState(InteractionState state) {
		mInteractionState = state;
		updateIconForState(state);
		updateMouseForState(state);

		if (state == InteractionState.MOUSE_WALK || state == InteractionState.EYETRACKER_WALK) {
			MoveWithGaze2.stop();
		}
		else if (state == InteractionState.MOUSE_LEGACY || state == InteractionState.EYETRACKER_LEGACY) {
			MoveWithGaze.stop();			
		}
		else {
			MoveWithGaze.stop();
			MoveWithGaze2.stop();
		}
	}

	public void setupInitialState() {
		if (EyeMineConfig.usingMouseEmulation.get()) {
			mInputSource = InputSource.Mouse;
			MouseHandler.updateState(InteractionState.MOUSE_NOTHING);
			ownMouseHelper.setUngrabbedMode(true);
		}
		else {
			mInputSource = InputSource.EyeTracker;
			MouseHandler.updateState(InteractionState.EYETRACKER_NORMAL);
			ownMouseHelper.setUngrabbedMode(false);
		}		
	}
	
		
	public void syncConfig() {	
		// has anything *relevant* changed??
		if ( (EyeMineConfig.usingMouseEmulation.get() &&
				mInputSource == InputSource.EyeTracker) ||
			 (!EyeMineConfig.usingMouseEmulation.get() &&
					 mInputSource == InputSource.Mouse)) {
			
			// wait until we're on GL thread before making changes...
			this.hasPendingConfigChange = true;
		}
		// this is safe to change at any point
		mIconEye.setAlpha(EyeMineConfig.fullscreenOverlayAlpha.get().floatValue());
	}

	private void syncConfigImpl() {
		if (this.hasPendingConfigChange) {
			// These changes need to happen on the main UI thread,   
			// since they need the right GL context	
			setupInitialState();
			this.hasPendingConfigChange = false;
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST) // important we get this
														// *after* other mods
	public void onClientTick(ClientTickEvent event) {
		PlayerEntity player = Minecraft.getInstance().player;
    	if (null != player && event.phase == TickEvent.Phase.START) {

			if (ownMouseHelper.hasPendingEvent()) {
				mTicksSinceMouseEvent = 0;
				ownMouseHelper.consumePendingEvent();
			}
			else {
				mTicksSinceMouseEvent++;
			}
		}
    	
    	if (!(Minecraft.getInstance().currentScreen instanceof ConfigScreen)) {
    		//	Sync config if necessary, but not while in config screen! 
    		syncConfigImpl();	
    	}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST) // important we get this
														// *before* other mods
	public void onKeyInput(KeyInputEvent event) {

		if (ModUtils.hasActiveGui()) { return; }
		if (event.getAction() != GLFW.GLFW_PRESS) { return; }

	    if (KeyWatcher.f3Pressed) { return; }	
		
		if (mSensitivityUpKB.getKey().getKeyCode() == event.getKey()) {
			increaseSens();
			ModUtils.sendPlayerMessage("Sensitivity: " + toPercent(2.0f*Minecraft.getInstance().gameSettings.mouseSensitivity));
		} else if (mSensitivityDownKB.getKey().getKeyCode() == event.getKey()) {
			decreaseSens();
			ModUtils.sendPlayerMessage("Sensitivity: " + toPercent(2.0f*Minecraft.getInstance().gameSettings.mouseSensitivity));						    	
		} else if (mToggleMouseViewControlKB.getKey().getKeyCode() == event.getKey()) {
			if (mInputSource == InputSource.EyeTracker) {
				LOGGER.debug("this key doesn't do anything in eyetracker mode");
				ModUtils.sendPlayerMessage("Warning: Minecraft expects eye tracker input, not mouse");
				ModUtils.sendPlayerMessage("Perhaps you need to switch on 'Mouse emulation' mode in Mod Config -> EyeGaze -> Basic options ?");
			} else {
				if (MouseHandler.mInteractionState == InteractionState.MOUSE_NOTHING) {
					MouseHandler.updateState(InteractionState.MOUSE_LOOK);
				}
				else {
					MouseHandler.updateState(InteractionState.MOUSE_NOTHING);
				}				
			}
		}
	}
	
	private float getSensitivityIncrement(float reference) {
		// Get a roughly-proportional increment
		// bearing in mind offset means we don't just take a linear scale
		float sens = (float) Minecraft.getInstance().gameSettings.mouseSensitivity ;
		float inc = 0.05f;		
		if (sens < 0.2f) {
			inc = 0.01f;
		}
		else if (sens < 0.0f) {
			inc = 0.005f;
		}
		return inc;
	}
	
	private void decreaseSens() {
		float sens = (float) Minecraft.getInstance().gameSettings.mouseSensitivity ;
		sens -= getSensitivityIncrement(sens);
		sens = Math.max(sens, MIN_SENS+0.05f);
		Minecraft.getInstance().gameSettings.mouseSensitivity = sens;
	}

	private void increaseSens() {
		float sens = (float) Minecraft.getInstance().gameSettings.mouseSensitivity ;		
		sens += getSensitivityIncrement(sens);
		sens = Math.min(sens, 1.0f);
		Minecraft.getInstance().gameSettings.mouseSensitivity = sens;
	}
	
	private void setEmptyCursor() {
		GLFW.glfwSetInputMode(Minecraft.getInstance().mainWindow.getHandle(), 
								GLFW.GLFW_CURSOR, 
								GLFW.GLFW_CURSOR_HIDDEN);
	}
	
	private void setNativeCursor() {
		GLFW.glfwSetInputMode(Minecraft.getInstance().mainWindow.getHandle(), 
								GLFW.GLFW_CURSOR, 
								GLFW.GLFW_CURSOR_NORMAL);
	}	

	public void setMouseNotGrabbed() {
		ownMouseHelper.setUngrabbedMode(true);
		this.setEmptyCursor();
	}

	public static boolean hasPendingEvent() {
		return mTicksSinceMouseEvent < 2;
	}
	
	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event) {

		// For any  open event, make sure cursor not overridden
		if (null != event.getGui()) {
			this.setNativeCursor();
		} else {
			// For any close event, make sure we're in the right 'grabbed' state.
			// (also sets cursor if applicable)
			if (mInputSource == InputSource.Mouse) {			
				this.setEmptyCursor();
			}
		}
	}

	// This is the constant offset applied in MC source, corresponding
	// to "mouse does not move"
	private static float MIN_SENS = -1F / 3F;

	String toPercent(double d) {
		DecimalFormat myFormatter = new DecimalFormat("#0.0");
		return myFormatter.format(d * 100) + "%";
	}
}

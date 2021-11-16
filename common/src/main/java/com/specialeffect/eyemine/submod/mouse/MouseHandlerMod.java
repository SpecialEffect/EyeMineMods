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

package com.specialeffect.eyemine.submod.mouse;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;
import com.specialeffect.eyemine.client.Keybindings;
import com.specialeffect.eyemine.client.MainClientHandler;
import com.specialeffect.eyemine.client.gui.crosshair.IconOverlay;
import com.specialeffect.eyemine.platform.EyeMineConfig;
import com.specialeffect.eyemine.submod.IConfigListener;
import com.specialeffect.eyemine.submod.SubMod;
import com.specialeffect.eyemine.submod.movement.MoveWithGaze;
import com.specialeffect.eyemine.submod.movement.MoveWithGaze2;
import com.specialeffect.eyemine.utils.MouseHelper;
import com.specialeffect.eyemine.utils.MouseHelper.PlayerMovement;
import com.specialeffect.utils.ModUtils;
import me.shedaniel.architectury.event.events.GuiEvent;
import me.shedaniel.architectury.event.events.client.ClientLifecycleEvent;
import me.shedaniel.architectury.event.events.client.ClientRawInputEvent;
import me.shedaniel.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import org.lwjgl.glfw.GLFW;

import java.text.DecimalFormat;

public class MouseHandlerMod extends SubMod implements IConfigListener {
	public final String MODID = "mousehandler";

	//public static Configuration mConfig;

	public enum InteractionState {
	    EYETRACKER_NORMAL, EYETRACKER_WALK, EYETRACKER_LEGACY, 
	    MOUSE_NOTHING, MOUSE_LOOK, MOUSE_WALK, MOUSE_LEGACY 
	}
	
	public static InteractionState mInteractionState;
	private static InputSource mInputSource = InputSource.EyeTracker;

	private static KeyMapping mSensitivityUpKB;
	private static KeyMapping mSensitivityDownKB;
	private static KeyMapping mToggleMouseViewControlKB;

	private static IconOverlay mIconEye;
	
	private static int mTicksSinceMouseEvent = 1000;
	
	private boolean hasPendingConfigChange = false;

	public void onInitializeClient() {
		// Set up icon rendering		
		mIconEye = new IconOverlay(Minecraft.getInstance(), "eyemine:textures/icons/eye.png");
		mIconEye.setPosition(0.5f,  0.5f, 0.1f, 1.9f);
		mIconEye.setAlpha(EyeMineConfig.getFullscreenOverlayAlpha());
		mIconEye.setVisible(false);
		MainClientHandler.addOverlayToRender(mIconEye);

		// Register key bindings
		Keybindings.keybindings.add(mSensitivityUpKB = new KeyMapping(
				"key.eyemine.sensitivity_up",
				Type.KEYSYM,
				GLFW.GLFW_KEY_RIGHT,
				"category.eyemine.category.eyegaze_settings" // The translation key of the keybinding's category.
		));

		Keybindings.keybindings.add(mSensitivityDownKB = new KeyMapping(
				"key.eyemine.sensitivity_down",
				Type.KEYSYM,
				GLFW.GLFW_KEY_LEFT,
				"category.eyemine.category.eyegaze_settings" // The translation key of the keybinding's category.
		));

		// Used to turn 'look with gaze' on and off when using mouse emulation
		// instead of an
		// eyetracker
		Keybindings.keybindings.add(mToggleMouseViewControlKB = new KeyMapping(
				"key.eyemine.toggle_mouse_look",
				Type.KEYSYM,
				GLFW.GLFW_KEY_Y,
				"category.eyemine.category.eyegaze_extra" // The translation key of the keybinding's category.
		));

		ClientTickEvent.CLIENT_PRE.register(this::onClientTick);
		ClientRawInputEvent.KEY_PRESSED.register(this::onKeyInput);
		GuiEvent.SET_SCREEN.register(this::onGuiOpen);


		ClientLifecycleEvent.CLIENT_SETUP.register((state) -> {
			setupInitialState();
		});
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
			MouseHelper.setMovementState(PlayerMovement.LEGACY);
			break;
		case MOUSE_NOTHING:
			MouseHelper.setMovementState(PlayerMovement.NONE);
			break;
		case EYETRACKER_NORMAL:
		case EYETRACKER_WALK:	
		case MOUSE_LOOK:
		case MOUSE_WALK:
			MouseHelper.setMovementState(PlayerMovement.VANILLA);
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
		if (EyeMineConfig.getUsingMouseEmulation()) {
			mInputSource = InputSource.Mouse;
			updateState(InteractionState.MOUSE_NOTHING);
			MouseHelper.setUngrabbedMode(true);
		}
		else {
			mInputSource = InputSource.EyeTracker;
			updateState(InteractionState.EYETRACKER_NORMAL);
			MouseHelper.setUngrabbedMode(false);
		}		
	}
	
	@Override
	public void syncConfig() {
		// has anything *relevant* changed??
		if (EyeMineConfig.getUsingMouseEmulation() && (mInputSource == InputSource.EyeTracker || mInputSource == InputSource.Mouse)) {
			// wait until we're on GL thread before making changes...
			this.hasPendingConfigChange = true;
		}
		// this is safe to change at any point
		mIconEye.setAlpha(EyeMineConfig.getFullscreenOverlayAlpha());
	}

	private void syncConfigImpl() {
		if (this.hasPendingConfigChange) {
			// These changes need to happen on the main UI thread,   
			// since they need the right GL context	
			setupInitialState();
			this.hasPendingConfigChange = false;
		}
	}

	public void onClientTick(Minecraft minecraft) {
		LocalPlayer player = minecraft.player;
    	if (player != null) {
			if (MouseHelper.hasPendingEvent()) {
				mTicksSinceMouseEvent = 0;
				MouseHelper.consumePendingEvent();
			}
			else {
				mTicksSinceMouseEvent++;
			}
		}

    	if (!(minecraft.screen instanceof OptionsScreen)) {
    		syncConfigImpl();
		}
	}

	private InteractionResult onKeyInput(Minecraft minecraft, int keyCode, int scanCode, int action, int modifiers) {
		if (ModUtils.hasActiveGui()) { return InteractionResult.PASS; }

		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 292)) { return InteractionResult.PASS; }
		
		if (mSensitivityUpKB.matches(keyCode, scanCode) && mSensitivityUpKB.consumeClick()) {
			increaseSens();
			ModUtils.sendPlayerMessage("Sensitivity: " + toPercent(2.0f* minecraft.options.sensitivity));
		} else if (mSensitivityDownKB.matches(keyCode, scanCode) && mSensitivityDownKB.consumeClick()) {
			decreaseSens();
			ModUtils.sendPlayerMessage("Sensitivity: " + toPercent(2.0f* minecraft.options.sensitivity));
		} else if (mToggleMouseViewControlKB.matches(keyCode, scanCode) && mToggleMouseViewControlKB.consumeClick()) {
			if (mInputSource == InputSource.EyeTracker) {
				LOGGER.debug("this key doesn't do anything in eyetracker mode");
				ModUtils.sendPlayerMessage("Warning: Minecraft expects eye tracker input, not mouse");
				ModUtils.sendPlayerMessage("Perhaps you need to switch on 'Mouse emulation' mode in Mod Config -> EyeGaze -> Basic options ?");
			} else {
				if (mInteractionState == InteractionState.MOUSE_NOTHING) {
					updateState(InteractionState.MOUSE_LOOK);
				}
				else {
					updateState(InteractionState.MOUSE_NOTHING);
				}				
			}
		}
		return InteractionResult.PASS;
	}
	
	private float getSensitivityIncrement(float reference) {
		// Get a roughly-proportional increment
		// bearing in mind offset means we don't just take a linear scale
		float sens = (float) Minecraft.getInstance().options.sensitivity ;
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
		float sens = (float) Minecraft.getInstance().options.sensitivity;
		sens -= getSensitivityIncrement(sens);
		sens = Math.max(sens, MIN_SENS+0.05f);
		Minecraft.getInstance().options.sensitivity = sens;
	}

	private void increaseSens() {
		float sens = (float) Minecraft.getInstance().options.sensitivity;
		sens += getSensitivityIncrement(sens);
		sens = Math.min(sens, 1.0f);
		Minecraft.getInstance().options.sensitivity = sens;
	}
	
	private void setEmptyCursor() {
		GLFW.glfwSetInputMode(Minecraft.getInstance().getWindow().getWindow(),
								GLFW.GLFW_CURSOR, 
								GLFW.GLFW_CURSOR_HIDDEN);
	}
	
	private void setNativeCursor() {
		GLFW.glfwSetInputMode(Minecraft.getInstance().getWindow().getWindow(),
								GLFW.GLFW_CURSOR, 
								GLFW.GLFW_CURSOR_NORMAL);
	}	

	public void setMouseNotGrabbed() {
		MouseHelper.setUngrabbedMode(true);
		this.setEmptyCursor();
	}

	public static boolean hasPendingEvent() {
		return mTicksSinceMouseEvent < 2;
	}

	private InteractionResultHolder<Screen> onGuiOpen(Screen screen) {
		// For any open event, make sure cursor not overridden
		if (screen != null) {
			this.setNativeCursor();
		} else {
			// For any close event, make sure we're in the right 'grabbed' state.
			// (also sets cursor if applicable)
			if (mInputSource == InputSource.Mouse) {
				this.setEmptyCursor();
			}
		}
		return InteractionResultHolder.success(screen);
	}

	// This is the constant offset applied in MC source, corresponding
	// to "mouse does not move"
	private static float MIN_SENS = -1F / 3F;

	private String toPercent(double d) {
		DecimalFormat myFormatter = new DecimalFormat("#0.0");
		return myFormatter.format(d * 100) + "%";
	}
}

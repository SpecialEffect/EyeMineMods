/**
 * Copyright (C) 2016 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
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
import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
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
	
	//FIXME private Cursor mEmptyCursor;
	private static IconOverlay mIconEye;
	
	private static int mTicksSinceMouseEvent = 1000;

	public void setup(final FMLCommonSetupEvent event) {
		
		// Set up icon rendering		
		mIconEye = new IconOverlay(Minecraft.getInstance(), "specialeffect:icons/eye.png");
		mIconEye.setPosition(0.5f,  0.5f, 0.175f, 1.9f);
		mIconEye.setAlpha(0.2f);
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

		// Set up an empty cursor to use when doing own mouse handling
		/* FIXME int w = Cursor.getMinCursorSize(); // See https://www.glfw.org/docs/latest/input_guide.html#cursor_objects
		IntBuffer buf = BufferUtils.createIntBuffer(4 * w * w);
		BufferUtils.zeroBuffer(buf);
		try {
			mEmptyCursor = new Cursor(w, w, 0, 0, 1, buf, null);
		} catch (LWJGLException e) {
			LOGGER.debug("LWJGLException creating cursor");
		}*/
		
		// post-init
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
		setupInitialState(); // FIXME: we don't have postInit any more... 
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
		boolean disabled = (state == InteractionState.MOUSE_NOTHING ||
							state == InteractionState.MOUSE_LEGACY ||
							state == InteractionState.EYETRACKER_LEGACY);
		if (ownMouseHelper != null) {
			ownMouseHelper.setDoVanillaMovements(!disabled);
		}
	}
	
	private static void updateState(InteractionState state) {
		mInteractionState = state;
		updateIconForState(state);
		updateMouseForState(state);

		//FIXME
		/*
		if (state == InteractionState.MOUSE_WALK || state == InteractionState.EYETRACKER_WALK) {
			MoveWithGaze2.stop();
		}
		else if (state == InteractionState.MOUSE_LEGACY || state == InteractionState.EYETRACKER_LEGACY) {
			MoveWithGaze.stop();			
		}
		else {
			MoveWithGaze.stop();
			MoveWithGaze2.stop();
		}*/
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
		}
	}
		
	public void syncConfig() {
		LOGGER.debug("syncConfig MouseHandler");
		LOGGER.debug("usingMouseEmulation: " + 
				EyeMineConfig.usingMouseEmulation.get());
		
		if (EyeMineConfig.usingMouseEmulation.get()) {
			ownMouseHelper.setUngrabbedMode(true);

			if (mInputSource != InputSource.Mouse) {
				LOGGER.debug("using mouse");
				mInputSource = InputSource.Mouse;
				MouseHandler.updateState(InteractionState.MOUSE_NOTHING); 
			}
			else {
				LOGGER.debug("nothing to change");
			}
		} else {
			ownMouseHelper.setUngrabbedMode(false);

			if (mInputSource != InputSource.EyeTracker) {
				LOGGER.debug("using eyetracker");
				mInputSource = InputSource.EyeTracker;
				MouseHandler.updateState(InteractionState.EYETRACKER_NORMAL); 
			} 
			else {
				LOGGER.debug("nothing to change");
			}
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
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST) // important we get this
														// *before* other mods
	public void onKeyInput(KeyInputEvent event) {
		if (ModUtils.hasActiveGui()) { return; }

		// FIXME: test that we do get this event soonest - are all mods on same thread?
		
		//FIXME also: shall we rejig user-reported sensitivity so it doesn't go negative?
		if (mSensitivityUpKB.isPressed()) {
			increaseSens();
			ModUtils.sendPlayerMessage("Sensitivity: " + toPercent(2.0f*Minecraft.getInstance().gameSettings.mouseSensitivity));
		} else if (mSensitivityDownKB.isPressed()) {
			decreaseSens();
			ModUtils.sendPlayerMessage("Sensitivity: " + toPercent(2.0f*Minecraft.getInstance().gameSettings.mouseSensitivity));						    	
		} else if (mToggleMouseViewControlKB.isPressed()) {
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

	public void setMouseNotGrabbed() {				
		ownMouseHelper.setUngrabbedMode(true);
		
		try {
			LOGGER.debug("setting empty cursor");
			//FIXME Mouse.setNativeCursor(mEmptyCursor);
		} catch (Exception e) {
			System.out.print("exception setting cursor");
		}
		
		/*FIXME
		catch (LWJGLException e) {		
			System.out.print("LWJGLException setting native cursor");
		} catch (NullPointerException e) {
			System.out.print("Cursor is null, so can't set");
		}*/
	}

	public static boolean hasPendingEvent() {
		return mTicksSinceMouseEvent < 2;
	}
	
	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event) {
		// For any  open event, make sure cursor not overridden
		if (null != event.getGui()) {
			/* FIXME try {				
				Mouse.setNativeCursor(null);
			} catch (LWJGLException e) {
			}*/
		}
		else {
			// For any close event, make sure we're in the right 'grabbed' state.
			// (also sets cursor if applicable)
			if (mInputSource == InputSource.Mouse) {
			// FIXME	this.setMouseNotGrabbed(); // doesn't seem necessary/appropriate now
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

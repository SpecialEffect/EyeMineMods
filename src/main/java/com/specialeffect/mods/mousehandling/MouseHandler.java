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

import java.nio.IntBuffer;
import java.text.DecimalFormat;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.gui.IconOverlay;
import com.specialeffect.mods.EyeGaze;
import com.specialeffect.mods.moving.MoveWithGaze;
import com.specialeffect.mods.moving.MoveWithGaze2;
import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MouseHelper;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

@Mod(modid = MouseHandler.MODID, version = ModUtils.VERSION, name = MouseHandler.NAME)
public class MouseHandler extends BaseClassWithCallbacks implements ChildModWithConfig {
	public static final String MODID = "specialeffect.mousehandler";
	public static final String NAME = "MouseHandler";

	public static Configuration mConfig;

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
	
	private Cursor mEmptyCursor;
	private static IconOverlay mIconEye;
	
	private static int mTicksSinceMouseEvent = 1000;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);

		ModUtils.setupModInfo(event, this.MODID, this.NAME, "Mouse utilities for auto-walk, mouse emulation, etc.");
		ModUtils.setAsParent(event, EyeGaze.MODID);

		// Set up icon rendering		
		mIconEye = new IconOverlay(Minecraft.getMinecraft(), "specialeffect:icons/eye.png");
		mIconEye.setPosition(0.5f,  0.5f, 0.175f, 1.9f);
		mIconEye.setAlpha(0.2f);
		mIconEye.setVisible(false);								
		
		// Set up config
		mConfig = new Configuration(event.getSuggestedConfigurationFile());
		this.syncConfig();			
	
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		MinecraftForge.EVENT_BUS.register(mIconEye);
		
		// Set up mouse helper to handle view control
		ownMouseHelper = new MouseHelperOwn();
		Minecraft.getMinecraft().mouseHelper = (MouseHelper)ownMouseHelper;

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
		if (EyeGaze.usingMouseEmulation) {
			mInputSource = InputSource.Mouse;
			this.updateState(InteractionState.MOUSE_NOTHING); 			
		}
		else {
			mInputSource = InputSource.EyeTracker;
			this.updateState(InteractionState.EYETRACKER_NORMAL); 
		}
	}
		
	public void syncConfig() {
		System.out.println("syncConfig MouseHandler");
		System.out.println("usingMouseEmulation: " + 
				EyeGaze.usingMouseEmulation);
		
		if (EyeGaze.usingMouseEmulation) {
			if (mInputSource != InputSource.Mouse) {
				System.out.println("using mouse");
				mInputSource = InputSource.Mouse;
				this.updateState(InteractionState.MOUSE_NOTHING); 
			}
			else {
				System.out.println("nothing to change");
			}
		} else {
			if (mInputSource != InputSource.EyeTracker) {
				System.out.println("using eyetracker");
				mInputSource = InputSource.EyeTracker;
				this.updateState(InteractionState.EYETRACKER_NORMAL); 
			} 
			else {
				System.out.println("nothing to change");
			}
		}
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {

		// Subscribe to config changes from parent
		EyeGaze.registerForConfigUpdates((ChildModWithConfig) this);

		// Register key bindings
		mSensitivityUpKB = new KeyBinding("Turn mouse sensitivity up", Keyboard.KEY_RIGHT, CommonStrings.EYEGAZE_SETTINGS);
		ClientRegistry.registerKeyBinding(mSensitivityUpKB);

		mSensitivityDownKB = new KeyBinding("Turn mouse sensitivity down", Keyboard.KEY_LEFT, CommonStrings.EYEGAZE_SETTINGS);
		ClientRegistry.registerKeyBinding(mSensitivityDownKB);

		// Used to turn 'look with gaze' on and off when using mouse emulation
		// instead of an
		// eyetracker
		mToggleMouseViewControlKB = new KeyBinding("Toggle look with gaze (using mouse)", Keyboard.KEY_Y, CommonStrings.EYEGAZE_EXTRA);
		ClientRegistry.registerKeyBinding(mToggleMouseViewControlKB);

		// Set up an empty cursor to use when doing own mouse handling
		int w = Cursor.getMinCursorSize();
		IntBuffer buf = BufferUtils.createIntBuffer(4 * w * w);
		BufferUtils.zeroBuffer(buf);
		try {
			mEmptyCursor = new Cursor(w, w, 0, 0, 1, buf, null);
		} catch (LWJGLException e) {
			System.out.println("LWJGLException creating cursor");
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST) // important we get this
														// *after* other mods
	public void onLiving(LivingUpdateEvent event) {
		if (ModUtils.entityIsMe(event.getEntityLiving())) {
			EntityPlayer player = (EntityPlayer) event.getEntityLiving();

			if (ownMouseHelper.hasPendingEvent()) {
				mTicksSinceMouseEvent = 0;
				ownMouseHelper.consumePendingEvent();
			}
			else {
				mTicksSinceMouseEvent++;
			}
			
			this.processQueuedCallbacks(event);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST) // important we get this
														// *before* other mods
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		if (mSensitivityUpKB.isPressed()) {
			this.turnOnVanillaMouse();
			increaseSens();
			this.queueChatMessage("Sensitivity: " + toPercent(2.0f*Minecraft.getMinecraft().gameSettings.mouseSensitivity));
		} else if (mSensitivityDownKB.isPressed()) {
			this.turnOnVanillaMouse();
			decreaseSens();
			this.queueChatMessage("Sensitivity: " + toPercent(2.0f*Minecraft.getMinecraft().gameSettings.mouseSensitivity));
		} else if (mToggleMouseViewControlKB.isPressed()) {
			if (mInputSource == InputSource.EyeTracker) {
				System.out.println("this key doesn't do anything in eyetracker mode");
				this.queueChatMessage("Warning: Minecraft expects eye tracker input, not mouse");
				this.queueChatMessage("Perhaps you need to switch on 'Mouse emulation' mode in Mod Config -> EyeGaze -> Basic options ?");
			} else {
				if (this.mInteractionState == InteractionState.MOUSE_NOTHING) {
					this.updateState(InteractionState.MOUSE_LOOK);
				}
				else {
					this.updateState(InteractionState.MOUSE_NOTHING);
				}				
			}
		}
	}
	
	private float getSensitivityIncrement(float reference) {
		// Get a roughly-proportional increment
		// bearing in mind offset means we don't just take a linear scale
		float sens = Minecraft.getMinecraft().gameSettings.mouseSensitivity ;
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
		float sens = Minecraft.getMinecraft().gameSettings.mouseSensitivity ;
		sens -= getSensitivityIncrement(sens);
		sens = Math.max(sens, MIN_SENS+0.05f);
		Minecraft.getMinecraft().gameSettings.mouseSensitivity = sens;
	}

	private void increaseSens() {
		float sens = Minecraft.getMinecraft().gameSettings.mouseSensitivity ;		
		sens += getSensitivityIncrement(sens);
		sens = Math.min(sens, 1.0f);
		Minecraft.getMinecraft().gameSettings.mouseSensitivity = sens;
	}		

	public void setMouseNotGrabbed() {		
		ownMouseHelper.ungrabMouseCursor();
		try {
			System.out.println("setting empty cursor");
			Mouse.setNativeCursor(mEmptyCursor);
		} catch (LWJGLException e) {
			System.out.print("LWJGLException setting native cursor");
		} catch (NullPointerException e) {
			System.out.print("Cursor is null, so can't set");
		}
	}

	public static boolean hasPendingEvent() {
		return mTicksSinceMouseEvent < 2;
	}
	
	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event) {
		// For any  open event, make sure cursor not overridden
		if (null != event.getGui()) {
			try {
				Mouse.setNativeCursor(null);
			} catch (LWJGLException e) {
			}
		}
		else {
			// For any close event, make sure we're in the right 'grabbed' state.
			// (also sets cursor if applicable)
			if (mInputSource == InputSource.Mouse) {
				this.setMouseNotGrabbed();
			}
		}
	}
	
	private void turnOnVanillaMouse() {		
//		mouseHelper.
	}

	// This is the constant offset applied in MC source, corresponding
	// to "mouse does not move"
	private static float MIN_SENS = -1F / 3F;
		
	private static void turnOffVanillaMouse() {
//		Minecraft.getMinecraft().mouseHelper = emptyMouseHelper;
	}

	String toPercent(float input) {
		DecimalFormat myFormatter = new DecimalFormat("#0.0");
		return myFormatter.format(input * 100) + "%";
	}
}

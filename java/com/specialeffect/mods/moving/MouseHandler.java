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

import java.nio.IntBuffer;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.callbacks.IOnLiving;
import com.specialeffect.callbacks.OnLivingCallback;
import com.specialeffect.callbacks.SingleShotOnLivingCallback;
import com.specialeffect.messages.MovePlayerMessage;
import com.specialeffect.mods.utils.SpecialEffectUtils;
import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.utils.ModUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLadder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

@Mod(modid = MouseHandler.MODID, version = ModUtils.VERSION, name = MouseHandler.NAME)
public class MouseHandler extends BaseClassWithCallbacks implements ChildModWithConfig {
	public static final String MODID = "specialeffect.MouseHandler";
	public static final String NAME = "MouseHandler";

	public static Configuration mConfig;

	private static KeyBinding mSensivityUpKB;
	private static KeyBinding mSensivityDownKB;
	private static KeyBinding mToggleMouseViewControlKB;

	private static boolean mPendingMouseEvent = false;
	public static boolean mLastEventWithinBounds = false;
	public static boolean mDoOwnViewControl = false;

	public static float mUserMouseSensitivity = -1.0f; // internal cache of
														// user's preference.
	private static int mIgnoreEventCount = 0;
	private static float mDeadBorder = 0.1f;

	private static InputSource mInputSource = InputSource.EyeTracker;
	private static boolean mMouseMovementDisabled = false;

	private Cursor mEmptyCursor;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(this);

		ModUtils.setupModInfo(event, this.MODID, this.NAME, "Mouse utilities for auto-walk, mouse emulation, etc.");
		ModUtils.setAsParent(event, SpecialEffectMovements.MODID);

		// Set up config
		mConfig = new Configuration(event.getSuggestedConfigurationFile());
		this.syncConfig();

		// Check the initial sensitivity setting.
		this.querySensitivity();

	}

	public void syncConfig() {
		System.out.println("syncConfig MouseHandler");
		mDeadBorder = SpecialEffectMovements.mDeadBorder;
		if (SpecialEffectMovements.usingMouseEmulation) {
			System.out.println("using mouse");
			mInputSource = InputSource.Mouse;
			// initially disabled
			mMouseMovementDisabled = true;
		} else {
			System.out.println("using eyetracker");
			mInputSource = InputSource.EyeTracker;
			// always enabled
			mMouseMovementDisabled = false;
		}
	}

	public static void setMouseMovementsDisabled(boolean doDisable) {
		if (mInputSource == InputSource.Mouse) {
			mMouseMovementDisabled = doDisable;
		}
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// Subscribe to event buses
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);

		// Subscribe to config changes from parent
		SpecialEffectMovements.registerForConfigUpdates((ChildModWithConfig) this);

		// Register key bindings
		mSensivityUpKB = new KeyBinding("Turn mouse sensitivity UP", Keyboard.KEY_RIGHT, "SpecialEffect");
		ClientRegistry.registerKeyBinding(mSensivityUpKB);

		mSensivityDownKB = new KeyBinding("Turn mouse sensitivity DOWN", Keyboard.KEY_LEFT, "SpecialEffect");
		ClientRegistry.registerKeyBinding(mSensivityDownKB);

		// Used to turn 'look with gaze' on and off when using mouse emulation
		// instead of an
		// eyetracker
		mToggleMouseViewControlKB = new KeyBinding("Toggle look with gaze", Keyboard.KEY_Y, "SpecialEffect");
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
		if (event.entityLiving instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.entityLiving;
			mPendingMouseEvent = false;

			this.processQueuedCallbacks(event);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST) // important we get this
														// *before* other mods
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		if (mSensivityUpKB.isPressed()) {
			this.resetSensitivity();
			Minecraft.getMinecraft().gameSettings.mouseSensitivity *= 1.1;
			this.querySensitivity();
			this.queueChatMessage("Sensitivity: " + toPercent(2.0f*Minecraft.getMinecraft().gameSettings.mouseSensitivity));
		} else if (mSensivityDownKB.isPressed()) {
			this.resetSensitivity();
			Minecraft.getMinecraft().gameSettings.mouseSensitivity /= 1.1;
			this.querySensitivity();
			this.queueChatMessage("Sensitivity: " + toPercent(2.0f*Minecraft.getMinecraft().gameSettings.mouseSensitivity));
		} else if (mToggleMouseViewControlKB.isPressed()) {
			if (mInputSource == InputSource.EyeTracker) {
				System.out.println("this key doesn't do anything in eyetracker mode");
			} else {
				// Only one mousehandling mode should be on at a time
				MoveWithGaze2.stop();
				// This is a bit of a proxy, it might have been changed by
				// something else
				// (but currently only WalkWithGaze2, which we just turned off!)
				mMouseMovementDisabled = !mMouseMovementDisabled;
				if (mMouseMovementDisabled) {
					MoveWithGaze.stop();
				}
			}
		}
	}

	public void setMouseNotGrabbed() {
		Mouse.setGrabbed(false);
		try {
			System.out.println("setting empty cursor");
			Mouse.setNativeCursor(mEmptyCursor);
		} catch (LWJGLException e) {
			System.out.print("LWJGLException setting native cursor");
		} catch (NullPointerException e) {
			System.out.print("Cursor is null, so can't set");
		}
	}

	public static void setIgnoreNextEvent() {
		mIgnoreEventCount++;
	}

	public static boolean hasPendingEvent() {
		return mPendingMouseEvent;
	}

	public boolean lastEventWithinBounds() {
		return mPendingMouseEvent && mLastEventWithinBounds;
	}

	private void onMouseInputGrabbed(InputEvent.MouseInputEvent event) {
		// Cancel any mouse events within a certain border. This avoids mouse
		// movements outside the window (e.g. from
		// eye gaze system) from having an impact on view direction.
		float x_abs = Math.abs((float) Mouse.getEventDX()); // distance from
															// centre
		float y_abs = Math.abs((float) Mouse.getEventDY());

		if (mIgnoreEventCount > 0 || !isPointInBounds(x_abs, y_abs)) {
			// In v1.8, it would be sufficient to query getDX and DY to consume
			// the deltas.
			// ... but this doesn't work in 1.8.8, so we hack it by setting the
			// mouse sensitivity down low.
			// See:
			// http://www.minecraftforge.net/forum/index.php?topic=29216.10;wap2
			this.zeroSensitivity();
		}
		// turn off anyway, if vanilla mouse movements turned off, but record
		// pending event.
		else if (mMouseMovementDisabled) {
			this.zeroSensitivity();
			mPendingMouseEvent = true;
		} else {
			this.resetSensitivity();
			mPendingMouseEvent = true;
		}

		mIgnoreEventCount = Math.max(mIgnoreEventCount - 1, 0);
	}

	// x_abs and y_abs are from centre of minecraft window
	private boolean isPointInBounds(float x_abs, float y_abs) {
		float r = 2 * mDeadBorder;

		float w_half = (float) Minecraft.getMinecraft().displayWidth / 2;
		float h_half = (float) Minecraft.getMinecraft().displayHeight / 2;

		if (x_abs > w_half * (1 - r) || y_abs > h_half * (1 - r)) {
			return false;
		} else {
			return true;
		}
	}

	private void onMouseInputNotGrabbed(InputEvent.MouseInputEvent event) {
		// Don't allow vanilla processing
		this.zeroSensitivity();

		if (!mMouseMovementDisabled) {
			float x = Math.abs((float) Mouse.getEventX());
			float y = Math.abs((float) Mouse.getEventY());

			float w_half = (float) Minecraft.getMinecraft().displayWidth / 2;
			float h_half = (float) Minecraft.getMinecraft().displayHeight / 2;

			float dx = x - w_half;
			float dy = y - h_half;

			if (isPointInBounds(dx, dy)) {
				float s = mUserMouseSensitivity;

				// handle yaw
				final float mMaxYaw = 35/2; // at 100% sensitivity
				final float dYaw = mMaxYaw * s * dx / w_half;

				// handle pitch
				final float mMaxPitch = -40/2; // at 100% sensitivity
				final float dPitch = mMaxPitch * s * dy / w_half;

				this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving() {
					@Override
					public void onLiving(LivingUpdateEvent event) {
						EntityPlayer player = (EntityPlayer) event.entity;
						player.rotationPitch += dPitch;
						player.rotationYaw += dYaw;
					}
				}));

				mPendingMouseEvent = true;
			}
		} else {
			mPendingMouseEvent = true;
		}
	}

	@SubscribeEvent
	public void onMouseInput(InputEvent.MouseInputEvent event) {

		if (Mouse.getEventButton() > -1) {
			// Don't hijack button events, let minecraft do it's own thing
			return;
		}

		if (mInputSource == InputSource.EyeTracker) {
			this.onMouseInputGrabbed(event);
		} else if (mInputSource == InputSource.Mouse) {
			this.onMouseInputNotGrabbed(event);
		}
	}
	
	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event) {
		// It's important that when the 'controls' menu is opened, we are
		// not overriding the sensitivity setting. It's also important that
		// we get any user updates to sensitivity.
		
		// This corresponds to the opening of the controls pane 
		if (null != event.gui && event.gui.getClass() == GuiControls.class) {
			this.resetSensitivity();			
		}
		// This corresponds to the opening/closing of *any other pane*.
		// NB: Important to know that new screens are opened before the controls
		// pane is closed; and close events don't have guiscreens attached to them.
		else {
			this.querySensitivity();			
		}
		
		// For any  open event, make sure cursor not overridden
		if (null != event.gui) {
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

	@EventHandler
	public void serverStopping(FMLServerStoppingEvent event) {
		this.resetSensitivity();
	}

	private void resetSensitivity() {
		if (mUserMouseSensitivity > 0) {
			Minecraft.getMinecraft().gameSettings.mouseSensitivity = mUserMouseSensitivity;
		}
	}

	private void zeroSensitivity() {
		// See http://www.minecraftforge.net/forum/index.php?topic=29216.10;wap2
		// for
		// magic number.
		Minecraft.getMinecraft().gameSettings.mouseSensitivity = -1F / 3F;
	}

	private void querySensitivity() {
		float sens = Minecraft.getMinecraft().gameSettings.mouseSensitivity;
		if (sens > 0) {
			mUserMouseSensitivity = sens;
		}
	}

	String toPercent(float input) {
		DecimalFormat myFormatter = new DecimalFormat("#0.0");
		return myFormatter.format(input * 100) + "%";
	}
}

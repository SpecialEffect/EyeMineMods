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

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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

	private static boolean mPendingMouseEvent = false;
	public static boolean mLastEventWithinBounds = false;
	public static boolean mDoOwnViewControl = false;

	public static float mUserMouseSensitivity = -1.0f; // internal cache of
														// user's preference.
	private static int mIgnoreEventCount = 0;
	private static float mDeadBorder = 0.1f;

	private static InputSource mInputSource = InputSource.EyeTracker;
	private static boolean mMouseMovementDisabled = false;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(this);

		ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Add key binding to start/stop walking continuously, with direction controlled by mouse/eyetracker");
		ModUtils.setAsParent(event, SpecialEffectMovements.MODID);

		// Set up config
		mConfig = new Configuration(event.getSuggestedConfigurationFile());
		this.syncConfig();

		// Check the initial sensitivity setting.
		this.querySensitivity();

		// We start without 'look with gaze' in mouse-emulation mode.
		if (mInputSource == InputSource.Mouse) {
			mMouseMovementDisabled = true;
		}
	}

	public void syncConfig() {
		System.out.println("syncConfig MouseHandler");
		mDeadBorder = SpecialEffectMovements.mDeadBorder;
		if (SpecialEffectMovements.usingMouseEmulation) {
			System.out.println("using mouse");
			mInputSource = InputSource.Mouse;
		} else {
			System.out.println("using eyetracker");
			mInputSource = InputSource.EyeTracker;
		}
	}

	public static void setMouseMovementsDisabled(boolean doDisable) {
		// if (mInputSource == InputSource.EyeTracker) {
		// // TODO: user-friendly message.
		// this.queueChatMessage("In eye tracker mode, ignoring request");
		// }
		mMouseMovementDisabled = doDisable;
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// Subscribe to event buses
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);

		// Subscribe to config changes from parent
		SpecialEffectMovements.registerForConfigUpdates((ChildModWithConfig) this);

		// Register key bindings
		mSensivityUpKB = new KeyBinding("Turn mouse sensitivity UP", Keyboard.KEY_ADD, "SpecialEffect");
		ClientRegistry.registerKeyBinding(mSensivityUpKB);

		mSensivityDownKB = new KeyBinding("Turn mouse sensitivity DOWN", Keyboard.KEY_SUBTRACT, "SpecialEffect");
		ClientRegistry.registerKeyBinding(mSensivityDownKB);

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
			this.queueChatMessage("Sensitivity: " + toPercent(Minecraft.getMinecraft().gameSettings.mouseSensitivity));
		} else if (mSensivityDownKB.isPressed()) {
			this.resetSensitivity();
			Minecraft.getMinecraft().gameSettings.mouseSensitivity /= 1.1;
			this.querySensitivity();
			this.queueChatMessage("Sensitivity: " + toPercent(Minecraft.getMinecraft().gameSettings.mouseSensitivity));
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

		// if (mDoOwnViewControl) {
		// Don't allow vanilla processing
		this.zeroSensitivity();
		System.out.println("disabled? " + mMouseMovementDisabled );
		if (!mMouseMovementDisabled) {

			// TODO: Cancel edge events
			float x = Math.abs((float) Mouse.getEventX());
			float y = Math.abs((float) Mouse.getEventY());

			System.out.println("x, y: " + x + ", " + y);

			float w_half = (float) Minecraft.getMinecraft().displayWidth / 2;
			float h_half = (float) Minecraft.getMinecraft().displayHeight / 2;

			float dx = x - w_half;
			float dy = y - h_half;

			if (isPointInBounds(dx, dy)) {
				System.out.println("Mouse (" + dx + ", " + dy + ")");

				float s = mUserMouseSensitivity;

				// handle yaw
				final float mMaxYaw = 50; // at 100% sensitivity
				final float dYaw = mMaxYaw * s * dx / w_half;

				// handle pitch
				final float mMaxPitch = -50; // at 100% sensitivity
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
			System.out.println("disabled, not doing anything.");
		}
		// }
	}

	@SubscribeEvent
	public void onMouseInput(InputEvent.MouseInputEvent event) {

		if (Mouse.getEventButton() > -1) {
			// Don't hijack button events, let minecraft do it's own thing
			return;
		}
		
		if (mInputSource == InputSource.EyeTracker) {
			System.out.println("onMouseInput grabbed");
			this.onMouseInputGrabbed(event);
		} else if (mInputSource == InputSource.Mouse) {
			//Mouse.setGrabbed(false);
			this.onMouseInputNotGrabbed(event);
			System.out.println("onMouseInput not-grabbed");
		}
	}

	// When we leave a GUI and enter the game, we record the user's
	// chosen sensitivity (and hack around with it in-game).
	// When we leave the game, we reset the sensitivity to how we found it.
	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event) {
		// This is an 'open' and 'close' event

		System.out.println("onGuiOpen " + event.gui == null);
		if (event.gui != null) { // open event
			this.resetSensitivity();
		} else {
			this.querySensitivity();
			// Make sure we're in the right 'grabbed' state.
			Mouse.setGrabbed(mInputSource == InputSource.EyeTracker);
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
		mUserMouseSensitivity = Minecraft.getMinecraft().gameSettings.mouseSensitivity;
	}

	String toPercent(float input) {
		DecimalFormat myFormatter = new DecimalFormat("#0.0");
		return myFormatter.format(input * 100) + "%";
	}
}

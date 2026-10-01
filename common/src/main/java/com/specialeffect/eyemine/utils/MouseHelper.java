/**
 * Copyright (C) 2016-2020 Kirsty McNaught
 * <p>
 * Developed for SpecialEffect, www.specialeffect.org.uk
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.eyemine.utils;

import com.mojang.blaze3d.platform.InputConstants;
import com.specialeffect.eyemine.mixin.MouseHandlerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.lwjgl.glfw.GLFW;

public class MouseHelper {
	private static MouseHelper INSTANCE;

	public static MouseHelper instance() {
		if (INSTANCE == null)
			INSTANCE = new MouseHelper();
		return INSTANCE;
	}

	// special case lets eye-gaze-cursor control minecraft but also escape
	// to access EyeMine keyboard
	public static boolean ungrabbedMouseMode = false;

	public static boolean mHasPendingEvent = false;

	public static double lastXVelocity = 0.0;
	public static double lastYVelocity = 0.0;

	// Track if gaze is below the hotbar area (where EyeMine keyboard renders)
	// Walking should pause when this is true
	public static boolean isGazeBelowHotbar = false;

	// Track if gaze is outside the window (in the dead border area)
	// Walking should pause when this is true - matches original behavior
	public static boolean isGazeOutsideWindow = false;

	public static synchronized void addPendingEvent() {
		mHasPendingEvent = true;
	}

	public static synchronized void consumePendingEvent() {
		mHasPendingEvent = false;
	}

	public static synchronized boolean hasPendingEvent() {
		return mHasPendingEvent;
	}

	public static boolean canChangeMouseCapture() {
		Minecraft minecraft = Minecraft.getInstance();
		// Cursor operations belong on the client thread; overlays also suppress capture changes.
		return minecraft.isSameThread() && minecraft.gui.overlay() == null;
	}

	public static void setUngrabbedMode(boolean ungrabbed) {
		ungrabbedMouseMode = ungrabbed;
		if (!canChangeMouseCapture()) {
			return;
		}

		MouseHandler mouseHandler = Minecraft.getInstance().mouseHandler;
		if (mouseHandler != null) {
			if (ungrabbed) {
				mouseHandler.releaseMouse();
			} else {
				mouseHandler.grabMouse();
			}
		}
	}

	public static PlayerMovement movementState = PlayerMovement.VANILLA;

	public enum PlayerMovement {
		NONE, VANILLA, LEGACY
	}

	/**
	 * public entry points for automated cursor actions
	 **/
	public void moveCursor(MouseHandler mouseHelper, double xpos, double ypos) {
		if (mouseHelper != null) {
			long handle = Minecraft.getInstance().getWindow().handle();
			GLFW.glfwSetCursorPos(handle, (float) xpos, (float) ypos);
			((MouseHandlerAccessor) mouseHelper).invokeOnMove(handle, xpos, ypos);
		}
	}

	public void leftMouseClickAtPosition(MouseHandler mouseHelper, double xpos, double ypos) {
		if (mouseHelper != null) {
			this.moveCursor(mouseHelper, xpos, ypos);
			this.mouseButton(InputConstants.MOUSE_BUTTON_LEFT, InputConstants.PRESS, 0);
			this.mouseButton(InputConstants.MOUSE_BUTTON_LEFT, InputConstants.RELEASE, 0);
		}
	}

	public void leftShiftMouseClickAtPosition(MouseHandler mouseHelper, double xpos, double ypos) {
		if (mouseHelper != null) {
			this.moveCursor(mouseHelper, xpos, ypos);
			this.mouseButton(InputConstants.MOUSE_BUTTON_LEFT, InputConstants.PRESS, InputConstants.MOD_SHIFT);
			this.mouseButton(InputConstants.MOUSE_BUTTON_LEFT, InputConstants.RELEASE, InputConstants.MOD_SHIFT);
		}
	}

	public void scroll(MouseHandler mouseHandler, double amount) {
		if (mouseHandler != null) {
			long handle = Minecraft.getInstance().getWindow().handle();
			((MouseHandlerAccessor) mouseHandler).invokeOnScroll(handle, 0, amount);
		}
	}

	/**
	 *
	 **/

	/* Move cursor to location and perform mouse action
	 * button: any constant GLFW.GLFW_MOUSE_BUTTON_X
	 * action: GLFW.GLFW_PRESS or GLFW.GLFW_RELEASE
	 * mods: GLFW.GLFW_MOD_[SHIFT/CONTROL/ALT/SUPER]
	 */
	public void mouseButton(int button, int action, int mods) {
		long handle = Minecraft.getInstance().getWindow().handle();
		((MouseHandlerAccessor) Minecraft.getInstance().mouseHandler).invokeOnButton(
				handle, new net.minecraft.client.input.MouseButtonInfo(button, mods), action);
	}

	public static void setMovementState(PlayerMovement state) {
		movementState = state;
	}
}

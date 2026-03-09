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

package com.specialeffect.eyemine.mixin;

import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.InputConstants;
import com.specialeffect.eyemine.EyeMine;
import com.specialeffect.eyemine.platform.EyeMineConfig;
import com.specialeffect.eyemine.utils.MouseHelper;
import com.specialeffect.eyemine.utils.MouseHelper.PlayerMovement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.SmoothDouble;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	private double xpos;

	@Shadow
	private double ypos;

	@Shadow
	private double accumulatedDX;

	@Shadow
	private double accumulatedDY;

	@Shadow
	private boolean mouseGrabbed;

	@Shadow
	private double lastHandleMovementTime;

	@Shadow
	@Final
	private SmoothDouble smoothTurnX;

	@Shadow
	@Final
	private SmoothDouble smoothTurnY;

	@Shadow
	private boolean ignoreFirstMove;

	@Shadow
	public abstract void grabMouse();

	@Shadow
	public abstract boolean isMouseGrabbed();

	@Shadow
	protected abstract void turnPlayer(double d);

	@Unique
	public float eyemine$deadBorder = 0.05f;

	@Unique
	public float eyemine$clipBorderHorizontal = 0.3f;

	@Unique
	public float eyemine$clipBorderVertical = 0.2f;

	// Store last known cursor position for ungrabbed mode
	@Unique
	private double eyemine$lastX = 0;

	@Unique
	private double eyemine$lastY = 0;

	/**
	 * Inject at HEAD of onMove to add our pending event tracking
	 */
	@Inject(method = "onMove(JDD)V", at = @At(value = "HEAD"))
	public void eyemine$addPendingEvent(long windowPointer, double xPos, double yPos, CallbackInfo ci) {
		MouseHelper.addPendingEvent();
	}

	/**
	 * Set cursor to normal mode before vanilla processes - this allows us to read position
	 */
	@Inject(method = "onMove(JDD)V", at = @At(value = "FIELD",
			target = "Lnet/minecraft/client/MouseHandler;minecraft:Lnet/minecraft/client/Minecraft;",
			ordinal = 0))
	public void eyemine$setInputMode(long handle, double xpos, double ypos, CallbackInfo ci) {
		GLFW.glfwSetInputMode(this.minecraft.getWindow().getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
	}

	/**
	 * Main injection point - replaces vanilla onMove logic after the window handle check
	 */
	@Inject(method = "onMove(JDD)V", at = @At(value = "FIELD",
			target = "Lnet/minecraft/client/MouseHandler;ignoreFirstMove:Z",
			ordinal = 0), cancellable = true)
	public void eyemine$processOnMove(long handle, double xpos, double ypos, CallbackInfo ci) {
		// Check if we're on a screen - if so, just update position and let vanilla handle it
		if (this.minecraft.screen != null && this.minecraft.getOverlay() == null) {
			this.xpos = xpos;
			this.ypos = ypos;
			ci.cancel();
			return;
		}

		// If mouse should be grabbed but isn't - this can happen if we alt-tab
		// away while world is loading, with pauseOnLostFocus=false
		if (!MouseHelper.ungrabbedMouseMode && this.minecraft.isWindowActive() && !this.mouseGrabbed) {
			this.grabMouse();
		}

		// Store current position for ungrabbed mode
		if (MouseHelper.ungrabbedMouseMode) {
			this.eyemine$lastX = xpos;
			this.eyemine$lastY = ypos;
		}

		// Check if gaze is below the hotbar area (configurable threshold)
		// This is where the EyeMine keyboard renders, so walking should pause
		// Skip the check if position is (0,0) in grabbed mode - that's our cursor reset position,
		// not a real gaze. GLFW fires an onMove callback when we reset the cursor.
		int threshold = EyeMineConfig.getGazeIdleThreshold();
		boolean isCursorResetEvent = !MouseHelper.ungrabbedMouseMode && xpos == 0 && ypos == 0;
		if (threshold > 0 && !isCursorResetEvent) {
			double screenHeight = this.minecraft.getWindow().getScreenHeight();
			// Use raw ypos from callback - this is the actual gaze/cursor position
			// In grabbed mode, ypos is reported relative to window after we set NORMAL mode above
			double thresholdY = screenHeight * (1.0 - threshold / 100.0);
			boolean wasBelow = MouseHelper.isGazeBelowHotbar;
			MouseHelper.isGazeBelowHotbar = ypos > thresholdY;
			// Log when state changes to help debug
			if (MouseHelper.isGazeBelowHotbar != wasBelow) {
				EyeMine.LOGGER.info("Gaze threshold: ypos={}, screenHeight={}, threshold={}%, thresholdY={}, isBelow={}",
					String.format("%.1f", ypos), String.format("%.1f", screenHeight), threshold,
					String.format("%.1f", thresholdY), MouseHelper.isGazeBelowHotbar);
			}
		} else if (threshold <= 0) {
			MouseHelper.isGazeBelowHotbar = false;
		}
		// Note: if isCursorResetEvent, we leave isGazeBelowHotbar unchanged

		// Process mouse position - sets accumulatedDX/DY based on position
		if (this.minecraft.isWindowActive()) {
			this.eyemine$processMousePosition(xpos, ypos, isCursorResetEvent);
		}

		// In grabbed mode (eye tracker), reset cursor to origin after each move
		// This makes each subsequent position effectively a delta
		if (!MouseHelper.ungrabbedMouseMode) {
			GLFW.glfwSetCursorPos(this.minecraft.getWindow().getWindow(), 0, 0);
			this.xpos = 0;
			this.ypos = 0;
			// In grabbed mode, call turnPlayer immediately since position IS the delta
			this.turnPlayer(Blaze3D.getTime() - this.lastHandleMovementTime);
		} else {
			// In ungrabbed mode, update xpos/ypos for vanilla compatibility
			this.xpos = xpos;
			this.ypos = ypos;
		}

		ci.cancel();
	}

	@Unique
	private void eyemine$processMousePosition(double x, double y, boolean isCursorResetEvent) {
		double w_half = (double) this.minecraft.getWindow().getScreenWidth() / 2;
		double h_half = (double) this.minecraft.getWindow().getScreenHeight() / 2;

		// Adjust coordinates to centralized when ungrabbed
		if (MouseHelper.ungrabbedMouseMode) {
			x -= w_half;
			y -= h_half;
		}

		double x_abs = Math.abs(x);
		double y_abs = Math.abs(y);

		// Check if gaze is outside the window (in dead border)
		// Skip updating for cursor reset events - (0,0) is our reset position, not real gaze
		boolean isOutside = x_abs > w_half * (1 - this.eyemine$deadBorder) ||
				y_abs > h_half * (1 - this.eyemine$deadBorder);
		if (!isCursorResetEvent) {
			MouseHelper.isGazeOutsideWindow = isOutside;
		}

		// If mouse is outside minecraft window, or gaze is at keyboard area, stop camera movement
		if (isOutside || MouseHelper.isGazeBelowHotbar) {
			// do nothing
			this.eyemine$resetVelocity();
		} else {
			// If mouse is around edges, clip effect
			if (x_abs > w_half * (1 - this.eyemine$clipBorderHorizontal)) {
				x = (int) (Math.signum(x) * (w_half * (1 - this.eyemine$clipBorderHorizontal)));
			}
			if (y_abs > h_half * (1 - this.eyemine$clipBorderVertical)) {
				y = (int) (Math.signum(y) * (h_half * (1 - this.eyemine$clipBorderVertical)));
			}

			this.accumulatedDX = x;
			this.accumulatedDY = y;

			// Remember there was a valid event, even if we're not moving
			MouseHelper.mHasPendingEvent = true;
		}
	}

	@Unique
	private void eyemine$resetVelocity() {
		MouseHelper.lastXVelocity = this.accumulatedDX;
		MouseHelper.lastYVelocity = this.accumulatedDY;
		this.accumulatedDX = 0.0D;
		this.accumulatedDY = 0.0D;
	}

	/**
	 * Override turnPlayer to use EyeMine's custom look logic
	 */
	@Inject(method = "turnPlayer(D)V", at = @At(value = "HEAD"), cancellable = true)
	public void eyemine$turnPlayer(double movementTime, CallbackInfo ci) {
		if (this.minecraft.player == null) {
			ci.cancel();
			return;
		}

		// In ungrabbed mode (emulation), recalculate accumulated values from stored position
		// This ensures values are correct even when called from game loop (handleAccumulatedMovement)
		if (MouseHelper.ungrabbedMouseMode) {
			this.eyemine$recalculateFromStoredPosition();
		}

		if (MouseHelper.movementState == PlayerMovement.VANILLA) {
			eyemine$updatePlayerLookVanilla();
		} else if (MouseHelper.movementState == PlayerMovement.LEGACY) {
			eyemine$updatePlayerLookLegacy();
		} else {
			// NONE state - keep track of last time but don't turn
			this.lastHandleMovementTime = Blaze3D.getTime();
		}

		ci.cancel();
	}

	/**
	 * Recalculate accumulatedDX/DY from stored cursor position for ungrabbed mode
	 */
	@Unique
	private void eyemine$recalculateFromStoredPosition() {
		double w_half = (double) this.minecraft.getWindow().getScreenWidth() / 2;
		double h_half = (double) this.minecraft.getWindow().getScreenHeight() / 2;

		// Position relative to center
		double x = this.eyemine$lastX - w_half;
		double y = this.eyemine$lastY - h_half;

		double x_abs = Math.abs(x);
		double y_abs = Math.abs(y);

		// If mouse is outside dead border, don't turn
		if (x_abs > w_half * (1 - this.eyemine$deadBorder) ||
				y_abs > h_half * (1 - this.eyemine$deadBorder)) {
			this.accumulatedDX = 0;
			this.accumulatedDY = 0;
		} else {
			// Clip to border regions
			if (x_abs > w_half * (1 - this.eyemine$clipBorderHorizontal)) {
				x = Math.signum(x) * (w_half * (1 - this.eyemine$clipBorderHorizontal));
			}
			if (y_abs > h_half * (1 - this.eyemine$clipBorderVertical)) {
				y = Math.signum(y) * (h_half * (1 - this.eyemine$clipBorderVertical));
			}

			this.accumulatedDX = x;
			this.accumulatedDY = y;
		}
	}

	@Unique
	public void eyemine$updatePlayerLookVanilla() {
		double d0 = Blaze3D.getTime();
		double d1 = d0 - this.lastHandleMovementTime;
		this.lastHandleMovementTime = d0;

		if (this.minecraft.isWindowActive()) {
			double d4 = this.minecraft.options.sensitivity().get() * 0.6000000238418579D + 0.20000000298023224D;
			double d5 = d4 * d4 * d4 * 8.0D;
			double d2;
			double d3;

			if (this.minecraft.options.smoothCamera) {
				double d6 = this.smoothTurnX.getNewDeltaValue(this.accumulatedDX * d5, d1 * d5);
				double d7 = this.smoothTurnY.getNewDeltaValue(this.accumulatedDY * d5, d1 * d5);
				d2 = d6;
				d3 = d7;
			} else {
				this.smoothTurnX.reset();
				this.smoothTurnY.reset();

				// quadratic fit near centre
				double w = this.minecraft.getWindow().getGuiScaledWidth();
				double h = this.minecraft.getWindow().getGuiScaledHeight();
				double d = h / 8;

				double p = 2; // quadratic near centre
				double k = 0.5 * (d5 * w) / (1 + p * (w / (2 * d) - 1));

				// linear further out (but continuous at transition point)
				double a = k * (1 - p);
				double m = p * k / d;

				if (Math.abs(this.accumulatedDX) > d) {
					d2 = Math.signum(this.accumulatedDX) * (a + m * Math.abs(this.accumulatedDX));
				} else {
					d2 = Math.signum(this.accumulatedDX) * k * Math.pow(Math.abs(this.accumulatedDX) / d, p);
				}
				if (Math.abs(this.accumulatedDY) > d) {
					d3 = Math.signum(this.accumulatedDY) * (a + m * Math.abs(this.accumulatedDY));
				} else {
					d3 = Math.signum(this.accumulatedDY) * k * Math.pow(Math.abs(this.accumulatedDY) / d, p);
				}
			}

			this.eyemine$resetVelocity();
			int i = this.minecraft.options.invertYMouse().get() ? -1 : 1;

			this.minecraft.getTutorial().onMouse(d2, d3);
			if (this.minecraft.player != null) {
				this.minecraft.player.turn(d2, d3 * (double) i);
			}
		} else {
			this.eyemine$resetVelocity();
		}
	}

	@Unique
	public void eyemine$updatePlayerLookLegacy() {
		// Rotate the player (yaw) according to x position only
		double d0 = Blaze3D.getTime();
		double d1 = d0 - this.lastHandleMovementTime;
		this.lastHandleMovementTime = d0;

		if (this.minecraft.isWindowActive()) {
			double d4 = 0.1 * this.minecraft.options.sensitivity().get() * 0.6F + 0.2F;
			double d5 = 0.5d * d4 * d4 * d4 * 8.0D;
			double d2;

			if (this.minecraft.options.smoothCamera) {
				d2 = this.smoothTurnX.getNewDeltaValue(this.accumulatedDX * d5, d1 * d5);
			} else {
				this.smoothTurnX.reset();
				this.smoothTurnY.reset();

				// quadratic fit near centre
				double w = this.minecraft.getWindow().getGuiScaledWidth();
				double h = this.minecraft.getWindow().getGuiScaledHeight();
				double d = h / 8;
				double p = 2; // quadratic near centre
				double k = 0.5 * (d5 * w) / (1 + p * (w / (2 * d) - 1));

				// linear further out (but continuous at transition point)
				double a = k * (1 - p);
				double m = p * k / d;

				if (Math.abs(this.accumulatedDX) > d) {
					d2 = Math.signum(this.accumulatedDX) * (a + m * Math.abs(this.accumulatedDX));
				} else {
					d2 = Math.signum(this.accumulatedDX) * k * Math.pow(Math.abs(this.accumulatedDX) / d, p);
				}

				// When going backward, reduce the yaw effect
				double h6 = (double) this.minecraft.getWindow().getGuiScaledHeight() / 6;
				if (this.accumulatedDY > h6) {
					d2 *= 0.5;
				}
			}

			this.eyemine$resetVelocity();

			this.minecraft.getTutorial().onMouse(d2, 0);
			if (this.minecraft.screen == null && this.minecraft.player != null) {
				this.minecraft.player.turn(d2, 0);
			}
		} else {
			this.eyemine$resetVelocity();
		}
	}

	/**
	 * Returns true if the mouse is grabbed.
	 * We override this so mining works even in ungrabbed mode.
	 */
	@Inject(method = "isMouseGrabbed()Z", at = @At("RETURN"), cancellable = true)
	public void eyemine$isMouseGrabbed(CallbackInfoReturnable<Boolean> cir) {
		// Somewhere deep in the MC engine, this is being queried to see whether mining should
		// occur, so we have to lie a little.
		boolean flag = this.mouseGrabbed;
		boolean flag2 = (this.minecraft.isWindowActive() && MouseHelper.ungrabbedMouseMode);
		cir.setReturnValue(flag || flag2);
	}

	@Inject(method = "grabMouse()V", at = @At(value = "HEAD"), cancellable = true)
	public void eyemine$grabMouse(CallbackInfo ci) {
		EyeMine.LOGGER.debug("grabMouse");
		if (!MouseHelper.hasGLcontext()) {
			ci.cancel();
		}
	}

	@Inject(method = "grabMouse()V", at = @At(value = "INVOKE",
			target = "Lcom/mojang/blaze3d/platform/InputConstants;grabOrReleaseMouse(JIDD)V",
			ordinal = 0), cancellable = true)
	public void eyemine$onlyGrabWhenUngrabbed(CallbackInfo ci) {
		if (!MouseHelper.ungrabbedMouseMode) {
			InputConstants.grabOrReleaseMouse(this.minecraft.getWindow().getWindow(), 212995, this.xpos, this.ypos);
		}

		this.minecraft.setScreen((Screen) null);
		((MinecraftAccessor) this.minecraft).setMissTime(10000);
		this.ignoreFirstMove = true;
		ci.cancel();
	}

	/**
	 * Resets the player keystate, disables the ingame focus, and ungrabs the mouse cursor.
	 */
	@Inject(at = @At("HEAD"), method = "releaseMouse()V", cancellable = true)
	public void eyemine$releaseMouse(CallbackInfo ci) {
		if (!MouseHelper.hasGLcontext()) {
			ci.cancel();
		}
	}
}

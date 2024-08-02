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

import com.specialeffect.eyemine.utils.MouseHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.util.SmoothDouble;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
	protected abstract void turnPlayer(double d);

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

	@Shadow public abstract void setIgnoreFirstMove();

	@Unique
	public float eyemine$deadBorder = 0.05f;
	@Unique
	public float eyemine$clipBorderHorizontal = 0.3f;
	@Unique
	public float eyemine$clipBorderVertical = 0.2f;


	@Inject(method = "onMove(JDD)V", at = @At(value = "HEAD"))
	public void EyeMine$addPendingEvent(long windowPointer, double xPos, double yPos, CallbackInfo ci) {
		MouseHelper.addPendingEvent();
	}

//	@Inject(method = "onMove(JDD)V",
//			locals = LocalCapture.CAPTURE_FAILEXCEPTION, at = @At(
//			value = "INVOKE",
//			target = "Lnet/minecraft/client/Minecraft;isWindowActive()Z",
//			shift = At.Shift.BEFORE,
//			ordinal = 0))
//	public void eyemine$GrabMouseOnMove(CallbackInfo ci) {
//		// If mouse should be grabbed but isn't - this can happen if we alt-tab
//		// away while world is loading, with pauseOnLostFocus=false
//		if (this.minecraft.level != null && !MouseHelper.ungrabbedMouseMode && this.minecraft.isWindowActive() && !this.isMouseGrabbed()) {
//			this.grabMouse();
//		}
//	}
//
//	@Inject(method = "handleAccumulatedMovement()V",
//			locals = LocalCapture.CAPTURE_FAILEXCEPTION, at = @At(
//			value = "INVOKE",
//			target = "Lnet/minecraft/client/Minecraft;getWindow()Lcom/mojang/blaze3d/platform/Window;",
//			shift = At.Shift.BEFORE,
//			ordinal = 0), cancellable = true)
//	public void eyemineSetInputMode(CallbackInfo ci) {
//		GLFW.glfwSetInputMode(this.minecraft.getWindow().getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
//	}
//
//
//	@Inject(method = "handleAccumulatedMovement()V",
//			locals = LocalCapture.CAPTURE_FAILEXCEPTION, at = @At(
//			value = "INVOKE",
//			target = "Lnet/minecraft/client/MouseHandler;isMouseGrabbed()Z",
//			shift = At.Shift.BEFORE,
//			ordinal = 0), cancellable = true)
//	public void eyemine$ProcessOnMove(CallbackInfo ci) {
//		if (this.minecraft.isWindowActive()) {
//			this.architectury_eyemine$processMousePosition(xpos, ypos);
//		}
//	}
//
//	@Inject(method = "turnPlayer(D)V",
//			locals = LocalCapture.CAPTURE_FAILEXCEPTION, at = @At(
//			value = "RETURN"), cancellable = true)
//	public void eyemine$GrabMouseOnMove3(CallbackInfo ci) {
//		// Reset to centre
//		if (!MouseHelper.ungrabbedMouseMode) {
//			GLFW.glfwSetCursorPos(this.minecraft.getWindow().getWindow(), 0, 0);
//			this.xpos = 0;
//			this.ypos = 0;
//		}
//		ci.cancel();
//	}
//
//	@Unique
//	private void architectury_eyemine$processMousePosition(double x, double y) {
//		double w_half = (double) this.minecraft.getWindow().getScreenWidth() / 2;
//		double h_half = (double) this.minecraft.getWindow().getScreenHeight() / 2;
//
//		// adjust coordinates to centralised when ungrabbed
//		if (MouseHelper.ungrabbedMouseMode) {
//			x -= w_half;
//			y -= h_half;
//		}
//
//		double x_abs = Math.abs(x);
//		double y_abs = Math.abs(y);
//
//		double deltaX = 0;
//		double deltaY = 0;
//
//		// If mouse is outside minecraft window, throw it away
//		if (x_abs > w_half * (1 - this.eyemine$deadBorder) ||
//				y_abs > h_half * (1 - this.eyemine$deadBorder)) {
//			// do nothing
//			this.architectury_eyemine$resetVelocity();
//		} else {
//			// If mouse is around edges, clip effect
//			if (x_abs > w_half * (1 - this.eyemine$clipBorderHorizontal)) {
//				x = (int) (Math.signum(x) * (w_half * (1 - this.eyemine$clipBorderHorizontal)));
//			}
//			if (y_abs > h_half * (1 - this.eyemine$clipBorderVertical)) {
//				y = (int) (Math.signum(y) * (h_half * (1 - this.eyemine$clipBorderVertical)));
//			}
//			deltaX = x;
//			deltaY = y;
//
//			this.accumulatedDX = deltaX;
//			this.accumulatedDY = deltaY;
//
//			// Remember there was a valid event, even if we're not moving
//			MouseHelper.mHasPendingEvent = true;
//		}
//	}
//
//	@Unique
//	private void architectury_eyemine$resetVelocity() {
//		MouseHelper.lastXVelocity = this.accumulatedDX;
//		MouseHelper.lastYVelocity = this.accumulatedDY;
//		this.accumulatedDX = 0.0D;
//		this.accumulatedDY = 0.0D;
//	}
//
//	@Inject(method = "turnPlayer(D)V", at = @At(value = "HEAD"), cancellable = true)
//	public void EyeMineTurnPlayer(double movementTime, CallbackInfo ci) {
//		// this gets called from Minecraft itself
//		if (this.minecraft.player == null) {
//			ci.cancel();
//		}
//
//		if (MouseHelper.movementState == MouseHelper.PlayerMovement.VANILLA) {
//			architectury_eyemine$updatePlayerLookVanilla(movementTime);
//		} else if (MouseHelper.movementState == MouseHelper.PlayerMovement.LEGACY) {
//			architectury_eyemine$updatePlayerLookLegacy(movementTime);
//		} else {
//			// keep track of last time
//			double d0 = Blaze3D.getTime();
//			this.lastHandleMovementTime = d0;
//		}
//
//		ci.cancel();
//	}
//
//	@Unique
//	public void architectury_eyemine$updatePlayerLookVanilla(double movementTime) {
//		double sensitivity = (Double) this.minecraft.options.sensitivity().get() * 0.6000000238418579 + 0.20000000298023224;
//		double f = sensitivity * sensitivity * sensitivity;
//		double g = f * 8.0;
//		double j;
//		double k;
//		if (this.minecraft.options.smoothCamera) {
//			double h = this.smoothTurnX.getNewDeltaValue(this.accumulatedDX * g, movementTime * g);
//			double i = this.smoothTurnY.getNewDeltaValue(this.accumulatedDY * g, movementTime * g);
//			j = h;
//			k = i;
//		} else if (this.minecraft.options.getCameraType().isFirstPerson() && this.minecraft.player.isScoping()) {
//			this.smoothTurnX.reset();
//			this.smoothTurnY.reset();
//			j = this.accumulatedDX * f;
//			k = this.accumulatedDY * f;
//		} else {
//			this.smoothTurnX.reset();
//			this.smoothTurnY.reset();
//			j = this.accumulatedDX * g;
//			k = this.accumulatedDY * g;
//		}
//
//		int l = 1;
//		if (this.minecraft.options.invertYMouse().get()) {
//			l = -1;
//		}
//
//		this.minecraft.getTutorial().onMouse(j, k);
//		if (this.minecraft.player != null) {
//			this.minecraft.player.turn(j, k * (double) l);
//		}
//	}
//
//	//TODO: Check if this is correct or if it needs to be updated
//	@Unique
//	public void architectury_eyemine$updatePlayerLookLegacy(double movementTime) {
//		// Rotate the player (yaw) according to x position only
//		double d0 = Blaze3D.getTime();
//		this.lastHandleMovementTime = d0;
//		if (this.minecraft.isWindowActive()) {
//			double d4 = 0.1 * this.minecraft.options.sensitivity().get() * (double) 0.6F + (double) 0.2F;
//			double d5 = 0.5d * d4 * d4 * d4 * 8.0D;
//			double d2;
//			if (this.minecraft.options.smoothCamera) {
//				double d6 = this.smoothTurnX.getNewDeltaValue(this.accumulatedDX * d5, movementTime * d5);
//				d2 = d6;
//			} else {
//				this.smoothTurnX.reset();
//				this.smoothTurnY.reset();
//
//				// quadratic fit near centre
//				double w = this.minecraft.getWindow().getGuiScaledWidth();
//				double h = this.minecraft.getWindow().getGuiScaledHeight();
//				double d = h / 8;
//				double p = 2; // quadratic near centre
//				double k = 2; // magnitude at inflection point
//				k = 0.5 * (d5 * w) / (1 + p * (w / (2 * d) - 1));    // adjust k so effect at edge is same as with linear version
//
//				// linear further out (but continuous at transition point)
//				double a = k * (1 - p);
//				double m = p * k / d;
//
//				if (Math.abs(this.accumulatedDX) > d) {
//					d2 = Math.signum(this.accumulatedDX) * (a + m * Math.abs(this.accumulatedDX));
//				} else {
//					d2 = Math.signum(this.accumulatedDX) * k * Math.pow(Math.abs(this.accumulatedDX) / d, p);
//				}
//
//				// When going backward, reduce the yaw effect
//				// TODO: ideally we'd have some smoother modulation here
//				double h6 = (double) this.minecraft.getWindow().getGuiScaledHeight() / 6;
//				if (this.accumulatedDY > h6) {
//					d2 *= 0.5;
//				}
//			}
//
//			this.architectury_eyemine$resetVelocity();
//
//			this.minecraft.getTutorial().onMouse(d2, 0);
//			if (this.minecraft.screen == null) {
//				if (this.minecraft.player != null) {
//					this.minecraft.player.turn(d2, 0);
//				}
//			}
//
//			// TODO: use the y position to walk forward/back too: or does this happen in WalkWithGaze2 mod?
//		} else {
//			this.architectury_eyemine$resetVelocity();
//		}
//	}
//
//	/**
//	 * Returns true if the mouse is grabbed.
//	 */
//	@Inject(method = "isMouseGrabbed()Z", at = @At("RETURN"), cancellable = true)
//	public void EyeMine$IsMouseGrabbed(CallbackInfoReturnable<Boolean> cir) {
//		// Somewhere deep in the MC engine, this is being queried to see whether mining should
//		// occur, so we have to lie a little.
//		boolean flag = this.mouseGrabbed;
//		boolean flag2 = (this.minecraft.isWindowActive() && MouseHelper.ungrabbedMouseMode);
//		cir.setReturnValue(flag || flag2);
//	}
//
//	@Inject(method = "grabMouse()V", at = @At(value = "HEAD"), cancellable = true)
//	public void EyeMine$GrabMouse(CallbackInfo ci) {
//		EyeMine.LOGGER.info("grabMouse");
//		if (!MouseHelper.hasGLcontext()) {
//			EyeMine.LOGGER.info("grabMouse: no GL context");
//			ci.cancel();
//		}
//	}
//
//	@Inject(method = "grabMouse()V",
//			locals = LocalCapture.CAPTURE_FAILEXCEPTION, at = @At(
//			value = "INVOKE",
//			target = "Lcom/mojang/blaze3d/platform/InputConstants;grabOrReleaseMouse(JIDD)V",
//			shift = At.Shift.BEFORE,
//			ordinal = 0), cancellable = true)
//	public void EyeMine$OnlyGrabWhenUngrabbed(CallbackInfo ci) {
//		if (!MouseHelper.ungrabbedMouseMode) {
//			InputConstants.grabOrReleaseMouse(this.minecraft.getWindow().getWindow(), 212995, this.xpos, this.ypos);
//		}
//		this.minecraft.setScreen((Screen) null);
//		this.minecraft.missTime = 10000;
//		this.ignoreFirstMove = true;
//		ci.cancel();
//	}
//
//	/**
//	 * Resets the player keystate, disables the ingame focus, and ungrabs the mouse cursor.
//	 */
//	@Inject(at = @At("HEAD"), method = "releaseMouse()V", cancellable = true)
//	public void EyeMine$ReleaseMouse(CallbackInfo ci) {
//		if (!MouseHelper.hasGLcontext()) {
//			ci.cancel();
//		}
//	}
}

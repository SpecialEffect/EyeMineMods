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
import com.specialeffect.eyemine.utils.MouseHelper;
import com.specialeffect.eyemine.utils.MouseHelper.PlayerMovement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.SmoothDouble;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

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
	private boolean ignoreFirstMove = true;
	@Shadow
	@Final
	private final SmoothDouble smoothTurnX = new SmoothDouble();
	@Shadow
	@Final
	private final SmoothDouble smoothTurnY = new SmoothDouble();
	@Shadow
	private double accumulatedDX;
	@Shadow
	private double accumulatedDY;
	@Shadow
	private double lastMouseEventTime = Double.MIN_VALUE;
	@Shadow
	private boolean mouseGrabbed;

	@Unique
	public float deadBorder = 0.05f;
	@Unique
	public float clipBorderHorizontal = 0.3f;
	@Unique
	public float clipBorderVertical = 0.2f;
	// TODO: different left/right vs up/down?

	private void setClipBorders(float horz, float vert) {
		clipBorderHorizontal = horz;
		clipBorderVertical = vert;
	}

	@SuppressWarnings("unused")
	private void debugLogging() {
		long cursorMode = GLFW.glfwGetInputMode(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_CURSOR);
		EyeMine.LOGGER.debug("***");
		if (cursorMode == GLFW.GLFW_CURSOR_DISABLED) {
			EyeMine.LOGGER.debug("cursor disabled");
			long raw = GLFW.glfwGetInputMode(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_RAW_MOUSE_MOTION);
			if (raw == GLFW.GLFW_TRUE) {
				EyeMine.LOGGER.debug("using raw motion");
			}
		} else if (cursorMode == GLFW.GLFW_CURSOR_NORMAL) {
			EyeMine.LOGGER.debug("normal cursor");
		}

		if (GLFW.GLFW_TRUE == GLFW.glfwGetWindowAttrib(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_HOVERED)) {
			EyeMine.LOGGER.debug("hovered");
		}
	}

	@Inject(method = "onMove(JDD)V",
			locals = LocalCapture.CAPTURE_FAILEXCEPTION, at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/Minecraft;getWindow()Lcom/mojang/blaze3d/platform/Window;",
			shift = Shift.BEFORE,
			ordinal = 1))
	public void EyeMineInputModeOnMove(long handle, double xpos, double ypos, CallbackInfo ci) {
		GLFW.glfwSetInputMode(this.minecraft.getWindow().getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
	}

	@Inject(method = "onMove(JDD)V",
			locals = LocalCapture.CAPTURE_FAILEXCEPTION, at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/Minecraft;getProfiler()Lnet/minecraft/util/profiling/ProfilerFiller;",
			shift = Shift.BEFORE,
			ordinal = 0), cancellable = true)
	public void EyeMineGrabMouseOnMove(long handle, double xpos, double ypos, CallbackInfo ci) {
		GuiEventListener guiEventListener = this.minecraft.screen;
		if (guiEventListener != null && this.minecraft.getOverlay() == null) {
			//The if from earlier just so we can turn it into an if/else
			this.xpos = xpos;
			this.ypos = ypos;
		} else {
			// If mouse should be grabbed but isn't - this can happen if we alt-tab
			// away while world is loading, with pauseOnLostFocus=false
			if (!MouseHelper.ungrabbedMouseMode && this.minecraft.isWindowActive() && !this.mouseGrabbed) {
				((MouseHandler) (Object) this).grabMouse();
			}

			this.minecraft.getProfiler().push("mouse");
			if (this.minecraft.isWindowActive()) { //((MouseHandler)(Object) this).isMouseGrabbed() &&
				this.processMousePosition(xpos, ypos);
			}

			((MouseHandler) (Object) this).turnPlayer();

			// Reset to centre
			if (!MouseHelper.ungrabbedMouseMode) {
				GLFW.glfwSetCursorPos(this.minecraft.getWindow().getWindow(), 0, 0);
				this.xpos = 0;
				this.ypos = 0;
			}
			this.minecraft.getProfiler().pop();
		}
		ci.cancel();
	}

	private void processMousePosition(double x, double y) {
		double w_half = (double) this.minecraft.getWindow().getScreenWidth() / 2;
		double h_half = (double) this.minecraft.getWindow().getScreenHeight() / 2;

		// adjust coordinates to centralised when ungrabbed
		if (MouseHelper.ungrabbedMouseMode) {
			x -= w_half;
			y -= h_half;
		}

		double x_abs = Math.abs(x);
		double y_abs = Math.abs(y);

		double deltaX = 0;
		double deltaY = 0;

		// If mouse is outside minecraft window, throw it away
		if (x_abs > w_half * (1 - this.deadBorder) ||
				y_abs > h_half * (1 - this.deadBorder)) {
			// do nothing
			this.resetVelocity();
		} else {
			// If mouse is around edges, clip effect
			if (x_abs > w_half * (1 - this.clipBorderHorizontal)) {
				x = (int) (Math.signum(x) * (w_half * (1 - this.clipBorderHorizontal)));
			}
			if (y_abs > h_half * (1 - this.clipBorderVertical)) {
				y = (int) (Math.signum(y) * (h_half * (1 - this.clipBorderVertical)));
			}
			deltaX = x;
			deltaY = y;

			this.accumulatedDX = deltaX;
			this.accumulatedDY = deltaY;

			// Remember there was a valid event, even if we're not moving
			MouseHelper.mHasPendingEvent = true;
		}
	}

	public void updatePlayerLookLegacy() {
		// Rotate the player (yaw) according to x position only
		double d0 = Blaze3D.getTime();
		double d1 = d0 - this.lastMouseEventTime;
		this.lastMouseEventTime = d0;
		if (this.minecraft.isWindowActive()) {
			double d4 = 0.1 * this.minecraft.options.sensitivity().get() * (double) 0.6F + (double) 0.2F;
			double d5 = 0.5d * d4 * d4 * d4 * 8.0D;
			double d2;
			if (this.minecraft.options.smoothCamera) {
				double d6 = this.smoothTurnX.getNewDeltaValue(this.accumulatedDX * d5, d1 * d5);
				d2 = d6;
			} else {
				this.smoothTurnX.reset();
				this.smoothTurnY.reset();

				// quadratic fit near centre
				double w = Minecraft.getInstance().getWindow().getGuiScaledWidth();
				double h = Minecraft.getInstance().getWindow().getGuiScaledHeight();
				double d = h / 8;
				double p = 2; // quadratic near centre
				double k = 2; // magnitude at inflection point
				k = 0.5 * (d5 * w) / (1 + p * (w / (2 * d) - 1));    // adjust k so effect at edge is same as with linear version

				// linear further out (but continuous at transition point)
				double a = k * (1 - p);
				double m = p * k / d;

				if (Math.abs(this.accumulatedDX) > d) {
					d2 = Math.signum(this.accumulatedDX) * (a + m * Math.abs(this.accumulatedDX));
				} else {
					d2 = Math.signum(this.accumulatedDX) * k * Math.pow(Math.abs(this.accumulatedDX) / d, p);
				}

				// When going backward, reduce the yaw effect
				// TODO: ideally we'd have some smoother modulation here
				double h6 = (double) Minecraft.getInstance().getWindow().getGuiScaledHeight() / 6;
				if (this.accumulatedDY > h6) {
					d2 *= 0.5;
				}
			}

			this.resetVelocity();

			this.minecraft.getTutorial().onMouse(d2, 0);
			if (this.minecraft.screen == null) {
				if (this.minecraft.player != null) {
					this.minecraft.player.turn(d2, 0);
				}
			}

			// TODO: use the y position to walk forward/back too: or does this happen in WalkWithGaze2 mod?
		} else {
			this.resetVelocity();
		}
	}

	private void resetVelocity() {
		MouseHelper.lastXVelocity = this.accumulatedDX;
		MouseHelper.lastYVelocity = this.accumulatedDY;
		this.accumulatedDX = 0.0D;
		this.accumulatedDY = 0.0D;
	}

	public void updatePlayerLookVanilla() {
		double d0 = Blaze3D.getTime();
		double d1 = d0 - this.lastMouseEventTime;
		this.lastMouseEventTime = d0;
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
				double w = Minecraft.getInstance().getWindow().getGuiScaledWidth();
				double h = Minecraft.getInstance().getWindow().getGuiScaledHeight();
				double d = h / 8;

				double p = 2; // quadratic near centre
				double k = 2; // magnitude at inflection point
				k = 0.5 * (d5 * w) / (1 + p * (w / (2 * d) - 1));

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

			this.resetVelocity();
			int i = 1;
			if (this.minecraft.options.invertYMouse().get()) {
				i = -1;
			}

			this.minecraft.getTutorial().onMouse(d2, d3);
			if (this.minecraft.player != null) {
				this.minecraft.player.turn(d2, d3 * (double) i);
			}

		} else {
			this.resetVelocity();
		}
	}

	@Inject(method = "turnPlayer()V", at = @At(value = "HEAD"), cancellable = true)
	public void EyeMineTurnPlayer(CallbackInfo ci) {
		// this gets called from Minecraft itself
		if (this.minecraft.player == null) {
			ci.cancel();
		}

		if (MouseHelper.movementState == PlayerMovement.VANILLA) {
			updatePlayerLookVanilla();
		} else if (MouseHelper.movementState == PlayerMovement.LEGACY) {
			updatePlayerLookLegacy();
		} else {
			// keep track of last time
			double d0 = Blaze3D.getTime();
			this.lastMouseEventTime = d0;
		}

		ci.cancel();
	}

	/**
	 * Returns true if the mouse is grabbed.
	 */
	@Inject(method = "isMouseGrabbed()Z", at = @At("RETURN"), cancellable = true)
	public void EyeMineIsMouseGrabbed(CallbackInfoReturnable<Boolean> cir) {
		// Somewhere deep in the MC engine, this is being queried to see whether mining should
		// occur, so we have to lie a little.
		boolean flag = this.mouseGrabbed;
		boolean flag2 = (this.minecraft.isWindowActive() && MouseHelper.ungrabbedMouseMode);
		cir.setReturnValue(flag || flag2);
	}

	@Inject(method = "grabMouse()V", at = @At(value = "HEAD"), cancellable = true)
	public void EyeMineGrabMouse(CallbackInfo ci) {
		EyeMine.LOGGER.info("grabMouse");
		if (!MouseHelper.hasGLcontext()) {
			ci.cancel();
		}
	}

	@Inject(method = "grabMouse()V",
			locals = LocalCapture.CAPTURE_FAILEXCEPTION, at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/platform/InputConstants;grabOrReleaseMouse(JIDD)V",
			shift = Shift.BEFORE,
			ordinal = 0), cancellable = true)
	public void EyeMineOnlyGrabWhenUngrabbed(CallbackInfo ci) {
		if (!MouseHelper.ungrabbedMouseMode) {
			InputConstants.grabOrReleaseMouse(this.minecraft.getWindow().getWindow(), 212995, this.xpos, this.ypos);
		}

		this.minecraft.setScreen((Screen) null);
		//FIXME: this.minecraft.missTime = 10000;
		this.ignoreFirstMove = true;
		ci.cancel();
	}

	/**
	 * Resets the player keystate, disables the ingame focus, and ungrabs the mouse cursor.
	 */
	@Inject(at = @At("HEAD"), method = "releaseMouse()V", cancellable = true)
	public void EyeMineReleaseMouse(CallbackInfo ci) {
		if (!MouseHelper.hasGLcontext()) {
			ci.cancel();
		}
	}
}

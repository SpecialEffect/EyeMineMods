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

import com.specialeffect.eyemine.utils.KeyboardInputHelper;
import net.minecraft.client.Options;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin extends Input {
	@Shadow
	@Final
	private Options options;

	@Inject(method = "tick(ZF)V",
			locals = LocalCapture.CAPTURE_FAILEXCEPTION, at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/client/player/KeyboardInput;up:Z",
			shift = Shift.AFTER,
			ordinal = 0))
	public void EyeMineTickSetUp(boolean bl, float f, CallbackInfo ci) {
		this.up = this.options.keyUp.isDown() || KeyboardInputHelper.mWalkForwardOverride.get();
	}

	@Inject(method = "tick(ZF)V",
			locals = LocalCapture.CAPTURE_FAILEXCEPTION, at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/client/player/KeyboardInput;forwardImpulse:F",
			shift = Shift.AFTER,
			ordinal = 0))
	public void EyeMineTick(boolean bl, float f, CallbackInfo ci) {
		if (KeyboardInputHelper.mWalkForwardOverride.get()) {
			this.forwardImpulse = Math.max(-1, Math.min(1, KeyboardInputHelper.mOverrideWalkSpeed));
			// TODO: do we still want as drastic a sneak-slowing-down with eyemine??
		}
	}
}

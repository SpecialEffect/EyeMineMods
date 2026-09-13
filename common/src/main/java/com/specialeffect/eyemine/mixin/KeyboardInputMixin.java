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
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin extends ClientInput {

	/**
	 * After tick() computes key presses and moveVector, override forward movement
	 * if EyeMine walk override is active.
	 *
	 * In 26.1.1, Input is an immutable record (boolean flags only), so we:
	 * 1. Set the forward flag to true in keyPresses
	 * 2. Override moveVector.x (forward impulse) with the fractional walk speed
	 *
	 * moveVector is a Vec2 where x = left/right (strafe) impulse, y = forward/backward impulse
	 */
	@Inject(method = "tick()V", at = @At("TAIL"))
	public void eyemine$overrideWalkForward(CallbackInfo ci) {
		if (KeyboardInputHelper.mWalkForwardOverride.get()) {
			Input current = this.keyPresses;
			this.keyPresses = new Input(
					true,
					current.backward(),
					current.left(),
					current.right(),
					current.jump(),
					current.shift(),
					current.sprint()
			);
			float speed = Math.max(-1, Math.min(1, KeyboardInputHelper.mOverrideWalkSpeed));
			this.moveVector = new Vec2(this.moveVector.x, speed);
		}
	}
}

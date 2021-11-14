/**
 * Copyright (C) 2016-2020 Kirsty McNaught
 *
 * Developed for SpecialEffect, www.specialeffect.org.uk
 *
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
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin extends Input {
	@Shadow @Final private Options options;

	/**
	 * @author Mrbysco
	 * @reason No other feasible way
	 */
	@Overwrite
	public void tick(boolean slow) {
		KeyboardInputHelper keyboardHelper = KeyboardInputHelper.instance();
		this.up = this.options.keyUp.isDown() || keyboardHelper.mWalkForwardOverride.get();
		this.down = this.options.keyDown.isDown();
		this.left = this.options.keyLeft.isDown();
		this.right = this.options.keyRight.isDown();
		this.forwardImpulse = this.up == this.down ? 0.0F : (this.up ? 1.0F : -1.0F);
		if (keyboardHelper.mWalkForwardOverride.get()) {
			this.forwardImpulse = Math.max(-1, Math.min(1, keyboardHelper.mOverrideWalkSpeed));
			// TODO: do we still want as drastic a sneak-slowing-down with eyemine??
		}
		this.leftImpulse = this.left == this.right ? 0.0F : (this.left ? 1.0F : -1.0F);
		this.jumping = this.options.keyJump.isDown();
		this.shiftKeyDown = this.options.keyShift.isDown();
		if (slow) {
			this.leftImpulse = (float)((double)this.leftImpulse * 0.3D);
			this.forwardImpulse = (float)((double)this.forwardImpulse * 0.3D);
		}
	}
}

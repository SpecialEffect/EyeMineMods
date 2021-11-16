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

package com.specialeffect.eyemine.utils;

import java.util.concurrent.atomic.AtomicBoolean;

public class KeyboardInputHelper {
	public static AtomicBoolean mSneakOverride = new AtomicBoolean(false);
	public static AtomicBoolean mWalkForwardOverride = new AtomicBoolean(false);
	public static float mOverrideWalkSpeed = 1.0f;

	public static void setSneakOverride(boolean b) {
		mSneakOverride.set(b);
	}

	public static void setWalkOverride(boolean b, float walkSpeed) {
		// Setting a speed of zero doesn't *always* result in no movement - e.g. boat will still move forward
		// so we want a request of ~0.0 to be equivalent to turning off the override.
		if (Math.abs(walkSpeed) > 1e-5) {
			mWalkForwardOverride.set(b);
			mOverrideWalkSpeed = walkSpeed; // TODO: is concurrency an issue?
		}
		else {
			mWalkForwardOverride.set(false);
		}
	}
}

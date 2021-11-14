package com.specialeffect.eyemine.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;

public class GLContext {
	@ExpectPlatform
	public static boolean hasGLcontext() {
		// Just throw an error, the content should get replaced at runtime.
		throw new AssertionError();
	}
}

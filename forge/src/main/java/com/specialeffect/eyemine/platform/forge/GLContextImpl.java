package com.specialeffect.eyemine.platform.forge;

import org.lwjgl.glfw.GLFW;

public class GLContextImpl {
	public static boolean hasGLcontext() {
		return GLFW.glfwGetCurrentContext() != 0;
	}
}

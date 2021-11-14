package com.specialeffect.eyemine.platform.fabric;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Overlay;

public class GLContextImpl {
	public static boolean hasGLcontext() {
		return RenderSystem.isOnGameThread() && !(Minecraft.getInstance().getOverlay() instanceof Overlay);
	}
}

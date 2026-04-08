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

package com.specialeffect.eyemine.client.gui.crosshair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class JoystickControlOverlay implements ICrosshairOverlay {

	public JoystickControlOverlay() {
		mResource = Identifier.fromNamespaceAndPath("eyemine", "textures/icons/overlay.png");
	}

	Identifier mResource;

	private boolean mVisible = false;

	private float mAlpha = 0.3f;

	public void setVisible(boolean bVisible) {
		mVisible = bVisible;
	}

	public void setAlpha(float alpha) {
		if (alpha > 0.0f && alpha < 0.9f) {
			alpha += 0.1f;
		}
		mAlpha = alpha;
	}

	@Override
	public void renderOverlay(GuiGraphicsExtractor guiGraphics, Minecraft minecraft) {
		if (mVisible && mAlpha > 0.0f) {

			int w = minecraft.getWindow().getGuiScaledWidth();
			int h = minecraft.getWindow().getGuiScaledHeight();

			int a = Math.clamp((int) (mAlpha * 255), 0, 255);
			int color = (a << 24) | 0xFFFFFF;
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, mResource, 0, 0, 0, 0, w, h, w, h, color);

		}
	}
}

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

package com.specialeffect.eyemine.client.gui.crosshair;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.specialeffect.utils.ModUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class JoystickControlOverlay implements ICrosshairOverlay {

	public JoystickControlOverlay() {
		mResource = new ResourceLocation("eyemine", "textures/icons/overlay.png");
	}

	ResourceLocation mResource;

	private boolean mVisible = false;
	
	private float mAlpha = 0.3f;

	public void setVisible(boolean bVisible) {
		mVisible = bVisible;
	}
	
	public void setAlpha(float alpha) {
		// Minecraft clips alpha at 0.1, so we add 0.1 back in to get reasonable user-facing behaviour
		if (alpha > 0.0f && alpha < 0.9f) {
			alpha += 0.1f;
		}
		mAlpha = alpha;
	}

	@Override
	public void renderOverlay(PoseStack poseStack, Minecraft minecraft) {
		if (mVisible && mAlpha > 0.0f) {
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

			int w = minecraft.getWindow().getGuiScaledWidth();
			int h = minecraft.getWindow().getGuiScaledHeight();

			minecraft.getTextureManager().bind(mResource);
			ModUtils.drawTexQuad(0, 0, w, h, mAlpha);

			RenderSystem.disableBlend();
		}
	}
}
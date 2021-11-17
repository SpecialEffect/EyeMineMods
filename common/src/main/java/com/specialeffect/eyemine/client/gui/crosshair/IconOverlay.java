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

public class IconOverlay implements ICrosshairOverlay {
	private ResourceLocation mResource;

	// Current state
	public boolean mVisible = false;

	// Position/appearance (see setters)
	// Position/size are relative to screen
	public float mCentreX = 0.5f;
	public float mCentreY = 0.5f;
	public float mHeight = 1.0f;
	public float mAspectRatio = 1.0f;
	public float mAlpha = 1.0f;
	public int fadeTime = 10;
	public int fadeCountdown = 0;

	public IconOverlay(Minecraft mc, String resourcePath) {
		mResource = new ResourceLocation(resourcePath);
	}

	public void setPosition(float centreX, float centreY, float height, float aspectRatio) {
		mCentreX = centreX;
		mCentreY = centreY;
		mAspectRatio = aspectRatio;
		mHeight = height;
	}

	public void setVisible(boolean visible) {
		if (visible != mVisible) {
			fadeCountdown = fadeTime;
		}

		mVisible = visible;
	}

	public void setAlpha(float alpha) {
		// Minecraft clips alpha at 0.1, so we add 0.1 back in to get reasonable user-facing behaviour
		if (alpha > 0.0f && alpha < 0.9f) {
			alpha += 0.1f;
		}
		mAlpha = alpha;
	}

	// A helper function to draw a texture scaled to fit.
	public void drawTexture(Minecraft minecraft, int screenHeight, int screenWidth, float fade) {
		// calculate position
		int height = (int)(screenWidth*mHeight);
		int width = (int)(height*mAspectRatio);
		int centreX = (int)(mCentreX*screenWidth);
		int centreY = (int)(mCentreY*screenHeight);

		// render the texture
		// TODO:white? black? drop shadow?
		minecraft.getTextureManager().bind(mResource);
		ModUtils.drawTexQuad(centreX - width/2, centreY - height/2,
				width, height, mAlpha*fade);
	}

	@Override
	public void renderOverlay(PoseStack poseStack, Minecraft minecraft) {
		if (mAlpha > 0.0 && (mVisible || fadeCountdown > 0)) {
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

			// Update fading ticks
			float fade = 1.0f;
			if (fadeCountdown > 0) {
				fadeCountdown--;
				fade = fadeCountdown/(float)fadeTime;
				if (mVisible) {
					fade = 1.0f - fade;
				}
			}

			// Don't show if the debug screen is open
			if (minecraft.options.renderDebug) {
				return;
			}

			int w = minecraft.getWindow().getGuiScaledWidth();
			int h = minecraft.getWindow().getGuiScaledHeight();
			drawTexture(minecraft, h, w, fade);

			RenderSystem.disableBlend();
		}
	}
}
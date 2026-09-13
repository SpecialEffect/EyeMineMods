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

public class IconOverlay implements ICrosshairOverlay {
	private Identifier mResource;

	public boolean mVisible = false;
	public float mCentreX = 0.5f;
	public float mCentreY = 0.5f;
	public float mHeight = 1.0f;
	public float mAspectRatio = 1.0f;
	public float mAlpha = 1.0f;
	public int fadeTime = 10;
	public int fadeCountdown = 0;

	public IconOverlay(Minecraft mc, String resourcePath) {
		mResource = Identifier.tryParse(resourcePath);
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
		if (alpha > 0.0f && alpha < 0.9f) {
			alpha += 0.1f;
		}
		mAlpha = alpha;
	}

	private static int colorWithAlpha(float alpha) {
		int a = Math.clamp((int) (alpha * 255), 0, 255);
		return (a << 24) | 0xFFFFFF;
	}

	public void drawTexture(GuiGraphicsExtractor guiGraphics, int screenHeight, int screenWidth, float fade) {
		int height = (int) (screenWidth * mHeight);
		int width = (int) (height * mAspectRatio);
		int centreX = (int) (mCentreX * screenWidth);
		int centreY = (int) (mCentreY * screenHeight);

		float alpha = mAlpha * fade;
		int x = centreX - width / 2;
		int y = centreY - height / 2;
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, mResource, x, y, 0, 0, width, height, width, height, colorWithAlpha(alpha));
	}

	@Override
	public void renderOverlay(GuiGraphicsExtractor guiGraphics, Minecraft minecraft) {
		if (mAlpha > 0.0 && (mVisible || fadeCountdown > 0)) {
			float fade = 1.0f;
			if (fadeCountdown > 0) {
				fadeCountdown--;
				fade = fadeCountdown / (float) fadeTime;
				if (mVisible) {
					fade = 1.0f - fade;
				}
			}

			if (minecraft.getDebugOverlay().showDebugScreen()) {
				return;
			}

			int w = minecraft.getWindow().getGuiScaledWidth();
			int h = minecraft.getWindow().getGuiScaledHeight();
			drawTexture(guiGraphics, h, w, fade);
		}
	}
}

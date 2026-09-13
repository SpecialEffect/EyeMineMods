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

import java.util.ArrayList;
import java.util.List;

//
// StateOverlay implements a simple status bar at the top of the screen which
// shows the current states such as attacking, walking, etc.
//
public class StateOverlay implements ICrosshairOverlay {
	public StateOverlay() {
		mResourcesLeft = new ArrayList<>();
		mResourcesRight = new ArrayList<>();
		mFlagsLeft = new ArrayList<>();
		mFlagsRight = new ArrayList<>();
	}

	private void rescale() {
		int maxSizeByWidth = mDisplayWidth / (mIconsPerRow + mIconPadding);
		int maxSizeByHeight = 2 * mDisplayHeight / (mIconsPerRow + mIconPadding);
		mIconSize = Math.min(maxSizeByWidth, maxSizeByHeight);
	}

	private static int mIconSize = 30;
	private static int mIconPadding = 5;
	private int mDisplayWidth;
	private int mDisplayHeight;
	private static final int mIconsPerRow = 10;

	private static List<Identifier> mResourcesLeft;
	private static List<Identifier> mResourcesRight;
	private static List<Boolean> mFlagsLeft;
	private static List<Boolean> mFlagsRight;

	public synchronized static int registerTextureLeft(String filepath) {
		Identifier res = Identifier.tryParse(filepath);
		mResourcesLeft.add(res);
		mFlagsLeft.add(false);
		return mResourcesLeft.size() - 1;
	}

	public synchronized static int registerTextureRight(String filepath) {
		Identifier res = Identifier.tryParse(filepath);
		mResourcesRight.add(res);
		mFlagsRight.add(false);
		return mResourcesRight.size() - 1;
	}

	/** Pack ARGB color int from alpha (0.0-1.0) and white RGB */
	private static int colorWithAlpha(float alpha) {
		int a = Math.clamp((int) (alpha * 255), 0, 255);
		return (a << 24) | 0xFFFFFF;
	}

	private void drawScaledTextureWithGlow(GuiGraphicsExtractor guiGraphics, Identifier res, int x, int y, int width, int height) {
		// Draw blur glow effect - progressive larger blits at low alpha
		int blurSteps = 4;
		double totalBlur = (double) width / 12;

		for (int i = 0; i < blurSteps; i++) {
			double blurAmount = totalBlur / blurSteps * (i + 1);
			float alpha = 1.0f / blurSteps;
			int bx = (int) (x - blurAmount);
			int by = (int) (y - blurAmount);
			int bw = (int) (width + 2 * blurAmount);
			int bh = (int) (height + 2 * blurAmount);
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, res, bx, by, 0, 0, bw, bh, bw, bh, colorWithAlpha(alpha));
		}

		// Draw the actual icon at full alpha
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, res, x, y, 0, 0, width, height, width, height, colorWithAlpha(1.0f));
	}

	public static void setStateLeftIcon(int i, boolean b) {
		mFlagsLeft.set(i, b);
	}

	public static void setStateRightIcon(int i, boolean b) {
		mFlagsRight.set(i, b);
	}

	@Override
	public void renderOverlay(GuiGraphicsExtractor guiGraphics, Minecraft minecraft) {
		if (minecraft.getDebugOverlay().showDebugScreen()) {
			return;
		}

		mDisplayWidth = minecraft.getWindow().getGuiScaledWidth();
		mDisplayHeight = minecraft.getWindow().getGuiScaledHeight();
		this.rescale();

		// LEFT icons
		int xPos = mIconPadding;
		int yPos = mIconPadding;
		for (int i = 0; i < mResourcesLeft.size(); i++) {
			if (mFlagsLeft.get(i)) {
				drawScaledTextureWithGlow(guiGraphics, mResourcesLeft.get(i), xPos, yPos, mIconSize, mIconSize);
			}
			xPos += mIconSize + mIconPadding;
		}

		// RIGHT ICONS
		xPos = mDisplayWidth - mIconSize - mIconPadding;
		for (int i = 0; i < mResourcesRight.size(); i++) {
			if (mFlagsRight.get(i)) {
				drawScaledTextureWithGlow(guiGraphics, mResourcesRight.get(i), xPos, yPos, mIconSize, mIconSize);
			}
			xPos -= (mIconSize + mIconPadding);
		}
	}
}

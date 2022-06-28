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

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.specialeffect.utils.ModUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

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
		// Scale icon sizes to fit screen		

		int maxSizeByWidth = mDisplayWidth / (mIconsPerRow + mIconPadding);
		int maxSizeByHeight = 2 * mDisplayHeight / (mIconsPerRow + mIconPadding);
		mIconSize = Math.min(maxSizeByWidth, maxSizeByHeight);

	}

	private static int mIconSize = 30;
	private static int mIconPadding = 5;
	private int mDisplayWidth;
	private int mDisplayHeight;
	private static final int mIconsPerRow = 10;

	// Lists of icons to draw on each half of screen
	private static List<ResourceLocation> mResourcesLeft;
	private static List<ResourceLocation> mResourcesRight;
	private static List<Boolean> mFlagsLeft;
	private static List<Boolean> mFlagsRight;

	// Add texture to list of icons, return position. 
	// You need to hang onto the position to later turn the
	// icon on/off.
	public synchronized static int registerTextureLeft(String filepath) {
		ResourceLocation res = new ResourceLocation(filepath);
		mResourcesLeft.add(res);
		mFlagsLeft.add(false);
		return mResourcesLeft.size() - 1;
	}

	// Add texture to list of icons, return position. 
	// You need to hang onto the position to later turn the
	// icon on/off.
	public synchronized static int registerTextureRight(String filepath) {
		ResourceLocation res = new ResourceLocation(filepath);
		mResourcesRight.add(res);
		mFlagsRight.add(false);
		return mResourcesRight.size() - 1;
	}

	// A helper function to draw a texture scaled to fit.
	private void drawScaledTextureWithGlow(Minecraft minecraft, ResourceLocation res, int x, int y, int width, int height) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.enableTexture();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, res);

		// First draw enlarged and blurred, for glow.
		RenderSystem.texParameter(GL11.GL_TEXTURE_2D,
				GL11.GL_TEXTURE_MIN_FILTER,
				GL11.GL_LINEAR);
		RenderSystem.texParameter(GL11.GL_TEXTURE_2D,
				GL11.GL_TEXTURE_MAG_FILTER,
				GL11.GL_LINEAR);

//		GlStateManager._texEnv(GL11.GL_TEXTURE_ENV,
//				GL11.GL_TEXTURE_ENV_MODE, //TODO: figure out what this was
//				GL11.GL_ADD );

		// We draw the texture larger, in white, at progressive levels of alpha 
		// for blur effect (the alpha gets added on each layer)
		int blurSteps = 4; // how many levels of progressive blur
		double totalBlur = width / 12; // in pixels

		for (int i = 0; i < blurSteps; i++) {
			double blurAmount = totalBlur / blurSteps * (i + 1);
			ModUtils.drawTexQuad(x - blurAmount,
					y - blurAmount,
					width + 2 * blurAmount,
					height + 2 * blurAmount,
					1.0f / blurSteps);
		}

//		GlStateManager._texEnv(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE );

		// TODO: it would be nice if we could modulate the alpha of these overlays, but that doesn't
		// work naively with the GL_REPLACE strategy we're using here. Will have to brush up my
		// OpenGL knowledge to get alpha-icon with drop-shadow
		RenderSystem.setShaderTexture(0, res);
		ModUtils.drawTexQuad(x, y, width, height, 1.0f);
	}

	public static void setStateLeftIcon(int i, boolean b) {
		mFlagsLeft.set(i, b);
	}

	public static void setStateRightIcon(int i, boolean b) {
		mFlagsRight.set(i, b);
	}

	@Override
	public void renderOverlay(PoseStack poseStack, Minecraft minecraft) {
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

		// Don't show if the debug screen is open
		if (minecraft.options.renderDebug) {
			return;
		}

//		RenderSystem.disableLighting();

		mDisplayWidth = minecraft.getWindow().getGuiScaledWidth();
		mDisplayHeight = minecraft.getWindow().getGuiScaledHeight();
		this.rescale();

		// LEFT icons
		int xPos = mIconPadding;
		int yPos = mIconPadding;
		for (int i = 0; i < mResourcesLeft.size(); i++) {
			if (mFlagsLeft.get(i)) {
				drawScaledTextureWithGlow(minecraft, mResourcesLeft.get(i), xPos, yPos, mIconSize, mIconSize);
			}
			xPos += mIconSize + mIconPadding;
		}

		// RIGHT ICONS
		xPos = mDisplayWidth - mIconSize - mIconPadding;
		for (int i = 0; i < mResourcesRight.size(); i++) {
			if (mFlagsRight.get(i)) {
				drawScaledTextureWithGlow(minecraft, mResourcesRight.get(i), xPos, yPos, mIconSize, mIconSize);
			}
			xPos -= (mIconSize + mIconPadding);
		}

		RenderSystem.disableBlend();
	}
}
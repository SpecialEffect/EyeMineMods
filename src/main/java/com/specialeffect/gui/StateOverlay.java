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

package com.specialeffect.gui;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.eventbus.api.SubscribeEvent;

//
// StateOverlay implements a simple status bar at the top of the screen which 
// shows the current states such as attacking, walking, etc.
//
public class StateOverlay 
{
	public StateOverlay(Minecraft mc)
	{
		super();

		mResourcesLeft = new ArrayList<ResourceLocation>();
		mResourcesRight = new ArrayList<ResourceLocation>();
		mFlagsLeft = new ArrayList<Boolean>();
		mFlagsRight = new ArrayList<Boolean>();
	}
	
	private void rescale() {
		// Scale icon sizes to fit screen		

		int maxSizeByWidth = mDisplayWidth/(mIconsPerRow+mIconPadding);
		int maxSizeByHeight = 2*mDisplayHeight/(mIconsPerRow+mIconPadding);
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
		return mResourcesLeft.size()-1;
	}

	// Add texture to list of icons, return position. 
	// You need to hang onto the position to later turn the
	// icon on/off.
	public synchronized static int registerTextureRight(String filepath) {
		ResourceLocation res = new ResourceLocation(filepath);
		mResourcesRight.add(res);
		mFlagsRight.add(false);
		return mResourcesRight.size()-1;
	}

	// A helper function to draw a texture scaled to fit.
	private void drawScaledTextureWithGlow(ResourceLocation res,
			int x, int y, 
			int width, int height)
	{	
		
		GlStateManager.pushTextureAttributes();

		Minecraft.getInstance().getTextureManager().bindTexture(res);	
		
		// First draw enlarged and blurred, for glow.
		GlStateManager.texParameter(GL11.GL_TEXTURE_2D, 
				GL11.GL_TEXTURE_MIN_FILTER, 
				GL11.GL_LINEAR);
		GlStateManager.texParameter(GL11.GL_TEXTURE_2D, 
				GL11.GL_TEXTURE_MAG_FILTER, 
				GL11.GL_LINEAR);

		GlStateManager.texEnv(GL11.GL_TEXTURE_ENV, 
				GL11.GL_TEXTURE_ENV_MODE, 
				GL11.GL_ADD );
		
		// We draw the texture larger, in white, at progressive levels of alpha 
		// for blur effect (the alpha gets added on each layer)
		int blurSteps = 4; // how many levels of progressive blur
		double totalBlur = width/12; // in pixels				

		for (int i=0; i < blurSteps; i++) {
			double blurAmount = totalBlur/blurSteps*(i+1);
			ModUtils.drawTexQuad(x - blurAmount, 
								y - blurAmount, 
								width + 2*blurAmount, 
								height + 2*blurAmount,  
								1.0f/blurSteps);
				}

		GlStateManager.texEnv(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE );

		// TODO: it would be nice if we could modulate the alpha of these overlays, but that doesn't
		// work naively with the GL_REPLACE strategy we're using here. Will have to brush up my
		// OpenGL knowledge to get alpha-icon with drop-shadow
		Minecraft.getInstance().getTextureManager().bindTexture(res);	
		ModUtils.drawTexQuad(x, y, width, height, 1.0f);		
		
		GlStateManager.popAttributes();

	}
	
	@SubscribeEvent
	public void onRenderExperienceBar(RenderGameOverlayEvent event)
	{

		if(event.getType() != ElementType.CROSSHAIRS)
		{      
			return;
		}


		GlStateManager.enableBlend();
//        GL11.glEnable(GL11.GL_BLEND);
		GlStateManager.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
//        GLX.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
		
		// Don't show if the debug screen is open
		if (Minecraft.getInstance().gameSettings.showDebugInfo) {
			return;
		}

		GlStateManager.disableLighting();
		
		mDisplayWidth = event.getWindow().getScaledWidth();
		mDisplayHeight = event.getWindow().getScaledHeight();
		this.rescale();

		// LEFT icons
		int xPos = mIconPadding;
		int yPos = mIconPadding;
		for (int i=0; i < mResourcesLeft.size(); i++) {
			if (mFlagsLeft.get(i)) {
				drawScaledTextureWithGlow(mResourcesLeft.get(i), xPos, yPos, mIconSize, mIconSize);
			}
			xPos += mIconSize + mIconPadding;
		}
		
		// RIGHT ICONS
		xPos = mDisplayWidth - mIconSize - mIconPadding;
		for (int i=0; i < mResourcesRight.size(); i++) {
			if (mFlagsRight.get(i)) {
				drawScaledTextureWithGlow(mResourcesRight.get(i), xPos, yPos, mIconSize, mIconSize);
			}
			xPos -= (mIconSize + mIconPadding);
		}

		GlStateManager.disableBlend();
//        GL11.glDisable(GL11.GL_BLEND);

	}

	public static void setStateLeftIcon(int i, boolean b) {
		mFlagsLeft.set(i, b);	
	}

	public static void setStateRightIcon(int i, boolean b) {
		mFlagsRight.set(i, b);	
	}
}
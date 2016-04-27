/**
 * Copyright (C) 2016 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.gui;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.lwjgl.opengl.GL11;

import com.specialeffect.utils.ModUtils;

//
// StateOverlay implements a simple status bar at the top of the screen which 
// shows the current states such as attacking, walking, etc.
//
public class StateOverlay extends Gui
{
	private Minecraft mc;

	public StateOverlay(Minecraft mc)
	{
		super();

		// We need this to invoke the render engine.
		this.mc = mc;

		mResourcesLeft = new ArrayList<ResourceLocation>();
		mResourcesRight = new ArrayList<ResourceLocation>();
		mFlagsLeft = new ArrayList<Boolean>();
		mFlagsRight = new ArrayList<Boolean>();
		
	}
	
	private void rescale() {
		// Scale icon sizes to fit screen
		Point size = ModUtils.getScaledDisplaySize(mc);
		mDisplayWidth = size.x;
		mDisplayHeight = size.y;

		int maxSizeByWidth = mDisplayWidth/(mIconsPerRow+mIconPadding);
		int maxSizeByHeight = 2*mDisplayHeight/(mIconsPerRow+mIconPadding);
		mIconSize = Math.min(maxSizeByWidth, maxSizeByHeight);
		//mIconSize = 18*2;
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
	public static int registerTextureLeft(String filepath) {
		ResourceLocation res = new ResourceLocation(filepath);
		mResourcesLeft.add(res);
		mFlagsLeft.add(false);
		return mResourcesLeft.size()-1;
	}

	// Add texture to list of icons, return position. 
	// You need to hang onto the position to later turn the
	// icon on/off.
	public static int registerTextureRight(String filepath) {
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
		GL11.glPushAttrib(GL11.GL_TEXTURE_BIT);

		this.mc.renderEngine.bindTexture(res);
		
		// First draw enlarged and blurred, for glow.
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, 
				GL11.GL_TEXTURE_MIN_FILTER, 
				GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, 
				GL11.GL_TEXTURE_MAG_FILTER, 
				GL11.GL_LINEAR);

		GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_ADD );

		// We draw the texture larger, in white, at progressive levels of alpha 
		// for blur effect (the alpha gets added on each layer)
		int blurSteps = 4; // how many levels of progressive blur
		double totalBlur = width/12; // in pixels		
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f/blurSteps);

		for (int i=0; i < blurSteps; i++) {
			double blurAmount = totalBlur/blurSteps*(i+1);
			ModUtils.drawTexQuad(x - blurAmount, 
								y - blurAmount, 
								width + 2*blurAmount, 
								height + 2*blurAmount);
				}

		GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE );
		GL11.glColor3f(1.0f, 1.0f, 1.0f);
		this.mc.renderEngine.bindTexture(res);
		ModUtils.drawTexQuad(x, y, width, height);
		
		// reset GL attributes!
		GL11.glPopAttrib();

	}
	
	// This event is called by GuiIngameForge during each frame by
	// GuiIngameForge.pre() and GuiIngameForce.post().
	@SubscribeEvent
	public void onRenderExperienceBar(RenderGameOverlayEvent event)
	{

		// We draw after the ExperienceBar has drawn.  The event raised by GuiIngameForge.pre()
		// will return true from isCancelable.  If you call event.setCanceled(true) in
		// that case, the portion of rendering which this event represents will be canceled.
		// We want to draw *after* the experience bar is drawn, so we make sure isCancelable() returns
		// false and that the eventType represents the ExperienceBar event.
		if(event.isCancelable() || event.type != ElementType.EXPERIENCE)
		{      
			return;
		}
		
		// Don't show if the debug screen is open
		if (Minecraft.getMinecraft().gameSettings.showDebugInfo) {
			return;
		}
		
		this.rescale();

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_LIGHTING); 
	

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
	}

	public static void setStateLeftIcon(int i, boolean b) {
		mFlagsLeft.set(i, b);	
	}

	public static void setStateRightIcon(int i, boolean b) {
		mFlagsRight.set(i, b);	
	}
}
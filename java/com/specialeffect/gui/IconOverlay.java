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

import org.lwjgl.opengl.GL11;

import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

//
//
public class IconOverlay extends Gui
{
	private Minecraft mc;
	private ResourceLocation mResource;
	
	private boolean mVisible = false;
	
	private int mDisplayHeight;
	private int mDisplayWidth;
	private float mAlpha = 1.0f;
	
	// These are all relative to screen	
	private float mCentreX = 0.5f;
	private float mCentreY = 0.5f;
	private float mHeight = 1.0f;
	private float mAspectRatio = 1.0f;
	
	public IconOverlay(Minecraft mc, String resourcePath)
	{
		super();

		// We need this to invoke the render engine.
		this.mc = mc;

		mResource = new ResourceLocation(resourcePath);		
	}
	
	public void setPosition(float centreX, float centreY, float height, float aspectRatio) {
		mCentreX = centreX;
		mCentreY = centreY;
		mAspectRatio = aspectRatio;
		mHeight = height;
	}

	public void setVisible(boolean visible) {
		mVisible = visible;
	}
	
	public void setAlpha(float alpha) {
		mAlpha = alpha;
	}
	
	private void rescale() {
		// Scale icon sizes to fit screen
		Point size = ModUtils.getScaledDisplaySize(mc);
		
		mDisplayWidth = size.x;
		mDisplayHeight = size.y;
	}


	// A helper function to draw a texture scaled to fit.
	private void drawTexture()
	{
		// calculate position
		int height = (int)(mDisplayHeight*mHeight);
		int width = (int)(height*mAspectRatio);
		int centreX = (int)(mCentreX*mDisplayWidth);
		int centreY = (int)(mCentreY*mDisplayHeight);
		
		GL11.glDisable(GL11.GL_LIGHTING); 
		GL11.glPushAttrib(GL11.GL_TEXTURE_BIT);

		this.mc.renderEngine.bindTexture(mResource);
		
		GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_ADD );

		GL11.glColor4f(1.0f, 1.0f, 1.0f, mAlpha);

		ModUtils.drawTexQuad(centreX - width/2, centreY - height/2, 
							 width, height);
		
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
		if(event.isCancelable() || event.getType() != ElementType.EXPERIENCE)
		{      
			return;
		}
		
		// Don't show if the debug screen is open
		if (Minecraft.getMinecraft().gameSettings.showDebugInfo) {
			return;
		}
		
		if (mVisible) {	
			this.rescale();
			this.drawTexture();
		}
	}

}
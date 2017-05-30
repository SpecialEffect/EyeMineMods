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
// StateOverlay implements a simple status bar at the top of the screen which 
// shows the current states such as attacking, walking, etc.
//
public class JoystickControlOverlay extends Gui
{
	private Minecraft mc;

	public JoystickControlOverlay(Minecraft mc)
	{
		super();

		// We need this to invoke the render engine.
		this.mc = mc;
		
		mResource = new ResourceLocation("specialeffect:icons/overlay.png");

	}
	
	private void rescale() {
		// Scale icon sizes to fit screen		
		Point size = ModUtils.getScaledDisplaySize(mc);
		mDisplayWidth = size.x;
		mDisplayHeight = size.y;
	}

	private int mDisplayWidth;
	private int mDisplayHeight;

	// Lists of icons to draw on each half of screen
	ResourceLocation mResource;

	private boolean mVisible = false;
	
	public void setVisible(boolean bVisible) {
		mVisible = bVisible;
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
		
		if (mVisible) {
		
			this.rescale();

			GL11.glDisable(GL11.GL_LIGHTING); 

			this.mc.renderEngine.bindTexture(mResource);		

			GL11.glPushAttrib(GL11.GL_TEXTURE_BIT);
			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_ADD );

			GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.5f);

			ModUtils.drawTexQuad(0, 0, mDisplayWidth, mDisplayHeight);

			// reset GL attributes!
			GL11.glPopAttrib();
		}

	}

}
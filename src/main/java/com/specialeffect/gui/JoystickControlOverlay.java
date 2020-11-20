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

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GLX;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class JoystickControlOverlay {

	public JoystickControlOverlay(Minecraft mc) {
  		mResource = new ResourceLocation("specialeffect", "icons/overlay.png");
		
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
	@SubscribeEvent
	public void onRenderGameOverlayEvent(final RenderGameOverlayEvent.Post event) {

		if(event.getType() != ElementType.CROSSHAIRS)
		{      
			return;
		}
		
		if (mVisible && mAlpha > 0.0f) {	
	        GL11.glEnable(GL11.GL_BLEND);
	        GLX.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
	        
			int w = event.getWindow().getScaledWidth();
			int h = event.getWindow().getScaledHeight();			
			
			Minecraft.getInstance().getTextureManager().bindTexture(mResource);			
			ModUtils.drawTexQuad(0, 0, w, h, mAlpha);
			
	        GL11.glDisable(GL11.GL_BLEND);	        

		}
	}
}
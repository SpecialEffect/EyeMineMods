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

import com.mojang.blaze3d.platform.GlStateManager;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.config.GuiUtils;

//
// StateOverlay implements a simple status bar at the top of the screen which 
// shows the current states such as attacking, walking, etc.
//
public class JoystickControlOverlay {
	private Minecraft mc;

	public JoystickControlOverlay(Minecraft mc) {
		super();

		// We need this to invoke the render engine.
		this.mc = mc;
//		
//		we are here, minecraft is not finding this file, although it seems to match conventions from tutorial
//		suggest a google is in order - do we need to explicitly add resources to the build>?
//				no, they are in the bin folder okay..
  		mResource = new ResourceLocation("specialeffect", "icons/fly-auto.png");
	}

	private void rescale() {
		// FIXME
		/*
		 * // Scale icon sizes to fit screen Point size =
		 * ModUtils.getScaledDisplaySize(mc); mDisplayWidth = size.x; mDisplayHeight =
		 * size.y;
		 */
		

//		public static Point getScaledDisplaySize(Minecraft mc) {
//			Point p = new Point(0, 0);
//			ScaledResolution res = new ScaledResolution(mc);
//			p.setLocation(res.getScaledWidth(), res.getScaledHeight());
	//
//			return p;
		
		Screen currScreen = Minecraft.getInstance().currentScreen;
		if (currScreen != null) {
			mDisplayWidth = currScreen.width/2;
			mDisplayHeight = currScreen.height/2;
		}
		
	}

	private int mDisplayWidth;
	private int mDisplayHeight;

	ResourceLocation mResource;

	private boolean mVisible = false;

	public void setVisible(boolean bVisible) {
		mVisible = bVisible;
	}

	public void render(RenderGameOverlayEvent.Post event) {
		if (mVisible) {			

			this.rescale();
			
			
			int w = event.getWindow().getScaledWidth();
            int h = event.getWindow().getScaledHeight();
            
            // hack for testing
            w = w/2;
            h = h/2;

			//GL11.glDisable(GL11.GL_LIGHTING);
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 0.25F);
			
			//this.mc.getTextureManager().bindTexture(mResource);
			
//			int w = mDisplayWidth;
//			int h = mDisplayHeight;
									
			//GuiUtils.drawContinuousTexturedBox(0,0,0,0,w,h,w,h,0,10);
			
			

	        
	        GlStateManager.enableBlend();
	        GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

	        GuiUtils.drawTexturedModalRect(0, 0, 0, 0, w, h, 10);
			
			
			// TODO: alpha?
//
//			GL11.glPushAttrib(GL11.GL_TEXTURE_BIT);
//			GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_ADD);
//
//			GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.5f);
//			// FIXME: add alpha as user param?
//
//			ModUtils.drawTexQuad(0, 0, mDisplayWidth, mDisplayHeight);

			// reset GL attributes!
			//GL11.glPopAttrib();
		}
	}

}
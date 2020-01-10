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
  		mResource = new ResourceLocation("specialeffect", "icons/overlay.png");
	}	

	ResourceLocation mResource;

	private boolean mVisible = false;

	public void setVisible(boolean bVisible) {
		mVisible = bVisible;
	}

	public void render(RenderGameOverlayEvent.Post event) {
		if (mVisible) {			

			int w = event.getWindow().getScaledWidth();
            int h = event.getWindow().getScaledHeight();
            float alpha = 0.5f;
            
			this.mc.getTextureManager().bindTexture(mResource);			
			ModUtils.drawTexQuad(0, 0, w, h, alpha);

		}
	}

}
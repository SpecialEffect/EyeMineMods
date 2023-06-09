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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class JoystickControlOverlay implements ICrosshairOverlay {

	public JoystickControlOverlay() {
		mResource = new ResourceLocation("eyemine", "textures/icons/overlay.png");
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

	@Override
	public void renderOverlay(GuiGraphics guiGraphics, Minecraft minecraft) {
		if (mVisible && mAlpha > 0.0f) {
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

			int w = minecraft.getWindow().getGuiScaledWidth();
			int h = minecraft.getWindow().getGuiScaledHeight();

			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.setShaderTexture(0, mResource);
			ModUtils.drawTexQuad(0, 0, w, h, mAlpha);

			RenderSystem.disableBlend();
		}
	}
}
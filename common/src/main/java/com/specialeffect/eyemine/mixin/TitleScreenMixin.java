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

package com.specialeffect.eyemine.mixin;


import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {

	@Unique
	private final boolean eyemine$showFadeInAnimation = false;
	@Unique
	private long eyemine$firstRenderTime;
	@Unique
	private float eyemine$animationTime;

	protected TitleScreenMixin(Component component) {
		super(component);
	}

	@Inject(at = @At("HEAD"), method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V")
	public void eyemineTitleHeadRender(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		if (this.eyemine$firstRenderTime == 0L && this.eyemine$showFadeInAnimation) {
			this.eyemine$firstRenderTime = System.currentTimeMillis();
		}
		eyemine$animationTime = this.eyemine$showFadeInAnimation ? (float) (System.currentTimeMillis() - this.eyemine$firstRenderTime) / 1000.0F : 1.0F;
	}

	@Inject(at = @At("TAIL"), method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V")
	public void eyemineTitleTailRender(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		float f1 = this.eyemine$showFadeInAnimation ? Mth.clamp(eyemine$animationTime - 1.0F, 0.0F, 1.0F) : 1.0F;
		int l = Mth.ceil(f1 * 255.0F) << 24;

		String subtitle = "EyeMine Edition";
		if ((l & -67108864) != 0) {
			Matrix3x2fStack pose = guiGraphics.pose();
			pose.pushMatrix();
			pose.translate((float) (this.width / 2), 25.0F);
			float f2 = 1.5f;
			pose.scale(f2, f2);
			guiGraphics.centeredText(this.font, subtitle, 0, -8, 16776960 | l);
			pose.popMatrix();
		}
	}
}

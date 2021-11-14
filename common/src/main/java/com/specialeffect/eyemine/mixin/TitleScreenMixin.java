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

package com.specialeffect.eyemine.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {

	private final boolean showFadeInAnimation = false;
	private long firstRenderTime;
	private float animationTime;

	protected TitleScreenMixin(BaseComponent component) {
		super(component);
	}

	@Inject(at = @At("HEAD"), method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V")
	public void eyemineTitleHeadRender(PoseStack poseStack, int p_render_1_, int p_render_2_, float p_render_3_, CallbackInfo ci) {
		if (this.firstRenderTime == 0L && this.showFadeInAnimation) {
			this.firstRenderTime = Util.getMillis();
		}

		animationTime = this.showFadeInAnimation ? (float)(Util.getMillis() - this.firstRenderTime) / 1000.0F : 1.0F;
	}

	@Inject(at = @At("TAIL"), method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V")
	public void eyemineTitleTailRender(PoseStack poseStack, int p_render_1_, int p_render_2_, float p_render_3_, CallbackInfo ci) {
		float f1 = this.showFadeInAnimation ? Mth.clamp(animationTime - 1.0F, 0.0F, 1.0F) : 1.0F;
		int l = Mth.ceil(f1 * 255.0F) << 24;

		String subtitle = "EyeMine Edition";
		if ((l & -67108864) != 0) {
			poseStack.pushPose();
			poseStack.translate((float)(this.width / 2), 25.0F, 0.0F);
			float f2 = 1.5f;
			poseStack.scale(f2, f2, f2);
			drawCenteredString(poseStack, this.font, subtitle, 0, -8, 16776960 | l);
			poseStack.popPose();
		}
	}
}

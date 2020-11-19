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

import com.mojang.blaze3d.platform.GlStateManager;
import javax.annotation.Nullable;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CustomMainMenu extends MainMenuScreen {

   @Nullable
   private final boolean showFadeInAnimation;
   private long firstRenderTime;
   
   public CustomMainMenu() {
      this(false);
   }

   public CustomMainMenu(boolean fadeIn) {
	  super(fadeIn);
      this.showFadeInAnimation = fadeIn;
   }
   
   protected void init() {
      super.init();
   }

   public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
	   
      if (this.firstRenderTime == 0L && this.showFadeInAnimation) {
         this.firstRenderTime = Util.milliTime();
      }

      float f = this.showFadeInAnimation ? (float)(Util.milliTime() - this.firstRenderTime) / 1000.0F : 1.0F;
      
      super.render(p_render_1_, p_render_2_, p_render_3_);
      
      float f1 = this.showFadeInAnimation ? MathHelper.clamp(f - 1.0F, 0.0F, 1.0F) : 1.0F;
      int l = MathHelper.ceil(f1 * 255.0F) << 24;
      
      String subtitle = "EyeMine Edition";
      if ((l & -67108864) != 0) {  
        GlStateManager.pushMatrix();
        GlStateManager.translatef((float)(this.width / 2), 25.0F, 0.0F);
        float f2 = 1.5f;
        GlStateManager.scalef(f2, f2, f2);
        this.drawCenteredString(this.font, subtitle, 0, -8, 16776960 | l);
        GlStateManager.popMatrix();
  }      
   }
}
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

package com.specialeffect.mods.misc;

import com.specialeffect.mods.ChildMod;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;


public class NightVisionHelper extends ChildMod {
	public static final String MODID = "nightvisionhelper";
	public static final String NAME = "NightVisionHelper";

	private int mDarkTicksAccum = 0;
	private int mShowMessageTicksAccum = 0;
	private int mTicksLoaded = 0;
	
	private static boolean mShowMessage;
	private static boolean mDisabled;
	private static boolean mTemporarilyDisabled;
	
	private float mLightnessThreshold;
	private int mTicksThreshold;
	
	public static void cancelAndHide() {
		mDisabled = true;
		mShowMessage = false;
	}
	
	public void setup(final FMLCommonSetupEvent event) {

	}
	

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
		
		this.resetState();
		
		// If player has overridden brightness beyond what's allowed in the settings menu, 
		// (i.e. by hacking options.txt) then they are a power user and we'll leave them to it			
		if (Minecraft.getInstance().gameSettings.gamma > 1.01f) {
			NightVisionHelper.cancelAndHide();
		}
	}	
    
    private void resetState() {
    	// Reset state
		mDisabled = false;
		mShowMessage = false;
		mDarkTicksAccum = 0;
		mShowMessageTicksAccum = 0;	
		mTicksLoaded = 0;
		mLightnessThreshold = 0.2f;
		mTicksThreshold = 2*20;
    }
    
    @SubscribeEvent 
    public void onDeath(LivingDeathEvent event) {
		if (ModUtils.entityIsMe(event.getEntityLiving())) {
			this.resetState();
			mTemporarilyDisabled = true;			
		}
    }

    @SubscribeEvent 
    public void onSpawn(LivingSpawnEvent event) {
		if (ModUtils.entityIsMe(event.getEntityLiving())) {
			this.resetState();
			mTemporarilyDisabled = false;
		}
    }

	@SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
		
		// Don't apply logic while in loading screen / other UIs
		if (Minecraft.getInstance().currentScreen != null) {
			return;
		}
		
    	PlayerEntity player = Minecraft.getInstance().player;    	
    	if (null != player && event.phase == TickEvent.Phase.START) {

    		if (!player.isCreative()) {
				// We won't worry about survival players, they know what they're doing
				return;
			}
			
			World world = Minecraft.getInstance().world;
			
			// We'll reduce (make stricter) the threshold for showing a warning message once the world
			// has been loaded a while. We're mainly trying to catch the "reloaded into pitch black and
			// don't know what's going on" failure mode.
			if (mTicksLoaded > 20*20) {
				mLightnessThreshold = 0.13f;
				mTicksThreshold = 10*20;
			}
			else {
				mTicksLoaded++;
			}
	        
			// If message is visible, keep alive for minimum time
			if (mShowMessage) {
				mShowMessageTicksAccum++;
				if (mDisabled && mShowMessageTicksAccum > 5*20) {
					mShowMessage = false;
				}				
			}
			
			if (mDisabled || mTemporarilyDisabled) {
				return;
			}			
	        	                  		            
	        BlockRayTraceResult result = ModUtils.getMouseOverBlock();
	        if (result != null) {
	        	
	        	// Get lightness of block(s) we're looking at	            	
	        	BlockPos pos = result.getPos().offset(result.getFace());	            		            			          
	            float lightnessBlock = world.getBrightness(pos);		   
	            
	            // Get lightness where player is
	            BlockPos posPlayer = player.getPosition();
	            float lightnessPlayer = world.getBrightness(posPlayer);		   
	            
	            float lightness = Math.max(lightnessBlock, lightnessPlayer);
	            
	            // If it's really dark, put message up to remind of night vision
	            
	            float gamma = (float) Minecraft.getInstance().gameSettings.gamma;
	            float threshold = mLightnessThreshold - gamma/10f; // i.e. 0.03f for max brightness
	            if (lightness < threshold) {
	            	mDarkTicksAccum++;		            		              		          
	            }
	            else if (mDarkTicksAccum > 0) {
	            	mDarkTicksAccum--;	           
	            }			           
	            
	            if (mDarkTicksAccum >= mTicksThreshold) { 
	            	mShowMessage = true;
	            }	            
	        }		
    	}
	}
	

	@SubscribeEvent
	public void onRenderExperienceBar(RenderGameOverlayEvent event)
	{		
		if (mShowMessage) {
			Minecraft mc = Minecraft.getInstance();
			
			int w = mc.mainWindow.getScaledWidth();
			int h = mc.mainWindow.getScaledHeight();
			
			String msg1 = "You are in the dark!";
			String msg2 = "To turn on night vision, use the EyeMine keyboard or press F12";
			String msg3 = "To reset to start location, press Home";
						
			FontRenderer font = mc.fontRenderer;
			
			int y = h/5;
			drawCenteredString(font, msg1, w/2,      y, 0xffffff);
			drawCenteredString(font, msg2, w/2, y + 20, 0xffffff);
			drawCenteredString(font, msg3, w/2, y + 40, 0xffffff);
		}
	}
	
	private void drawCenteredString(FontRenderer font, String msg, int x, int y, int c) {
		int stringWidth = font.getStringWidth(msg);		
        font.drawStringWithShadow(msg, x - stringWidth/2, y, c);        
	}

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {	
		// Any key dismisses the message (eventually, after minimum time)
		if (mShowMessage) {
			mDisabled = true;
		}
	}
	
}

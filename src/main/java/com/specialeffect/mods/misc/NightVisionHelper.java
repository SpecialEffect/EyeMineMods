/**
 * Copyright (C) 2016 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.mods.misc;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.callbacks.DelayedOnLivingCallback;
import com.specialeffect.callbacks.IOnLiving;
import com.specialeffect.mods.EyeGaze;
import com.specialeffect.utils.ModUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

@Mod(modid = NightVisionHelper.MODID, version = ModUtils.VERSION, name = NightVisionHelper.NAME)
public class NightVisionHelper extends BaseClassWithCallbacks {
	public static final String MODID = "specialeffect.nightvisionhelper";
	public static final String NAME = "NightVisionHelper";

	private int mDarkTicksAccum = 0;
	private int mShowMessageTicksAccum = 0;
	
	private static boolean mShowMessage;
	private static boolean mDisabled;
	
	private float mLightnessThreshold;
	private int mTicksThreshold;
	
	public static void cancelAndHide() {
		mDisabled = true;
		mShowMessage = false;
	}

	@EventHandler
	public void onWorldLoad(FMLServerStartedEvent event) {		
		
		// Reset state
		mDisabled = false;
		mShowMessage = false;
		mDarkTicksAccum = 0;
		mShowMessageTicksAccum = 0;		
		mLightnessThreshold = 0.2f;
		mTicksThreshold = 2*20;
		
		// If player has overridden brightness beyond what's allowed in the settings menu, 
		// (i.e. by hacking options.txt) then they are a power user and we'll leave them to it			
		if (Minecraft.getMinecraft().gameSettings.gammaSetting > 1.01f) {
			NightVisionHelper.cancelAndHide();
		}

		// We'll reduce (make stricter) the threshold for showing a warning message once the world
		// has been loaded a while. We're mainly trying to catch the "reloaded into pitch black and
		// don't know what's going on" failure mode.
		this.queueOnLivingCallback(new DelayedOnLivingCallback(new IOnLiving() {			
			@Override
			public void onLiving(LivingUpdateEvent event) {
				// TODO Auto-generated method stub
				mLightnessThreshold = 0.13f;
				mTicksThreshold = 10*20;
			}
		}, 20*20));
	}
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(this);

		ModUtils.setupModInfo(event, this.MODID, this.NAME, "Automatic warning if respawning in the dark");
		ModUtils.setAsParent(event, EyeGaze.MODID);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// Subscribe to event buses
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);		
	}


	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (ModUtils.entityIsMe(event.getEntityLiving())) {
			
			EntityPlayer player = (EntityPlayer)event.getEntityLiving();
			World world = Minecraft.getMinecraft().world;
			
			if (!player.isCreative()) {
				// We won't worry about survival players, they know what they're doing
				return;
			}
            
			// If message is visible, keep alive for minimum time
			if (mShowMessage) {
				mShowMessageTicksAccum++;
				if (mDisabled && mShowMessageTicksAccum > 20*20) {
					mShowMessage = false;
				}				
			}
			
			// If night vision is on, abort						
			if (player.isPotionActive(MobEffects.NIGHT_VISION)) {
				return;
			}
			
			if (mDisabled) {
				return;
			}
			
            if (player instanceof EntityPlayerMP) { // sometimes on local side, brightness flickers to zero, so we only look at server info
            	                  	
	            RayTraceResult result = Minecraft.getMinecraft().objectMouseOver;
	            
	            if (result != null && result.getBlockPos() != null) {
	            	
	            	// Get lightness of block(s) we're looking at
	            	BlockPos pos = result.getBlockPos().offset(result.sideHit);	            		            			          
		            float lightnessBlock = world.getLightBrightness(pos);		   
		            
		            // Get lightness where player is
		            BlockPos posPlayer = player.getPosition();
		            float lightnessPlayer = world.getLightBrightness(posPlayer);		   
		            
		            float lightness = Math.max(lightnessBlock, lightnessPlayer);
		            
		            // If it's really dark, put message up to remind of night vision
		            
		            float gamma = Minecraft.getMinecraft().gameSettings.gammaSetting;
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

			// Process any events which were queued by key events
			this.processQueuedCallbacks(event);
		}
	}
	

	// This event is called by GuiIngameForge during each frame by
	// GuiIngameForge.pre() and GuiIngameForce.post().
	@SubscribeEvent
	public void onRenderExperienceBar(RenderGameOverlayEvent event)
	{		
		if (mShowMessage) {
			Minecraft mc = Minecraft.getMinecraft();
			
			ScaledResolution res = new ScaledResolution(mc);						
			int w = res.getScaledWidth();
			int h = res.getScaledHeight();
			
			String msg1 = "You are in the dark!";
			String msg2 = "To turn on night vision, use the EyeMine keyboard or press F12";
			String msg3 = "To reset to start location, press Home";
						
			FontRenderer font = mc.fontRendererObj;
			
			int y = h/5;
			drawCenteredString(font, msg1, w/2,      y, 0xffffff);
			drawCenteredString(font, msg2, w/2, y + 20, 0xffffff);
			drawCenteredString(font, msg3, w/2, y + 40, 0xffffff);
		}
	}
	
	private void drawCenteredString(FontRenderer font, String msg, int x, int y, int c) {
		int stringWidth = font.getStringWidth(msg);		
        font.drawString(msg, x - stringWidth/2, y, c);        
	}
	
	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		// Any key dismisses the message (eventually, after minimum time)
		if (mShowMessage) {
			mDisabled = true;
		}
	}
	
}

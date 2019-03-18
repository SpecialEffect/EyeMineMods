/**
 * Copyright (C) 2016 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.mods.moving;

import org.lwjgl.input.Keyboard;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.callbacks.IOnLiving;
import com.specialeffect.callbacks.SingleShotOnLivingCallback;
import com.specialeffect.gui.StateOverlay;
import com.specialeffect.mods.EyeGaze;
import com.specialeffect.overrides.MovementInputFromOptionsOverride;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;


@Mod(modid = Sneak.MODID, version = ModUtils.VERSION, name = Sneak.NAME)

public class Sneak extends BaseClassWithCallbacks {

	public static final String MODID = "specialeffect.sneaktoggle";
	public static final String NAME = "SneakToggle";

	private static KeyBinding mSneakKB;
	private static boolean mIsSneaking = false;
	
	private static int mIconIndex;
	  
	private Minecraft mMinecraft;
	
	private MovementInputFromOptionsOverride mMovementOverride;
	
	@EventHandler
	@SuppressWarnings("static-access")
	public void preInit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);

		ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Add key binding to start/stop sneaking");
    	ModUtils.setAsParent(event, EyeGaze.MODID);

	}

	@EventHandler
	public void init(FMLInitializationEvent event) {

	    mMinecraft = Minecraft.getMinecraft();

		// Register key bindings
		mSneakKB = new KeyBinding("Start/stop sneaking", Keyboard.KEY_Z, CommonStrings.EYEGAZE_EXTRA);
		ClientRegistry.registerKeyBinding(mSneakKB);
		
		// Register an icon for the overlay
		mIconIndex = StateOverlay.registerTextureLeft("specialeffect:icons/sneak.png");
		
	}
	
	@SubscribeEvent
	public void onClientTick(ClientTickEvent event)
	{		
		if ((mMinecraft.player != null)) {

			if (null == mMovementOverride) {
				mMovementOverride = new MovementInputFromOptionsOverride(mMinecraft.gameSettings);				
			}			

			if (!(mMinecraft.player.movementInput instanceof MovementInputFromOptionsOverride))
			{
				mMinecraft.player.movementInput = mMovementOverride;	
			}
		}
	}
	
	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (ModUtils.entityIsMe(event.getEntityLiving())) {
			this.processQueuedCallbacks(event);
			
			// Make sure icon up to date
    		StateOverlay.setStateLeftIcon(mIconIndex, mIsSneaking);    		
		}
	}
	
	public static void stop() {
		mIsSneaking = false;
	}

	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
//		
		if(mSneakKB.isPressed()) {
			mIsSneaking = !mIsSneaking;
			if (mMovementOverride != null) 
			{
				mMovementOverride.setSneakOverride(mIsSneaking);
			}
			else{
				System.out.println("null handler");
				mIsSneaking = false;
			}

			this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving()
        	{				
				@Override
				public void onLiving(LivingUpdateEvent event) {
					EntityPlayer player = (EntityPlayer)event.getEntityLiving();

			        player.sendMessage(new TextComponentString(
			        		 "Sneaking: " + (mIsSneaking ? "ON" : "OFF")));
				}		
			}));
		}
	}

}

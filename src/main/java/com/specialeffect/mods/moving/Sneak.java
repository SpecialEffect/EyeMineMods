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

import org.lwjgl.glfw.GLFW;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.gui.StateOverlay;
import com.specialeffect.mods.EyeGaze;
import com.specialeffect.overrides.MovementInputFromOptionsOverride;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.event.TickEvent.ClientTickEvent;


public class Sneak  {

	public static final String MODID = "sneaktoggle";
	public static final String NAME = "SneakToggle";

	private static KeyBinding mSneakKB;
	private static boolean mIsSneaking = false;
	
	private static int mIconIndex;
	
	private Minecraft mMinecraft;
	
	private MovementInputFromOptionsOverride mMovementOverride;
		

	public Sneak() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
	}
	
	@SuppressWarnings("static-access")
	private void setup(final FMLCommonSetupEvent event) {
		MinecraftForge.EVENT_BUS.register(this);

		ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Add key binding to start/stop sneaking");
    	ModUtils.setAsParent(event, EyeGaze.MODID);

	    mMinecraft = Minecraft.getInstance();

		// Register key bindings
		mSneakKB = new KeyBinding("Start/stop sneaking", GLFW.GLFW_KEY_Z, CommonStrings.EYEGAZE_EXTRA);
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
//		if (ModUtils.entityIsMe(event.getEntityLiving())) {
//			this.processQueuedCallbacks(event);
//			
//			PlayerEntity player = (PlayerEntity)event.getEntityLiving();
//			player.setSneaking(mIsSneaking);
//			// Make sure icon up to date
//			StateOverlay.setStateLeftIcon(mIconIndex, mIsSneaking);    		
//		}
	}
	
	// FIXME: replace with IMC??
	public void stop() {		
		updateSneak(false);
	}	
	
	private void updateSneak(boolean bSneak) {
		mIsSneaking = bSneak;

		// TODO: is there any reason we don't want to just hold down the key here? 
		// this also helps with mod tooltips
		
		// FIXME: remove movement override stuff if not using for Sneak  

		final KeyBinding useItemKeyBinding = Minecraft.getInstance().gameSettings.keyBindSneak;			
		KeyBinding.setKeyBindState(useItemKeyBinding.getKey(), bSneak);			
		ModUtils.sendPlayerMessage("Sneaking: " + (bSneak ? "ON" : "OFF"));
		
		// Make sure icon up to date?
		StateOverlay.setStateLeftIcon(mIconIndex, mIsSneaking);    		
	}

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {
		if(mSneakKB.isPressed()) {			
			updateSneak(!mIsSneaking);
		}
	}

}

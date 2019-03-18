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
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;


@Mod(modid = Swim.MODID, version = ModUtils.VERSION, name = Swim.NAME)

public class Swim extends BaseClassWithCallbacks {

	public static final String MODID = "specialeffect.swimtoggle";
	public static final String NAME = "SwimToggle";

	private static KeyBinding mSwimKB;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);

		ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Add key binding to start/stop swimming (= jumping)");
    	ModUtils.setAsParent(event, EyeGaze.MODID);

	}

	@EventHandler
	public void init(FMLInitializationEvent event) {

		// Register key bindings
		mSwimKB = new KeyBinding("Start/stop swimming", Keyboard.KEY_V, CommonStrings.EYEGAZE_EXTRA);
		ClientRegistry.registerKeyBinding(mSwimKB);
		
		// Register an icon for the overlay
		mIconIndex = StateOverlay.registerTextureLeft("specialeffect:icons/swim.png");

		StateOverlay.setStateLeftIcon(mIconIndex, mSwimmingTurnedOn);

	}
	
	public static boolean isSwimmingOn() {
		return mSwimmingTurnedOn;
	}
	
	private int mIconIndex;
	private boolean mJumpKeyOverridden = false;

	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (ModUtils.entityIsMe(event.getEntityLiving())) {
			EntityPlayer player = (EntityPlayer)event.getEntityLiving();
			
			if (mSwimmingTurnedOn) {
				final KeyBinding swimBinding = 
						Minecraft.getMinecraft().gameSettings.keyBindJump;

				// Switch on swim key when in water
				if (player.isInWater() && 						
						!swimBinding.isKeyDown() ) {
					KeyBinding.setKeyBindState(swimBinding.getKeyCode(), true);			
					mJumpKeyOverridden = true;
				}
				
				// Switch off when on land or if flying
				else if ((player.onGround || player.capabilities.isFlying) &&
						  swimBinding.isKeyDown()) {

					if (mJumpKeyOverridden) {
						KeyBinding.setKeyBindState(swimBinding.getKeyCode(), false);
						mJumpKeyOverridden = false;
					}
				}
			}
			this.processQueuedCallbacks(event);
			
		}
	}
	
	private static boolean mSwimmingTurnedOn = true;

	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		if(mSwimKB.isPressed()) {
			final KeyBinding swimBinding = 
					Minecraft.getMinecraft().gameSettings.keyBindJump;
			
			mSwimmingTurnedOn = !mSwimmingTurnedOn;

			StateOverlay.setStateLeftIcon(mIconIndex, mSwimmingTurnedOn);
			
			if (!mSwimmingTurnedOn) {
				KeyBinding.setKeyBindState(swimBinding.getKeyCode(), false);
			}
			this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving()
        	{				
				@Override
				public void onLiving(LivingUpdateEvent event) {
					EntityPlayer player = (EntityPlayer)event.getEntityLiving();
			        player.sendMessage(new TextComponentString(
			        		 "Swimming: " + (mSwimmingTurnedOn? "ON" : "OFF")));
				}		
			}));
		}
	}

}

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
import com.specialeffect.callbacks.IOnLiving;
import com.specialeffect.callbacks.SingleShotOnLivingCallback;
import com.specialeffect.gui.StateOverlay;
import com.specialeffect.mods.ChildMod;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;


public class Swim extends BaseClassWithCallbacks implements ChildMod {

	public static final String MODID = "swimtoggle";
	public static final String NAME = "SwimToggle";

	private static KeyBinding mSwimKB;

	public Swim() {
	}
	
	public void setup(final FMLCommonSetupEvent event) {
		// Register key bindings
		mSwimKB = new KeyBinding("Start/stop swimming", GLFW.GLFW_KEY_V, CommonStrings.EYEGAZE_EXTRA);
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
			PlayerEntity player = (PlayerEntity)event.getEntityLiving();
			
			if (mSwimmingTurnedOn) {
				final KeyBinding swimBinding = 
						Minecraft.getInstance().gameSettings.keyBindJump;

				// Switch on swim key when in water
				if (player.isInWater() && 						
						!swimBinding.isKeyDown() ) {
					KeyBinding.setKeyBindState(swimBinding.getKey(), true);			
					mJumpKeyOverridden = true;
				}
				
				// Switch off when on land or if flying
				else if ((player.onGround || player.isAirBorne) && // TESTME isAirborne?
						  swimBinding.isKeyDown()) {

					if (mJumpKeyOverridden) {
						KeyBinding.setKeyBindState(swimBinding.getKey(), false);
						mJumpKeyOverridden = false;
					}
				}
			}
			this.processQueuedCallbacks(event);
			
		}
	}
	
	private static boolean mSwimmingTurnedOn = true;

	@SubscribeEvent
    public void onClientTickEvent(final ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
		if(mSwimKB.isPressed()) {
			final KeyBinding swimBinding = 
					Minecraft.getInstance().gameSettings.keyBindJump;
			
			mSwimmingTurnedOn = !mSwimmingTurnedOn;

			StateOverlay.setStateLeftIcon(mIconIndex, mSwimmingTurnedOn);
			
			if (!mSwimmingTurnedOn) {
				KeyBinding.setKeyBindState(swimBinding.getKey(), false);
			}
			this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving()
        	{				
				@Override
				public void onLiving(LivingUpdateEvent event) {
					PlayerEntity player = (PlayerEntity)event.getEntityLiving();
			        player.sendMessage(new StringTextComponent(
			        		 "Swimming: " + (mSwimmingTurnedOn? "ON" : "OFF")));
				}		
			}));
		}
	}

}

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

import com.specialeffect.gui.StateOverlay;
import com.specialeffect.mods.ChildMod;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;


public class Swim extends ChildMod {

	public static final String MODID = "swimtoggle";
	public static final String NAME = "SwimToggle";

	private static KeyBinding mSwimKB;
	private static boolean mSwimmingTurnedOn = true;

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
	
	public static void stopActivelySwimming() {
		final KeyBinding swimBinding = 
				Minecraft.getInstance().gameSettings.keyBindJump;
		KeyBinding.setKeyBindState(swimBinding.getKey(), false);	
		jumpkeyTimer = jumpkeyCooldown; 
	}
	
	public static boolean isSwimmingOn() {
		return mSwimmingTurnedOn;
	}
	
	private int mIconIndex;
	private boolean mJumpKeyOverridden = false;
	
	private static int jumpkeyTimer = 0;
	private static int jumpkeyCooldown = 500;
	
	private boolean isPlayerInAir(PlayerEntity player) {
		World world = Minecraft.getInstance().world;
		BlockPos playerPos = player.getPosition();
		return world.getBlockState(playerPos).isAir();			
	}
	
	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {
		PlayerEntity player = Minecraft.getInstance().player;
    	if (null != player && event.phase == TickEvent.Phase.START) {
			if (jumpkeyTimer > 0) {
	    		System.out.println(jumpkeyTimer);
				jumpkeyTimer -= 1;
			}
			jumpkeyCooldown = 6;
			
			if (mSwimmingTurnedOn) {
				final KeyBinding swimBinding = 
						Minecraft.getInstance().gameSettings.keyBindJump;

				// Switch on swim key when in water
				if (player.isInWater() && 						
						!swimBinding.isKeyDown() &&
						jumpkeyTimer == 0) {
					KeyBinding.setKeyBindState(swimBinding.getKey(), true);			
					mJumpKeyOverridden = true;
					System.out.println("on");
				}
				
				// Switch off when on land
				else if ((player.onGround || isPlayerInAir(player)) &&
						  swimBinding.isKeyDown()) {

					if (mJumpKeyOverridden) {
						System.out.println("off");
						KeyBinding.setKeyBindState(swimBinding.getKey(), false);
						mJumpKeyOverridden = false;
						// don't turn back on until timer finished - otherwise we can trigger 'fly'.
						jumpkeyTimer = jumpkeyCooldown;
					}
				}
			}
			
			
		}
	}

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {   
		if (ModUtils.hasActiveGui()) { return; }       	
		
		if(mSwimKB.isPressed()) {
			final KeyBinding swimBinding = 
					Minecraft.getInstance().gameSettings.keyBindJump;
			
			mSwimmingTurnedOn = !mSwimmingTurnedOn;

			StateOverlay.setStateLeftIcon(mIconIndex, mSwimmingTurnedOn);
			
			if (!mSwimmingTurnedOn) {
				KeyBinding.setKeyBindState(swimBinding.getKey(), false);
			}
			
			ModUtils.sendPlayerMessage("Swimming: " + (mSwimmingTurnedOn? "ON" : "OFF"));				
		}
	}

}

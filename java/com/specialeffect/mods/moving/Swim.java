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

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
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
		FMLCommonHandler.instance().bus().register(this);

		ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Add key binding to start/stop swimming (= jumping)");
    	ModUtils.setAsParent(event, EyeGaze.MODID);

	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// Subscribe to event buses
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);

		// Register key bindings
		mSwimKB = new KeyBinding("Start/stop swimming", Keyboard.KEY_V, CommonStrings.EYEGAZE_EXTRA);
		ClientRegistry.registerKeyBinding(mSwimKB);
		
		// Register an icon for the overlay
		mIconIndex = StateOverlay.registerTextureLeft("specialeffect:icons/swim.png");

		StateOverlay.setStateLeftIcon(mIconIndex, mSwimmingTurnedOn);

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

				// Only hold down the swim button when actually in water.
				if (player.isInWater() && 						
						!swimBinding.isKeyDown() ) {
					KeyBinding.setKeyBindState(swimBinding.getKeyCode(), true);			
					mJumpKeyOverridden = true;
				}
				// Switch off when on land
				else if (!player.isInWater() &&
						swimBinding.isKeyDown() ) {
					// If water is underneath, don't stop swimming yet (probably in the
					// process of swimming).
					BlockPos playerPos = player.getPosition();
					BlockPos blockBelow = new BlockPos(playerPos.getX(),
							playerPos.getY()-1, playerPos.getZ());
			    	World world = Minecraft.getMinecraft().world;
					Block block = world.getBlockState(blockBelow).getBlock();
					if (block != null && block instanceof BlockLiquid) {
						// do nothing
					}
					else {
						if (mJumpKeyOverridden) {
							KeyBinding.setKeyBindState(swimBinding.getKeyCode(), false);
							mJumpKeyOverridden = false;
						}
					}
				}
			}
			this.processQueuedCallbacks(event);
			
		}
	}
	
	private boolean mSwimmingTurnedOn = true;

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

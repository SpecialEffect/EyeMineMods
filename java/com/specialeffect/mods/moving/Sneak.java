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
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
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


@Mod(modid = Sneak.MODID, version = ModUtils.VERSION, name = Sneak.NAME)

public class Sneak extends BaseClassWithCallbacks {

	public static final String MODID = "specialeffect.sneaktoggle";
	public static final String NAME = "SneakToggle";

	private static KeyBinding mSneakKB;
	private static KeyBinding mMCSneakBinding;
	private boolean mIsSneaking;
	
	private static int mIconIndex;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(this);

		ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Add key binding to start/stop sneaking");
    	ModUtils.setAsParent(event, SpecialEffectMovements.MODID);

	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// Subscribe to event buses
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);

		// Register key bindings
		mSneakKB = new KeyBinding("Toggle sneak", Keyboard.KEY_Z, "SpecialEffect");
		ClientRegistry.registerKeyBinding(mSneakKB);
		
		// Register an icon for the overlay
		mIconIndex = StateOverlay.registerTextureLeft("specialeffect:icons/sneak.png");
		
		// Query sneak key binding
		mMCSneakBinding = Minecraft.getMinecraft().gameSettings.keyBindSneak;

	}
	
	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (ModUtils.entityIsMe(event.getEntityLiving())) {
			this.processQueuedCallbacks(event);
			
			// Make sure icon up to date
    		StateOverlay.setStateLeftIcon(mIconIndex, mMCSneakBinding.isKeyDown());
		}
	}
	
	public static void stop() {
		KeyBinding.setKeyBindState(mMCSneakBinding.getKeyCode(), false);
	}

	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		if(mSneakKB.isPressed()) {
			if (mMCSneakBinding.isKeyDown()) {
				KeyBinding.setKeyBindState(mMCSneakBinding.getKeyCode(), false);
			}
			else {
				KeyBinding.setKeyBindState(mMCSneakBinding.getKeyCode(), true);
			}

			this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving()
        	{				
				@Override
				public void onLiving(LivingUpdateEvent event) {
					EntityPlayer player = (EntityPlayer)event.getEntityLiving();
			        player.addChatComponentMessage(new ChatComponentText(
			        		 "Sneaking: " + (mMCSneakBinding.isKeyDown() ? "ON" : "OFF")));
				}		
			}));
		}
	}

}

/**
 * Copyright (C) 2016 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.mods.mining;

import org.lwjgl.glfw.GLFW;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.gui.StateOverlay;
import com.specialeffect.mods.EyeGaze;
import com.specialeffect.mods.misc.ContinuouslyAttack;
import com.specialeffect.mods.mousehandling.MouseHandler;
import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.java.games.input.Keyboard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Hand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ContinuouslyMine.MODID)
public class ContinuouslyMine 
extends BaseClassWithCallbacks 
implements ChildModWithConfig 
{
	public static final String MODID = "specialeffect.continuouslydestroy";
	public static final String NAME = "ContinuouslyDestroy";
	private static int mIconIndex;
	private static KeyBinding mDestroyKB;
	private boolean mAutoSelectTool = true;
	private boolean mWaitingForPickaxe = false;

	public ContinuouslyMine() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
	}
	
	private void setup(final FMLCommonSetupEvent event) {
		// preinit
		MinecraftForge.EVENT_BUS.register(this);
		
		ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Add key binding to start/stop continuously attacking.");
		ModUtils.setAsParent(event, EyeGaze.MODID);

		//init 
		// Register for config changes from parent
		EyeGaze.registerForConfigUpdates((ChildModWithConfig)this);

		// Register key bindings	
		mDestroyKB = new KeyBinding("Start/stop mining", GLFW.GLFW_KEY_M, CommonStrings.EYEGAZE_COMMON);
		ClientRegistry.registerKeyBinding(mDestroyKB);
		
		// Register an icon for the overlay
		mIconIndex = StateOverlay.registerTextureRight("specialeffect:icons/mine.png");
	}
	
	public void syncConfig() {
		mAutoSelectTool = EyeGaze.mAutoSelectTool;
	}
	
	public static void stop() {
		mIsAttacking = false;
		final KeyBinding attackBinding = 
				Minecraft.getInstance().gameSettings.keyBindAttack;
		
		KeyBinding.setKeyBindState(attackBinding.getKey(), mIsAttacking);
		
		StateOverlay.setStateRightIcon(mIconIndex, false);
	}
	
	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (ModUtils.entityIsMe(event.getEntityLiving())) {
			
			final KeyBinding attackBinding = 
					Minecraft.getInstance().gameSettings.keyBindAttack;
			
			PlayerEntity player = (PlayerEntity) event.getEntityLiving();
			
			if (mIsAttacking) {
				// always select tool - first time we might need to ask server to
				// create a new one				
				if (player.isCreative() && 
						mAutoSelectTool) {
	    			boolean havePickaxe = MineOne.choosePickaxe(player.inventory);
	    			if (havePickaxe) {
	    				mWaitingForPickaxe = false;
	    			}
	    			else if(!mWaitingForPickaxe) 
	    			{
	    				MineOne.requestCreatePickaxe();
		    			mWaitingForPickaxe = true;		    		
	    			}
	    		}
				
				// Set mouse in correct state - shouldn't attack unless there's an
				// accompanying mouse movement.	
				if (MouseHandler.hasPendingEvent() || mMouseEventLastTick) {
					KeyBinding.setKeyBindState(attackBinding.getKey(), true);
				}
				else {
					KeyBinding.setKeyBindState(attackBinding.getKey(), false);
				}
			}
			
			// When attacking programmatically, the player doesn't swing unless
			// an attackable-block is in reach. We fix that here.
			if (attackBinding.isKeyDown()) {
				player.swingArm(Hand.MAIN_HAND);				
			}
			
			// Remember mouse status so we can have one tick of grace
			// (necessary if minecraft running faster than eye tracker).
			mMouseEventLastTick = MouseHandler.hasPendingEvent();
			
			this.processQueuedCallbacks(event);
		}
	}
	
	private static boolean mIsAttacking = false;
	private boolean mMouseEventLastTick = false;
	
	@SubscribeEvent
    public void onClientTickEvent(final ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        if(mDestroyKB.isPressed()) {
			
			mIsAttacking = !mIsAttacking;
			StateOverlay.setStateRightIcon(mIconIndex, mIsAttacking);

			final KeyBinding attackBinding = 
					Minecraft.getInstance().gameSettings.keyBindAttack;
			
			if (mIsAttacking) {
				KeyBinding.setKeyBindState(attackBinding.getKey(), true);
			}
			else {
				KeyBinding.setKeyBindState(attackBinding.getKey(), false);
			}
			
			// Don't allow mining *and* attacking at same time
			ContinuouslyAttack.stop();
		}
	}
}

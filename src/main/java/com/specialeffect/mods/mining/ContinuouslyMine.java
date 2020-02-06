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
import com.specialeffect.messages.AddItemToHotbar;
import com.specialeffect.mods.ChildMod;
import com.specialeffect.mods.EyeMineConfig;
import com.specialeffect.mods.mousehandling.MouseHandler;
import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class ContinuouslyMine 
extends BaseClassWithCallbacks 
implements ChildMod, ChildModWithConfig
{
	public static final String MODID = "continuouslydestroy";
	public static final String NAME = "ContinuouslyDestroy";
	private static final String PROTOCOL_VERSION = Integer.toString(1);

    public static SimpleChannel channel;
    
	private static int mIconIndex;
	private static KeyBinding mDestroyKB;
	private boolean mAutoSelectTool = true;
	private boolean mWaitingForPickaxe = false;

	public void setup(final FMLCommonSetupEvent event) {

		// setup channel for comms
		channel = NetworkRegistry.newSimpleChannel(
                new ResourceLocation("specialeffect","mineone")
                ,() -> PROTOCOL_VERSION
                , PROTOCOL_VERSION::equals
                , PROTOCOL_VERSION::equals);
        int id = 0;
        
        channel.registerMessage(id++, AddItemToHotbar.class, AddItemToHotbar::encode, 
        		AddItemToHotbar::decode, AddItemToHotbar.Handler::handle);        
        
		// Register key bindings	
		mDestroyKB = new KeyBinding("Start/stop mining", GLFW.GLFW_KEY_M, CommonStrings.EYEGAZE_COMMON);
		ClientRegistry.registerKeyBinding(mDestroyKB);
		
		// Register an icon for the overlay
		mIconIndex = StateOverlay.registerTextureRight("specialeffect:icons/mine.png");
	}
	
	public void syncConfig() {
		mAutoSelectTool = EyeMineConfig.mAutoSelectTool.get();
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
	    			boolean havePickaxe = choosePickaxe(player.inventory);
	    			if (havePickaxe) {
	    				mWaitingForPickaxe = false;
	    			}
	    			else if(!mWaitingForPickaxe) 
	    			{
	    				requestCreatePickaxe();
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
			mMouseEventLastTick = true;////FIXME SOON MouseHandler.hasPendingEvent();
			
			this.processQueuedCallbacks(event);
		}
	}
	
	private static boolean mIsAttacking = false;
	private boolean mMouseEventLastTick = false;
	

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {     
		if (ModUtils.hasActiveGui()) { return; }

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
			//FIXME SOON ContinuouslyAttack.stop();
		}
	}
	

	// returns true if successful
	static boolean choosePickaxe(PlayerInventory inventory) {
		
		// In creative mode, we can either select a pickaxe from the hotbar 
		// or just rustle up a new one		
		if (inventory.getCurrentItem().getItem() instanceof PickaxeItem)
		{
			return true;
		}
		else
		{
			int pickaxeId = ModUtils.findItemInHotbar(inventory, PickaxeItem.class);
			if (pickaxeId > -1) {
				inventory.currentItem = pickaxeId;
				return true;
			}
			else {
				return false;
			}
		}		
	}
	
	static void requestCreatePickaxe() {
		// Ask server to put new item in hotbar
        channel.sendToServer(new AddItemToHotbar(new ItemStack(Items.DIAMOND_PICKAXE)));
	}	
}

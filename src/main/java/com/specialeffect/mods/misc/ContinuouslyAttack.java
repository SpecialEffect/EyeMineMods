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

import org.lwjgl.glfw.GLFW;

import com.specialeffect.messages.AddItemToHotbar;
import com.specialeffect.messages.AttackEntityMessage;
import com.specialeffect.gui.StateOverlay;
import com.specialeffect.mods.ChildMod;
import com.specialeffect.mods.mining.ContinuouslyMine;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class ContinuouslyAttack 
 
extends ChildMod {
	public final String MODID = "continuouslyattack";
	public static final String NAME = "ContinuouslyAttack";
	private static final String PROTOCOL_VERSION = Integer.toString(1);

    public static SimpleChannel channel;
    
	private boolean mAutoSelectSword = true;
	private static int mIconIndex;
	private static KeyBinding mAttackKB;
	
	private boolean mWaitingForSword = false;
	
	public void setup(final FMLCommonSetupEvent event) {
		
		// FIXME: now in eyegaze mod, check working okay // FMLJavaModLoadingContext.get().getModEventBus().register(this);

		// setup channel for comms
		channel = NetworkRegistry.newSimpleChannel(
                new ResourceLocation("specialeffect","continuouslyattack")
                ,() -> PROTOCOL_VERSION
                , PROTOCOL_VERSION::equals
                , PROTOCOL_VERSION::equals);
        int id = 0;
        
        channel.registerMessage(id++, AttackEntityMessage.class, AttackEntityMessage::encode, 
        		AttackEntityMessage::decode, AttackEntityMessage.Handler::handle);        

        channel.registerMessage(id++, AddItemToHotbar.class, AddItemToHotbar::encode, 
        		AddItemToHotbar::decode, AddItemToHotbar.Handler::handle);        
        
		// Register key bindings	
		mAttackKB = new KeyBinding("Start/stop attacking", GLFW.GLFW_KEY_R, CommonStrings.EYEGAZE_COMMON);
		ClientRegistry.registerKeyBinding(mAttackKB);
		
		// Register an icon for the overlay
		mIconIndex = StateOverlay.registerTextureRight("specialeffect:icons/attack.png");
		
	}
	
	
	public static void stop() {
		mIsAttacking = false;
		StateOverlay.setStateRightIcon(mIconIndex, false);
	}
	
	
	
	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {
		PlayerEntity player = Minecraft.getInstance().player;
    	if (null != player && event.phase == TickEvent.Phase.START) {
			    		
			if (mIsAttacking) {
				if (player.isCreative() && 
						mAutoSelectSword) {
	    			boolean haveSword = chooseWeapon(player.inventory);
	    			if (haveSword) {
	    				mWaitingForSword = false;
	    			}
	    			else if(!mWaitingForSword) 
	    			{
	    				requestCreateSword();
		    			mWaitingForSword = true;		    		
	    			}
	    		}
				
				// Get entity being looked at
				EntityRayTraceResult entityResult = ModUtils.getMouseOverEntity();
				boolean recharging = false;
				if (null != entityResult) {
					Entity entity = entityResult.getEntity();
					
					// Attack locally and on server
					if (player.getCooledAttackStrength(0) > 0.95) {
						player.attackTargetEntityWithCurrentItem(entity);
						channel.sendToServer(new AttackEntityMessage(entity));
					}
					else {
						recharging = true;
					}
				}				
			
				// When attacking programmatically, the player doesn't swing unless
				// an attackable-block is in reach. We fix that here, for better feedback.
				if (!player.isSwingInProgress && !recharging) {
					player.swingArm(Hand.MAIN_HAND);
				}
			}
			
			
		}
	}
	
	public static boolean mIsAttacking = false;

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {     
		if (ModUtils.hasActiveGui()) { return; }
		
        if(mAttackKB.isPressed()) {
			mIsAttacking = !mIsAttacking;
			StateOverlay.setStateRightIcon(mIconIndex, mIsAttacking);
			
			// Don't allow mining *and* attacking at same time
			ContinuouslyMine.stop();
		}
	}
	
	/*public void onRenderGameOverlayEvent(final RenderGameOverlayEvent.Post event) {
		if (event.isCancelable() && event.isCanceled()) {
            return;
        }
		
		//TODO: check event type? HOTBAR CROSSHAIRS BOSSHEALTH EXPERIENCE TEXT POTION_ICONS SUBTITLES CHAT ALL VIGNETTE HELMET PORTAL
		if (event.getType() == ElementType.HOTBAR) {
			Minecraft mc = Minecraft.getInstance();
			mc.fontRenderer.drawString("Some HUD text", 10, 10, 0xffffff);
		}
	}*/	
		
	//returns true if successful
	private boolean chooseWeapon(PlayerInventory inventory) {
		
		// In creative mode, we can either select a sword from the hotbar 
		// or just rustle up a new one
		if (inventory.getCurrentItem().getItem() instanceof SwordItem)
		{
			return true;
		}
		else
		{
			int swordId = ModUtils.findItemInHotbar(inventory, SwordItem.class);
			if (swordId > -1) {
				inventory.currentItem = swordId;
				return true;
			}
			else {
				return false;
			}
		}		
	}	
	
	private void requestCreateSword() {
		// Ask server to put new item in hotbar
		channel.sendToServer(new AddItemToHotbar(new ItemStack(Items.DIAMOND_SWORD)));
	}
}

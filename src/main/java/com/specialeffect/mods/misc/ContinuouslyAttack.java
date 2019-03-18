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

import org.lwjgl.input.Keyboard;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.gui.StateOverlay;
import com.specialeffect.messages.AddItemToHotbar;
import com.specialeffect.messages.AttackEntityMessage;
import com.specialeffect.mods.mining.ContinuouslyMine;
import com.specialeffect.mods.EyeGaze;
import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
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
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = ContinuouslyAttack.MODID, version = ModUtils.VERSION, name = ContinuouslyAttack.NAME)
public class ContinuouslyAttack 
extends BaseClassWithCallbacks 
implements ChildModWithConfig {
	public static final String MODID = "specialeffect.continuouslyattack";
	public static final String NAME = "ContinuouslyAttack";
	public static SimpleNetworkWrapper network;
	private boolean mAutoSelectSword = true;
	private static int mIconIndex;
	private static KeyBinding mAttackKB;
	
	private boolean mWaitingForSword = false;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(this);
		
		ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Add key binding to start/stop continuously attacking.");
		ModUtils.setAsParent(event, EyeGaze.MODID);

		network = NetworkRegistry.INSTANCE.newSimpleChannel(this.NAME);
		network.registerMessage(AddItemToHotbar.Handler.class, AddItemToHotbar.class, 0, Side.SERVER);
		network.registerMessage(AttackEntityMessage.Handler.class, AttackEntityMessage.class, 1, Side.SERVER);

	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// Subscribe to event buses
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
		
		// Register for config changes from parent
		EyeGaze.registerForConfigUpdates((ChildModWithConfig)this);
		
		// Register key bindings	
		mAttackKB = new KeyBinding("Start/stop attacking", Keyboard.KEY_R, CommonStrings.EYEGAZE_COMMON);
		ClientRegistry.registerKeyBinding(mAttackKB);
		
		// Register an icon for the overlay
		mIconIndex = StateOverlay.registerTextureRight("specialeffect:icons/attack.png");
	}
	
	@Override
	public void syncConfig() {
		mAutoSelectSword = EyeGaze.mAutoSelectSword;	
	}
	
	
	public static void stop() {
		mIsAttacking = false;
		StateOverlay.setStateRightIcon(mIconIndex, false);
	}
	
	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (ModUtils.entityIsMe(event.getEntityLiving())) {
			EntityPlayer player = (EntityPlayer) event.getEntityLiving();

			if (mIsAttacking) {
				if (player.capabilities.isCreativeMode && 
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
				RayTraceResult mov = Minecraft.getMinecraft().objectMouseOver;
				Entity entity = mov.entityHit;
				if (null != entity) {
					// Attack locally and on server
					player.attackTargetEntityWithCurrentItem(entity);
					ContinuouslyAttack.network.sendToServer(new AttackEntityMessage(entity));
				}
			
				// When attacking programmatically, the player doesn't swing unless
				// an attackable-block is in reach. We fix that here, for better feedback.
				if (!player.isSwingInProgress) {
					player.swingArm(EnumHand.MAIN_HAND);
				}
			}
			
			this.processQueuedCallbacks(event);
		}
	}
	
	public static boolean mIsAttacking = false;
	
	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		
		if(mAttackKB.isPressed()) {
			mIsAttacking = !mIsAttacking;
			StateOverlay.setStateRightIcon(mIconIndex, mIsAttacking);
			
			// Don't allow mining *and* attacking at same time
			ContinuouslyMine.stop();
		}
	}
	
	//returns true if successful
	private boolean chooseWeapon(InventoryPlayer inventory) {
		
		// In creative mode, we can either select a sword from the hotbar 
		// or just rustle up a new one
		if (inventory.getCurrentItem().getItem() instanceof ItemSword)
		{
			return true;
		}
		else
		{
			int swordId = ModUtils.findItemInHotbar(inventory, ItemSword.class);
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
		ContinuouslyAttack.network.sendToServer(new AddItemToHotbar(new ItemStack(Items.DIAMOND_SWORD)));
	}
}

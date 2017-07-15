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

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

import org.lwjgl.input.Keyboard;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.callbacks.IOnLiving;
import com.specialeffect.callbacks.SingleShotOnLivingCallback;
import com.specialeffect.gui.StateOverlay;
import com.specialeffect.mods.mining.ContinuouslyMine;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
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

@Mod(modid = ContinuouslyAttack.MODID, version = ModUtils.VERSION, name = ContinuouslyAttack.NAME)
public class ContinuouslyAttack extends BaseClassWithCallbacks {
	public static final String MODID = "specialeffect.continuouslyattack";
	public static final String NAME = "ContinuouslyAttack";
    private Robot robot;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(this);
		
		ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Add key binding to start/stop continuously attacking.");
		ModUtils.setAsParent(event, SpecialEffectMisc.MODID);

	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// Subscribe to event buses
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
		
		// Register key bindings	
		mAttackKB = new KeyBinding("Attack", Keyboard.KEY_R, "SpecialEffect");
		ClientRegistry.registerKeyBinding(mAttackKB);
		
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
		
		// Register an icon for the overlay
		mIconIndex = StateOverlay.registerTextureRight("specialeffect:icons/attack.png");
	}
	
	private static int mIconIndex;
	private static KeyBinding mAttackKB;
	
	public static void stop() {
		mIsAttacking = false;
		StateOverlay.setStateRightIcon(mIconIndex, false);
	}
	
	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (ModUtils.entityIsMe(event.getEntityLiving())) {
			
			if (mIsAttacking) {
				// Get entity being looked at
				RayTraceResult mov = Minecraft.getMinecraft().objectMouseOver;
				Entity entity = mov.entityHit;
				if (null != entity) {
					// It feels like we should be able to just call 
					// player.attackTargetEntityWithCurrentItem but
					// it doesn't seem to work. 
					robot.mousePress(KeyEvent.BUTTON1_MASK);
					robot.mouseRelease(KeyEvent.BUTTON1_MASK);
				}
			}
			
			// When attacking programmatically, the player doesn't swing unless
			// an attackable-block is in reach. We fix that here, for better feedback.
			if (mIsAttacking) {
				EntityPlayer player = (EntityPlayer) event.getEntityLiving();
				player.swingArm(EnumHand.MAIN_HAND);
			}
			
			this.processQueuedCallbacks(event);
		}
	}
	
	private static boolean mIsAttacking = false;
	
	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		
		if(mAttackKB.isPressed()) {
			mIsAttacking = !mIsAttacking;
			StateOverlay.setStateRightIcon(mIconIndex, mIsAttacking);
			
			// Note: I'd like to use Minecraft.getMinecraft().gameSettings.keyBindAttack to
			// make this robust to key changes in the config. However, through minecraft key 
			// API you can only set key bind state, which misses the key press event you need
			// for attacking. So we use java.awt.Robot, which requires explicitly using a 
			// mouse event rather than a keyboard event. This is a shame.
			
//			if (mIsAttacking && !Mouse.isButtonDown(0)) {
//				robot.mousePress(KeyEvent.BUTTON1_MASK);
//			}
//			else if (Mouse.isButtonDown(0)) {
//				robot.mouseRelease(KeyEvent.BUTTON1_MASK);
//			}

			this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving()
        	{				
				@Override
				public void onLiving(LivingUpdateEvent event) {
					EntityPlayer player = (EntityPlayer)event.getEntityLiving();
			        player.sendMessage(new TextComponentString(
			        		 "Attacking: " + (mIsAttacking ? "ON" : "OFF")));
			        
			        if (player.capabilities.isCreativeMode) {
		    			chooseWeapon(player.inventory);
		    		}
		        }		
			}));
			
			// Don't allow mining *and* attacking at same time
			ContinuouslyMine.stop();
		}
	}
	
	private void chooseWeapon(InventoryPlayer inventory) {
		
		// In creative mode, we can either select a sword from the hotbar 
		// or just rustle up a new one

		int swordId = ModUtils.findItemInHotbar(inventory, ItemSword.class);
		if (swordId > -1) {
			inventory.currentItem = swordId;
		}
		else {
			ModUtils.moveItemToHotbarAndSelect(inventory, 
					new ItemStack(Items.DIAMOND_SWORD));	
		}
	}
}

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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.callbacks.IOnLiving;
import com.specialeffect.callbacks.SingleShotOnLivingCallback;
import com.specialeffect.gui.StateOverlay;
import com.specialeffect.mods.misc.ContinuouslyAttack;
import com.specialeffect.mods.moving.MouseHandler;
import com.specialeffect.utils.ModUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
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

@Mod(modid = ContinuouslyMine.MODID, version = ModUtils.VERSION, name = ContinuouslyMine.NAME)
public class ContinuouslyMine extends BaseClassWithCallbacks {
	public static final String MODID = "specialeffect.continuouslydestroy";
	public static final String NAME = "ContinuouslyDestroy";
	private static int mIconIndex;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(this);
		
		ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Add key binding to start/stop continuously attacking.");
		ModUtils.setAsParent(event, SpecialEffectMining.MODID);
		
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// Subscribe to event buses
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
		
		// Register key bindings	
		mDestroyKB = new KeyBinding("Mine", Keyboard.KEY_M, "SpecialEffect");
		ClientRegistry.registerKeyBinding(mDestroyKB);
		
		// Register an icon for the overlay
		mIconIndex = StateOverlay.registerTextureRight("specialeffect:icons/mine.png");
	}
	
	private static KeyBinding mDestroyKB;
	
	public static void stop() {
		mIsAttacking = false;
		final KeyBinding attackBinding = 
				Minecraft.getMinecraft().gameSettings.keyBindAttack;
		
		KeyBinding.setKeyBindState(attackBinding.getKeyCode(), mIsAttacking);
		
		StateOverlay.setStateRightIcon(mIconIndex, false);
	}
	
	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (ModUtils.entityIsMe(event.getEntityLiving())) {
			
			final KeyBinding attackBinding = 
					Minecraft.getMinecraft().gameSettings.keyBindAttack;
			
			// Set mouse in correct state - shouldn't attack unless there's an
			// accompanying mouse movement.
			if (mIsAttacking) {
				if (MouseHandler.hasPendingEvent() || mMouseEventLastTick) {
					KeyBinding.setKeyBindState(attackBinding.getKeyCode(), true);
				}
				else {
					KeyBinding.setKeyBindState(attackBinding.getKeyCode(), false);
				}
			}
			
			// When attacking programmatically, the player doesn't swing unless
			// an attackable-block is in reach. We fix that here.
			if (attackBinding.isKeyDown()) {
				event.getEntityLiving().swingItem();
			}
			
			// Remember mouse status so we can have one tick of grace
			// (necessary if minecraft running faster than eye tracker).
			mMouseEventLastTick =MouseHandler.hasPendingEvent();
			
			this.processQueuedCallbacks(event);
		}
	}
	
	private static boolean mIsAttacking = false;
	private boolean mMouseEventLastTick = false;
	
	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		if(mDestroyKB.isPressed()) {
			
			mIsAttacking = !mIsAttacking;
			StateOverlay.setStateRightIcon(mIconIndex, mIsAttacking);

			final KeyBinding attackBinding = 
					Minecraft.getMinecraft().gameSettings.keyBindAttack;
			
			if (mIsAttacking) {
				KeyBinding.setKeyBindState(attackBinding.getKeyCode(), true);
			}
			else {
				KeyBinding.setKeyBindState(attackBinding.getKeyCode(), false);
			}

			this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving()
        	{				
				@Override
				public void onLiving(LivingUpdateEvent event) {
					EntityPlayer player = (EntityPlayer)event.getEntityLiving();
			        player.addChatComponentMessage(new ChatComponentText(
			        		 "Mining: " + (mIsAttacking ? "ON" : "OFF")));
				}		
			}));
			
			// Don't allow mining *and* attacking at same time
			ContinuouslyAttack.stop();
		}
	}
}

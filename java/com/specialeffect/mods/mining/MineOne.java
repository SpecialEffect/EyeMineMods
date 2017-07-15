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

import org.lwjgl.input.Keyboard;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.callbacks.IOnLiving;
import com.specialeffect.callbacks.OnLivingCallback;
import com.specialeffect.callbacks.SingleShotOnLivingCallback;
import com.specialeffect.utils.ModUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
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

@Mod(modid = MineOne.MODID, version = ModUtils.VERSION, name = MineOne.NAME)
public class MineOne extends BaseClassWithCallbacks {
	public static final String MODID = "specialeffect.autodestroy";
	public static final String NAME = "AutoDestroy";

	private boolean mDestroying = false;
	private BlockPos mBlockToDestroy;
	
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
		mDestroyKB = new KeyBinding("Auto-Destroy", Keyboard.KEY_N, "SpecialEffect");
		ClientRegistry.registerKeyBinding(mDestroyKB);
	}
	
	private static KeyBinding mDestroyKB;
	
	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (ModUtils.entityIsMe(event.getEntityLiving())) {
			this.processQueuedCallbacks(event);

			if (mDestroying) {

				// Select the best tool from the inventory
				World world = Minecraft.getMinecraft().world;
	    		EntityPlayer player = (EntityPlayer)event.getEntityLiving();
					    						
	    		// Slightly different behaviour in survival vs creative:
	    		// CREATIVE: A pickaxe was chosen when mining began
	    		// SURVIVAL: Check selected item can actually destroy block
	    		// (but don't do the choosing, otherwise you'll waste good tools on easy blocks)
	    		if (!player.capabilities.isCreativeMode) {
	    			Block blockIn = world.getBlockState(mBlockToDestroy).getBlock();
	    			if (!ForgeHooks.canHarvestBlock(blockIn, player, world, mBlockToDestroy)) {
	    				String message = "Can't destroy this block with current item";
				        player.sendMessage(new TextComponentString(message));
	    				this.stopDestroying();
	    				return;
	    			}
	    		}
				
				// Stop attacking if we're not pointing at the block any more
				// (which means either we've destroyed it, or moved away)
				RayTraceResult mov = Minecraft.getMinecraft().objectMouseOver;
				boolean blockDestroyed = (world.getBlockState(mBlockToDestroy).getBlock() instanceof BlockAir);
				boolean movedAway =  false;		
				BlockPos pos = this.getMouseOverBlockPos();
				if (pos != null) {
					movedAway = mBlockToDestroy.distanceSq(pos.getX(), pos.getY(), pos.getZ()) > 0;
				}
				
				if (mov == null || blockDestroyed || movedAway) {
					this.stopDestroying();
				}
			}
		}
	}
	
	private void startDestroying() {
		mDestroying = true;
		
		final KeyBinding attackBinding = 
				Minecraft.getMinecraft().gameSettings.keyBindAttack;
		KeyBinding.setKeyBindState(attackBinding.getKeyCode(), true);
		
		// Select appropriate tool in creative
		this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving() {
			@Override
			public void onLiving(LivingUpdateEvent event) {
				System.out.println("onLiving queued event");
	    		EntityPlayer player = (EntityPlayer)event.getEntityLiving();
				if (player.capabilities.isCreativeMode) {
	    			choosePickaxe(player.inventory);
	    		}
			}
		}));
	}
	
	private void stopDestroying() {
		final KeyBinding attackBinding = 
				Minecraft.getMinecraft().gameSettings.keyBindAttack;
		KeyBinding.setKeyBindState(attackBinding.getKeyCode(), false);
		mDestroying = false;
	}
	
	// Return the position of the block that the mouse is pointing at.
	// May be null, if pointing at something other than a block.
	private BlockPos getMouseOverBlockPos() {
		BlockPos pos = null;
		RayTraceResult mov = Minecraft.getMinecraft().objectMouseOver;
		if (mov != null) {
			pos = mov.getBlockPos(); // may still be null if there's an entity there
		}
		return pos;
	}

	
	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		if(mDestroyKB.isPressed()) {
			mBlockToDestroy = this.getMouseOverBlockPos();
			if (mBlockToDestroy == null) {
				System.out.println("Nothing to attack");
				return;
			}
			else {
				this.startDestroying();
			}
		}
	}
	
	static void choosePickaxe(InventoryPlayer inventory) {
		
		// In creative mode, we can either select a pickaxe from the hotbar 
		// or just rustle up a new one

		int pickaxeId = ModUtils.findItemInHotbar(inventory, ItemPickaxe.class);
		if (pickaxeId > -1) {
			inventory.currentItem = pickaxeId;
		}
		else {
			ModUtils.moveItemToHotbarAndSelect(inventory, 
					new ItemStack(Items.DIAMOND_PICKAXE));			
		}
	}
}

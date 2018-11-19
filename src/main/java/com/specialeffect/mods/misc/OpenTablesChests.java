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
import com.specialeffect.callbacks.IOnLiving;
import com.specialeffect.callbacks.SingleShotOnLivingCallback;
import com.specialeffect.messages.ActivateBlockAtPosition;
import com.specialeffect.mods.EyeGaze;
import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
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

@Mod(modid = OpenTablesChests.MODID, 
version = ModUtils.VERSION,
name = OpenTablesChests.NAME)
public class OpenTablesChests 
extends BaseClassWithCallbacks
implements ChildModWithConfig
{

	public static final String MODID = "specialeffect.opentableschests";
	public static final String NAME = "OpenTablesChests";

    public static Configuration mConfig;
	private static KeyBinding mOpenChestKB;
	private static KeyBinding mOpenCraftingTableKB;	
	
    public static SimpleNetworkWrapper network;
    
    private static int mRadius = 5;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {    
		FMLCommonHandler.instance().bus().register(this);    	
		
		ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Add key bindings to open nearby chests/crafting tables.");
		ModUtils.setAsParent(event, EyeGaze.MODID);
		
        network = NetworkRegistry.INSTANCE.newSimpleChannel(this.NAME);
        network.registerMessage(ActivateBlockAtPosition.Handler.class, 
        						ActivateBlockAtPosition.class, 0, Side.SERVER);
        
		// Set up config
    	mConfig = new Configuration(event.getSuggestedConfigurationFile());
    	this.syncConfig();
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		// Subscribe to event buses
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);    	

		// Register for config changes from parent
		EyeGaze.registerForConfigUpdates((ChildModWithConfig)this);
				
		// Register key bindings	
		mOpenChestKB = new KeyBinding("Open chest", Keyboard.KEY_LBRACKET, CommonStrings.EYEGAZE_EXTRA);
		mOpenCraftingTableKB = new KeyBinding("Open crafting table", Keyboard.KEY_RBRACKET, CommonStrings.EYEGAZE_EXTRA);
		ClientRegistry.registerKeyBinding(mOpenChestKB);
		ClientRegistry.registerKeyBinding(mOpenCraftingTableKB);
	}

	public void syncConfig() {
        mRadius = EyeGaze.mRadiusChests;
	}

	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (ModUtils.entityIsMe(event.getEntityLiving())) {
			this.processQueuedCallbacks(event);
		}			
	}
	
	// Search for closest block of a certain class, within maximum radius
	private static BlockPos findClosestBlockOfType(String className, EntityPlayer player, World world, int radius) {
		BlockPos playerPos = player.getPosition();		
	    Class classType;
    	BlockPos closestBlockPos = null;

		try {
			classType = Class.forName(className);
			
	    	// Find closest chest (within radius)
	    	double closestDistanceSq = Double.MAX_VALUE;
	    	for (int x = -radius; x <= radius; x++) {
	    		for (int z = -radius; z <= radius; z++) {
	    			for (int y = -radius; y <= radius; y++) { 

	    				BlockPos blockPos = playerPos.add(x, y, z);

	    				// Check if block is appropriate class
	    				Block block = world.getBlockState(blockPos).getBlock();
	    				if (classType.isInstance(block)) {
	    					double distSq = playerPos.distanceSq(new Vec3i(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
	    					if (distSq < closestDistanceSq) {
	    						closestBlockPos = blockPos;
	    						closestDistanceSq = distSq;
	    					}
	    				}
	    			}
	    		}
	    	}
		} catch (ClassNotFoundException e) {
			System.out.println("Could not find class: " + className);
		}
	    return closestBlockPos;
	}

	
	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		if(mOpenChestKB.isPressed()) {
			this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving() {
				@Override
				public void onLiving(LivingUpdateEvent event) {
					EntityPlayer player = (EntityPlayer) event.getEntityLiving();
					World world = Minecraft.getMinecraft().world;

					BlockPos closestBlockPos = OpenTablesChests.findClosestBlockOfType(
							BlockChest.class.getName(), player, world, mRadius);
					
					// Ask server to open 
					if (null == closestBlockPos) {
						player.sendMessage(new TextComponentString(
								"No chests found in range"));
					}
					else {
						OpenTablesChests.network.sendToServer(
								new ActivateBlockAtPosition(closestBlockPos));
					}
				}
			}));
		}
		else if(mOpenCraftingTableKB.isPressed()) {
			this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving() {
				@Override
				public void onLiving(LivingUpdateEvent event) {
					EntityPlayer player = (EntityPlayer) event.getEntityLiving();
					World world = Minecraft.getMinecraft().world;

					BlockPos closestBlockPos = OpenTablesChests.findClosestBlockOfType(
							BlockWorkbench.class.getName(), player, world, mRadius);

					// Ask server to open 
					if (null == closestBlockPos) {
						player.sendMessage(new TextComponentString(
								"No crafting tables found in range"));
					}
					else {
						OpenTablesChests.network.sendToServer(
								new ActivateBlockAtPosition(closestBlockPos));
					}
				}
			}));
		}
	}

}

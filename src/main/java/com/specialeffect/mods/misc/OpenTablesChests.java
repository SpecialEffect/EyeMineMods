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

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.callbacks.IOnLiving;
import com.specialeffect.callbacks.SingleShotOnLivingCallback;
import com.specialeffect.messages.ActivateBlockAtPosition;
import com.specialeffect.mods.ChildMod;
import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.block.Block;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;


public class OpenTablesChests 
extends BaseClassWithCallbacks
implements ChildMod, ChildModWithConfig
{

	public static final String MODID = "opentableschests";
	public static final String NAME = "OpenTablesChests";

	private static KeyBinding mOpenChestKB;
	private static KeyBinding mOpenCraftingTableKB;	
	
    public static SimpleChannel channel;
    
    private static int mRadius = 5;
    
    private static final String PROTOCOL_VERSION = Integer.toString(1);

    public void setup(final FMLCommonSetupEvent event)
    {
		
		channel = NetworkRegistry.newSimpleChannel(
                new ResourceLocation("specialeffect","opentableschests")
                ,() -> PROTOCOL_VERSION
                , PROTOCOL_VERSION::equals
                , PROTOCOL_VERSION::equals);
        int id = 0;
        
        channel.registerMessage(id++, ActivateBlockAtPosition.class, ActivateBlockAtPosition::encode, 
        		ActivateBlockAtPosition::decode, ActivateBlockAtPosition.Handler::handle);

				
		// Register key bindings	
		mOpenChestKB = new KeyBinding("Open chest", GLFW.GLFW_KEY_LEFT_BRACKET, CommonStrings.EYEGAZE_EXTRA);
		mOpenCraftingTableKB = new KeyBinding("Open crafting table", GLFW.GLFW_KEY_RIGHT_BRACKET, CommonStrings.EYEGAZE_EXTRA);
		ClientRegistry.registerKeyBinding(mOpenChestKB);
		ClientRegistry.registerKeyBinding(mOpenCraftingTableKB);
	}

	public void syncConfig() {
        
	}

	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (ModUtils.entityIsMe(event.getEntityLiving())) {
			this.processQueuedCallbacks(event);
		}			
	}
	
	// Search for closest block of a certain class, within maximum radius
	private static BlockPos findClosestBlockOfType(String className, PlayerEntity player, World world, int radius) {
		BlockPos playerPos = player.getPosition();		
	    Class<?> classType;
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
    public void onKeyInput(KeyInputEvent event) { 
		System.out.println("keyinput current thread = " + Thread.currentThread());
		if(mOpenChestKB.isPressed()) {
			this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving() {
				@Override
				public void onLiving(LivingUpdateEvent event) {
					PlayerEntity player = (PlayerEntity) event.getEntityLiving();
					World world = Minecraft.getInstance().world;

					BlockPos closestBlockPos = OpenTablesChests.findClosestBlockOfType(
							ChestBlock.class.getName(), player, world, mRadius);
					
					// Ask server to open 
					if (null == closestBlockPos) {
						player.sendMessage(new StringTextComponent(
								"No chests found in range"));
					}
					else {
		                channel.sendToServer(new ActivateBlockAtPosition(closestBlockPos));
					}
				}
			}));
		}
		else if(mOpenCraftingTableKB.isPressed()) {
			this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving() {
				@Override
				public void onLiving(LivingUpdateEvent event) {
					PlayerEntity player = (PlayerEntity) event.getEntityLiving();
					World world = Minecraft.getInstance().world;

					BlockPos closestBlockPos = OpenTablesChests.findClosestBlockOfType(
							CraftingTableBlock.class.getName(), player, world, mRadius);

					// Ask server to open 
					if (null == closestBlockPos) {
						player.sendMessage(new StringTextComponent(
								"No crafting tables found in range"));
					}
					else {
						channel.sendToServer(new ActivateBlockAtPosition(closestBlockPos));
					}
				}
			}));
		}
	}

}

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
import com.specialeffect.callbacks.DelayedOnLivingCallback;
import com.specialeffect.callbacks.IOnLiving;
import com.specialeffect.callbacks.SingleShotOnLivingCallback;
import com.specialeffect.messages.UseItemAtPositionMessage;
import com.specialeffect.utils.KeyPressCounter;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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

@Mod(modid = AutoPillar.MODID, 
	 version = ModUtils.VERSION,
	 name = AutoPillar.NAME)
public class AutoPillar extends BaseClassWithCallbacks
{
    public static final String MODID = "specialeffect.autopillar";
    public static final String NAME = "AutoPillar";

    public static KeyBinding autoPlaceKeyBinding;
    
    public static SimpleNetworkWrapper network;
    
    private KeyPressCounter keyCounterWalkDir = new KeyPressCounter();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {    
    	FMLCommonHandler.instance().bus().register(this);
        
        ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Add key binding to create pillar, or 'nerd-pole'.");
		ModUtils.setAsParent(event, SpecialEffectMisc.MODID);

        network = NetworkRegistry.INSTANCE.newSimpleChannel(this.NAME);
        network.registerMessage(UseItemAtPositionMessage.Handler.class, UseItemAtPositionMessage.class, 0, Side.SERVER);
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
		// Subscribe to event buses
        FMLCommonHandler.instance().bus().register(this);
    	MinecraftForge.EVENT_BUS.register(this);

    	// Register key bindings
        autoPlaceKeyBinding = new KeyBinding("Auto-place block", Keyboard.KEY_L, "SpecialEffect");
        ClientRegistry.registerKeyBinding(autoPlaceKeyBinding);
        
    }
    
    private float lastPlayerPitch;
    
    @SubscribeEvent
    public void onLiving(LivingUpdateEvent event) {
    	if (ModUtils.entityIsMe(event.getEntityLiving())) {
			EntityPlayer player = (EntityPlayer)event.getEntityLiving();
			synchronized (mOnLivingQueue) {
				this.lastPlayerPitch = player.rotationPitch;
			}
    				
    		// Process any events which were queued by key events
    		this.processQueuedCallbacks(event);
    	}
    }
    
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        
        // Auto place is implemented as:
        // - Next onLiving tick: look at floor, jump
        // - After 10 ticks (when jump is high): place current item below player.
    	// - From 12 - 17ticks, gradually reset view 
    	// Technically there is no need to change player's view, but the user experience
    	// is weird if you don't (you don't really know what just happened).
        if(autoPlaceKeyBinding.isPressed()) {   
        	float origPitchTemp = 0;        	
        	synchronized (mOnLivingQueue) {
        		origPitchTemp = this.lastPlayerPitch;
			}
  			final float origPitch = origPitchTemp;
  			final float pillarPitch = 85; // 90 = look straight down
  			final int numTicksReset = 5;
  		
  			final float deltaPitch = (pillarPitch - origPitch)/numTicksReset;
  			
        	this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving()
        	{				
				@Override
				public void onLiving(LivingUpdateEvent event) {
		            // It's important to make sure we're approximately - but not exactly - centred
		            // on a block here, so that the block always ends up under us (if you do this
		            // with integer positions you often find your position alternating between 2 blocks)
		    		// Also look down, purely for effect.
					EntityPlayer player = (EntityPlayer)event.getEntityLiving();
					player.setPositionAndRotation(Math.floor(player.posX)+0.4, 
		    						   Math.floor(player.posY), 
		    						   Math.floor(player.posZ)+0.4,
		    						   player.rotationYaw,
		    						   pillarPitch);

		    		// Then jump
		    		player.jump();
				}		
			}));
        	this.queueOnLivingCallback(new DelayedOnLivingCallback(new IOnLiving()
        	{				
				@Override
				public void onLiving(LivingUpdateEvent event) {
 	                World world = Minecraft.getMinecraft().world;
		    		EntityPlayer player = (EntityPlayer)event.getEntityLiving();
		    		BlockPos playerPos = player.getPosition();

	                ItemStack item = player.getHeldItem(EnumHand.MAIN_HAND);
	                if (item != null) {
	                	BlockPos blockPos = new BlockPos(playerPos.getX(), 
	                									 playerPos.getY()-1, // y = up 
	                									 playerPos.getZ());
	                	
			            // Ask server to use item
			    		AutoPillar.network.sendToServer(
			    				new UseItemAtPositionMessage(item, blockPos));

			    		// Make sure we get the animation
			    		player.swingArm(EnumHand.MAIN_HAND);
	                }
				}		
			},
        	10 )); // delayed by 10 ticks
        	
        	for (int i = 1; i < numTicksReset+1; i++) {        		
        		final int j = i;
        		this.queueOnLivingCallback(new DelayedOnLivingCallback(new IOnLiving()
	        	{				
					@Override
					public void onLiving(LivingUpdateEvent event) {
						EntityPlayer player = (EntityPlayer)event.getEntityLiving();
						
						player.setPositionAndRotation(player.posX,
			    						  		 	  player.posY, 
			    						  		 	  player.posZ,
						    						  player.rotationYaw,
						    						  pillarPitch - deltaPitch*j);
						// This forces an update, otherwise you sometimes lose the
						// last delta.
						player.setPositionAndUpdate(player.posX,
			    						  		 	  player.posY, 
			    						  		 	  player.posZ);
					}		
				},
	        	12+j));
        	}
        }
    }
    
    
    
}

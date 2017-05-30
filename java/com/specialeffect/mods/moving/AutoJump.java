/**
 * Copyright (C) 2016 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.mods.moving;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.lwjgl.input.Keyboard;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.callbacks.DelayedOnLivingCallback;
import com.specialeffect.callbacks.IOnLiving;
import com.specialeffect.callbacks.OnLivingCallback;
import com.specialeffect.callbacks.SingleShotOnLivingCallback;
import com.specialeffect.gui.StateOverlay;
import com.specialeffect.messages.UseItemAtPositionMessage;
import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.utils.KeyPressCounter;
import com.specialeffect.utils.ModUtils;
import com.sun.prism.Material;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.command.ICommandManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import scala.collection.parallel.mutable.DoublingUnrolledBuffer;

@Mod(modid = AutoJump.MODID, 
	 version = ModUtils.VERSION,
	 name = AutoJump.NAME)
public class AutoJump 
extends BaseClassWithCallbacks
implements ChildModWithConfig
{
    public static final String MODID = "specialeffect.autojump";
    public static final String NAME = "AutoJump";

    public static KeyBinding autoJumpKeyBinding;    
    public static Configuration mConfig;

    private boolean mDoingAutoJump = true;
	private int mIconIndex;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {    
    	FMLCommonHandler.instance().bus().register(this);
        
        ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Automatically step over blocks.");
    	ModUtils.setAsParent(event, SpecialEffectMovements.MODID);

    }
    
    public void syncConfig() {
    	mDoingAutoJump = SpecialEffectMovements.defaultDoAutoJump;
		StateOverlay.setStateLeftIcon(mIconIndex, mDoingAutoJump);
	}

	@EventHandler
    public void init(FMLInitializationEvent event)
    {
		// Subscribe to event buses
        FMLCommonHandler.instance().bus().register(this);
    	MinecraftForge.EVENT_BUS.register(this);
    	
    	// Register key bindings
        autoJumpKeyBinding = new KeyBinding("Toggle Auto-Jump", Keyboard.KEY_J, "SpecialEffect");
        ClientRegistry.registerKeyBinding(autoJumpKeyBinding);
        
        // Register an icon for the overlay
        mIconIndex = StateOverlay.registerTextureLeft("specialeffect:icons/jump.png");
        
        // Subscribe to parent's config changes
        // This has to happen after texture is registered, since it will trigger a syncConfig call.
    	SpecialEffectMovements.registerForConfigUpdates((ChildModWithConfig) this);
    }
	
    @SubscribeEvent
    public void onLiving(LivingUpdateEvent event) {
    	if (ModUtils.entityIsMe(event.getEntityLiving())) {
    		EntityPlayer player = (EntityPlayer)event.getEntityLiving();
    		
    		if (mDoingAutoJump) {
    			player.stepHeight = 1.0f;
    		}
    		else {
    			player.stepHeight = 0.6f;
    		}
    		
    		// Process any events which were queued by key events
    		this.processQueuedCallbacks(event);
    	}
    }
    
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        
        if(autoJumpKeyBinding.isPressed()) {
        	mDoingAutoJump = !mDoingAutoJump;
    		StateOverlay.setStateLeftIcon(mIconIndex, mDoingAutoJump);

	        this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving()
        	{				
				@Override
				public void onLiving(LivingUpdateEvent event) {
					EntityPlayer player = (EntityPlayer)event.getEntityLiving();
			        player.addChatComponentMessage(new ChatComponentText(
			        		 "Auto jump: " + (mDoingAutoJump ? "ON" : "OFF")));
				}		
			}));
        }
    }
}

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

import org.lwjgl.input.Keyboard;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.callbacks.IOnLiving;
import com.specialeffect.callbacks.SingleShotOnLivingCallback;
import com.specialeffect.gui.StateOverlay;
import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
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
			        player.sendMessage(new TextComponentString(
			        		 "Auto jump: " + (mDoingAutoJump ? "ON" : "OFF")));
				}		
			}));
        }
    }
}

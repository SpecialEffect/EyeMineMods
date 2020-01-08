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

import org.lwjgl.glfw.GLFW;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.callbacks.IOnLiving;
import com.specialeffect.callbacks.SingleShotOnLivingCallback;
//FIXME import com.specialeffect.gui.StateOverlay;
import com.specialeffect.mods.EyeGaze;
import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.java.games.input.Keyboard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AutoJump.MODID)
public class AutoJump 
extends BaseClassWithCallbacks
implements ChildModWithConfig
{
    public static final String MODID = "autojump";
    public static final String NAME = "AutoJump";

    public static KeyBinding autoJumpKeyBinding;    
    //FIXME public static Configuration mConfig;

    private boolean mDoingAutoJump = true;
	private int mIconIndex;

	public AutoJump() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
	}
	
	private void setup(final FMLCommonSetupEvent event) {
		//preinit
		
    	MinecraftForge.EVENT_BUS.register(this);
        
        ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Automatically step over blocks.");
    	ModUtils.setAsParent(event, EyeGaze.MODID);

    	//init 
    	// Register key bindings
        autoJumpKeyBinding = new KeyBinding("Turn auto-jump on/off", GLFW.GLFW_KEY_J, CommonStrings.EYEGAZE_COMMON);
        ClientRegistry.registerKeyBinding(autoJumpKeyBinding);
        
        // Register an icon for the overlay
        //FIXME mIconIndex = StateOverlay.registerTextureLeft("specialeffect:icons/jump.png");
        
        // Subscribe to parent's config changes
        // This has to happen after texture is registered, since it will trigger a syncConfig call.
    	EyeGaze.registerForConfigUpdates((ChildModWithConfig) this);
    }
    
    public void syncConfig() {
    	mDoingAutoJump = EyeGaze.defaultDoAutoJump;
    	// Turn off vanilla autojump since it doesn't play nicely with 
    	// our gaze-based walking methods.
    	Minecraft.getInstance().gameSettings.autoJump = mDoingAutoJump;
		//FIXME: StateOverlay.setStateLeftIcon(mIconIndex, mDoingAutoJump);
	}

	
	
    @SubscribeEvent
    public void onLiving(LivingUpdateEvent event) {
    	if (ModUtils.entityIsMe(event.getEntityLiving())) {
    		PlayerEntity player = (PlayerEntity)event.getEntityLiving();
    		
    		// We can't rely solely on the vanilla autojump implementation,
    		// since it doesn't play nicely with our gaze movement methods.
    		// We'll keep it in sync though so that keyboard-play is consistent
    		// with our autojump state (if you're moving with the keyboard you
    		// get visually-nicer autojump behaviour).
    		if (mDoingAutoJump) {
    			player.stepHeight = 1.0f;
    		}
    		else {
    			player.stepHeight = 0.6f;
    		}
	    	Minecraft.getInstance().gameSettings.autoJump = mDoingAutoJump;

    		// Process any events which were queued by key events
    		this.processQueuedCallbacks(event);
    	}
    }
    
    @SubscribeEvent
    public void onClientTickEvent(final ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        if(autoJumpKeyBinding.isPressed()) {
        	mDoingAutoJump = !mDoingAutoJump;
    		//FIXME: StateOverlay.setStateLeftIcon(mIconIndex, mDoingAutoJump);

	        this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving()
        	{				
				@Override
				public void onLiving(LivingUpdateEvent event) {
					PlayerEntity player = (PlayerEntity)event.getEntityLiving();
			        player.sendMessage(new StringTextComponent(
			        		 "Auto jump: " + (mDoingAutoJump ? "ON" : "OFF")));
				}		
			}));
        }
    }
}

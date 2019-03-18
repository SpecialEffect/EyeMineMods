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

import java.awt.Point;

import org.lwjgl.input.Keyboard;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.callbacks.IOnLiving;
import com.specialeffect.callbacks.SingleShotOnLivingCallback;
import com.specialeffect.messages.MovePlayerMessage;
import com.specialeffect.mods.EyeGaze;
import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.KeyPressCounter;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(WalkIncrements.MODID)
public class WalkIncrements 
extends BaseClassWithCallbacks
implements ChildModWithConfig
{
    public static final String MODID = "specialeffect.walkincrements";
    public static final String NAME = "WalkIncrements";
    public static Configuration mConfig;

    public static KeyBinding walkKeyBinding;
    public static KeyBinding walkDirectionKeyBinding;
    
    public static SimpleNetworkWrapper network;
    
    private KeyPressCounter keyCounterWalkDir = new KeyPressCounter();

    @EventHandler
	@SuppressWarnings("static-access")
    public void preInit(FMLPreInitializationEvent event) {    
    	MinecraftForge.EVENT_BUS.register(this);
    	mConfig = new Configuration(event.getSuggestedConfigurationFile());
    	this.syncConfig();
        
        ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Add key bindings to walk fixed amount, for alternative inputs.");
    	ModUtils.setAsParent(event, EyeGaze.MODID);
        
        network = NetworkRegistry.INSTANCE.newSimpleChannel(this.NAME);
        network.registerMessage(MovePlayerMessage.Handler.class, MovePlayerMessage.class, 0, Side.SERVER);

    }
    
    @SubscribeEvent
	@SuppressWarnings("static-access")
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if(eventArgs.getModID().equals(this.MODID)) {
			syncConfig();
		}
	}
    
    public void syncConfig() {       
        mWalkDistance = EyeGaze.moveIncrement;
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	
    	// Subscribe to parent's config changes
    	EyeGaze.registerForConfigUpdates((ChildModWithConfig) this);
    	
    	// Register key bindings
    	walkDirectionKeyBinding = new KeyBinding("Configure walking direction", Keyboard.KEY_O, CommonStrings.EYEGAZE_ADVANCED);
        ClientRegistry.registerKeyBinding(walkDirectionKeyBinding);
        
    	walkKeyBinding = new KeyBinding("Step forward", Keyboard.KEY_P, CommonStrings.EYEGAZE_ADVANCED);
        ClientRegistry.registerKeyBinding(walkKeyBinding);
        
    }
    
    @SubscribeEvent
    public void onLiving(LivingUpdateEvent event) {
    	if (ModUtils.entityIsMe(event.getEntityLiving())) {
    		// Process any events which were queued by key events
    		this.processQueuedCallbacks(event);
    	}
    }
    
    private static double mWalkDistance = 1.0f;

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        
        // Configure walk direction for next "walk" command.
        // a = north, aa = north-east, aaa = east, etc.
        if(walkDirectionKeyBinding.isPressed()) {
        	keyCounterWalkDir.increment();
        }

        // Walk: Move 100 units forward next onLiving tick.
        if(walkKeyBinding.isPressed()) {
        	final int i = keyCounterWalkDir.getCount();
        	keyCounterWalkDir.reset();
        	
            this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving() {
				@Override
				public void onLiving(LivingUpdateEvent event) {
					EntityPlayer player = (EntityPlayer)event.getEntityLiving();
					Point p = ModUtils.getCompassPoint(i);

					float theta = (float)Math.atan2(p.getX(), p.getY());

					if (player.isRiding()) {
						// Ask server to move entity being ridden
						WalkIncrements.network.sendToServer(
							new MovePlayerMessage((float)mWalkDistance, theta));
					}
					else {
						float strafe = - (float)(p.getX() * mWalkDistance);
						float forward = (float)(p.getY() * mWalkDistance);
						player.moveEntityWithHeading(strafe, forward);
					}
				}
			}));
        }
    }
}

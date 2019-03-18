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
import com.specialeffect.messages.UseItemAtPositionMessage;
import com.specialeffect.mods.EyeGaze;
import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.KeyPressCounter;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
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

@Mod(ViewIncrements.MODID)
public class ViewIncrements 
extends BaseClassWithCallbacks
implements ChildModWithConfig
{
    public static final String MODID = "specialeffect.viewincrements";
    public static final String NAME = "ViewIncrements";
    public static Configuration mConfig;

    public static KeyBinding moveViewKB;
	public static KeyBinding viewDirectionKeyBinding;
    
    public static SimpleNetworkWrapper network;
    
    private KeyPressCounter keyCounterViewDir = new KeyPressCounter();


    @EventHandler
	@SuppressWarnings("static-access")
    public void preInit(FMLPreInitializationEvent event) {    
    	MinecraftForge.EVENT_BUS.register(this);
    	mConfig = new Configuration(event.getSuggestedConfigurationFile());
    	this.syncConfig();
        
        ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Add key bindings to change view by fixed amount, for alternative inputs.");
    	ModUtils.setAsParent(event, EyeGaze.MODID);

    	network = NetworkRegistry.INSTANCE.newSimpleChannel(this.NAME);
        network.registerMessage(UseItemAtPositionMessage.Handler.class, UseItemAtPositionMessage.class, 0, Side.SERVER);
        
    }
    
    public void syncConfig() {
    	mViewDeltaRelative = EyeGaze.viewIncrement;
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	// Subscribe to parent's config changes
    	EyeGaze.registerForConfigUpdates((ChildModWithConfig) this);
    	
    	// Register key bindings
    	viewDirectionKeyBinding = new KeyBinding("Configure direction for view delta", Keyboard.KEY_U, CommonStrings.EYEGAZE_ADVANCED);
        ClientRegistry.registerKeyBinding(viewDirectionKeyBinding);
        
    	moveViewKB = new KeyBinding("Apply view delta", Keyboard.KEY_I, CommonStrings.EYEGAZE_ADVANCED);
        ClientRegistry.registerKeyBinding(moveViewKB);
        
    }
    
    @SubscribeEvent
    public void onLiving(LivingUpdateEvent event) {
    	if (ModUtils.entityIsMe(event.getEntityLiving())) {
    		// Process any events which were queued by key events
    		this.processQueuedCallbacks(event);
    	}
    }
    
    private static int mViewDeltaRelative = 2;

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        
        // Configure walk direction for next "walk" command.
        // a = north, aa = north-east, aaa = east, etc.
        if(viewDirectionKeyBinding.isPressed()) {
        	keyCounterViewDir.increment();
        }

        // Walk: Move 100 units forward next onLiving tick.
        if(moveViewKB.isPressed()) {
        	final int i = keyCounterViewDir.getCount();
        	keyCounterViewDir.reset();
        	
            this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving() {
				@Override
				public void onLiving(LivingUpdateEvent event) {
					
					Point p = ModUtils.getCompassPoint(i);
			    	int dYaw = (int)p.getX() * mViewDeltaRelative;
			    	int dPitch = - (int)p.getY() * mViewDeltaRelative; // pitch is opposite to what you expect

			    	EntityPlayer player = (EntityPlayer)event.getEntityLiving();

			    	Vec3d pos = player.getPositionVector();
	    			float yaw = player.rotationYaw;
	    			float pitch = player.rotationPitch; 
	    			
	    			player.setPositionAndRotation(pos.xCoord, pos.yCoord, pos.zCoord, 
	    					(float)(yaw+dYaw), (float)(pitch+dPitch));
				}
			}));
        }
    }
}

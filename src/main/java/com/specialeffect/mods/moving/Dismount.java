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

import com.specialeffect.messages.DismountPlayerMessage;
import com.specialeffect.messages.RideEntityMessage;
import com.specialeffect.mods.ChildMod;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;



public class Dismount  extends ChildMod {

	public final String MODID = "dismount";
	private static final String PROTOCOL_VERSION = Integer.toString(1);

	private static KeyBinding mDismountKB;
	
    public static SimpleChannel channel;

	public void setup(final FMLCommonSetupEvent event) {

    	// setup channel for comms
		channel = NetworkRegistry.newSimpleChannel(
                new ResourceLocation("specialeffect","dismount")
                ,() -> PROTOCOL_VERSION
                , PROTOCOL_VERSION::equals
                , PROTOCOL_VERSION::equals);
        int id = 0;        
        channel.registerMessage(id++, DismountPlayerMessage.class, DismountPlayerMessage::encode, 
        		DismountPlayerMessage::decode, DismountPlayerMessage.Handler::handle);                   	       
        channel.registerMessage(id++, RideEntityMessage.class, RideEntityMessage::encode, 
        		RideEntityMessage::decode, RideEntityMessage.Handler::handle);                   	       
		
		// Register key bindings
		mDismountKB = new KeyBinding("Ride or dismount", GLFW.GLFW_KEY_F15, CommonStrings.EYEGAZE_EXTRA);
		ClientRegistry.registerKeyBinding(mDismountKB);
	}

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {
		if (ModUtils.hasActiveGui()) { return; }
		
		if(mDismountKB.isPressed()) {
			// Dismount player locally
			PlayerEntity player = Minecraft.getInstance().player;
			if (player.isPassenger()) {					
				player.stopRiding();				
				// Dismount player on server
		        channel.sendToServer(new DismountPlayerMessage());
			}
			else {						
				EntityRayTraceResult entity = ModUtils.getMouseOverEntity();									
				if (entity != null) {									
					player.startRiding(entity.getEntity());
					channel.sendToServer(new RideEntityMessage(entity.getEntity().getEntityId()));					
				}
			}			
		}
	}

}

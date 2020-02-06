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
import com.specialeffect.messages.DismountPlayerMessage;
import com.specialeffect.mods.ChildMod;
import com.specialeffect.mods.EyeGaze;
import com.specialeffect.mods.mining.GatherDrops;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings.Input;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;



public class Dismount extends BaseClassWithCallbacks implements ChildMod {

	public static final String MODID = "dismount";
	public static final String NAME = "Dismount";
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
		
		// Register key bindings
		mDismountKB = new KeyBinding("Ride or dismount", GLFW.GLFW_KEY_F15, CommonStrings.EYEGAZE_EXTRA);
		ClientRegistry.registerKeyBinding(mDismountKB);
	}

	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (ModUtils.entityIsMe(event.getEntityLiving())) {
			this.processQueuedCallbacks(event);
		}
	}

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {
        
		if(mDismountKB.isPressed()) {
			// Dismount player locally
			this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving() {
				@Override
				public void onLiving(LivingUpdateEvent event) {
					PlayerEntity player = (PlayerEntity)event.getEntityLiving();

					if (player.isPassenger()) {
						player.detach();						
						// FIXME??	player.motionY += 0.5D;
						
						// Dismount player on server
				        channel.sendToServer(new DismountPlayerMessage());

					}
					else {						
						EntityRayTraceResult entity = ModUtils.getMouseOverEntity();									
						if (entity != null) {
							// FIXME: see if there's a better way to do this now
							//
							// Riding entity programmatically seems to not do everything that 
							// a "Use" action would do, so we:
							// - drop current item to ensure empty hand
							// - "use" entity you're pointing at
							// - pick up dropped item again
							player.dropItem(true);
							Input useItemKeyCode = Minecraft.getInstance().gameSettings.keyBindUseItem.getKey();
							KeyBinding.onTick(useItemKeyCode);
							GatherDrops.gatherBlocks(player);							
						
						}
					}
				}
			}));	
		}
	}

}

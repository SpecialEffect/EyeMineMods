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

import java.util.List;
import org.lwjgl.glfw.GLFW;

import com.specialeffect.messages.DismountPlayerMessage;
import com.specialeffect.messages.RideEntityMessage;
import com.specialeffect.mods.ChildMod;
import com.specialeffect.mods.utils.KeyWatcher;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.minecart.MinecartEntity;
import net.minecraft.entity.passive.horse.HorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;



public class Dismount  extends ChildMod {

	public final String MODID = "dismount";

	private static KeyBinding mDismountKB;
	
	public void setup(final FMLCommonSetupEvent event) {

    	// setup channel for comms
        this.setupChannel(MODID, 1);

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
	    if (event.getAction() != GLFW.GLFW_PRESS) { return; }
		
	    if (KeyWatcher.f3Pressed) { return; }

		if(mDismountKB.getKey().getKeyCode() == event.getKey()) {
			PlayerEntity player = Minecraft.getInstance().player;
			if (player.isPassenger()) {
				// Dismount player locally
				player.stopRiding();				
				player.jump();
				// Dismount player on server
		        channel.sendToServer(new DismountPlayerMessage());
			}
			else {						
				EntityRayTraceResult entityResult = ModUtils.getMouseOverEntity();
				Entity entity = entityResult == null ? null : entityResult.getEntity();
				
				if (entity == null) {
					// If there's nothing under the crosshair, but there's something rideable really close, 
					// assume this was the intended target
					// (helps if mob walking away while you are dwelling)
					
					World world = Minecraft.getInstance().world;					
					AxisAlignedBB box = player.getBoundingBox().grow(2);
					
					List<Entity> mobEntities = world.getEntitiesWithinAABB(MobEntity.class, box);
					List<Entity> boatEntities = world.getEntitiesWithinAABB(BoatEntity.class, box);
					List<Entity> minecartEntities = world.getEntitiesWithinAABB(MinecartEntity.class, box);					
					
					List<Entity> entities = ModUtils.joinLists(mobEntities, boatEntities, minecartEntities);					
					
					for (Entity e : entities) {
						System.out.println(e);
					}
					if (entities.isEmpty()) {
						ModUtils.sendPlayerMessage("Nothing found to ride");
					}
					else if (entities.size() == 1) {
						entity = entities.get(0); 
						ModUtils.sendPlayerMessage("Attempting to mount nearby "+entity.getName().getString());						
					}
					else {
						ModUtils.sendPlayerMessage("Found multiple entities nearby, please use crosshair to select");
					}					
				}
				if (entity != null) {				
					// Ideally we already have an empty hand, and we can use this to interact
					// with the entity (and therefore let the entity handle all riding logic itself)
					// If we try to ride the entity directly, we end up riding things that shouldn't be ridden!
					
					// Most EyeMine users will probably have an empty off-hand (since we don't give direct access
					// to using the off-hand). We'll use this hand if it's empty, to avoid the need to remove items. 
					// If someone is advanced enough to fill their offhand, they can work out how to drop their item.
					Hand hand = Hand.MAIN_HAND;
					if (player.getHeldItem(Hand.OFF_HAND).getItem() == Items.AIR) {
						hand = Hand.OFF_HAND;
					}
					// special case warning (there are probably more scenarios)
					else if (entity instanceof HorseEntity && 
							player.getHeldItem(Hand.MAIN_HAND).getItem() != Items.AIR) {
						ModUtils.sendPlayerMessage("You need an empty hand to ride a horse");
					}
					
					entity.processInitialInteract(player, hand);
					channel.sendToServer(new RideEntityMessage(entity.getEntityId(), hand));					
				}
			}			
		}
	}

}

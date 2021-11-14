/**
 * Copyright (C) 2016-2020 Kirsty McNaught
 * 
 * Developed for SpecialEffect, www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.eyemine.submod.movement;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;
import com.specialeffect.eyemine.client.Keybindings;
import com.specialeffect.eyemine.submod.SubMod;
import com.specialeffect.utils.ModUtils;
import me.shedaniel.architectury.event.events.client.ClientScreenInputEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class Dismount extends SubMod {
	public final String MODID = "dismount";

	private static KeyMapping mDismountKB;

	public void onInitializeClient() {
		// Register key bindings
		Keybindings.keybindings.add(mDismountKB = new KeyMapping(
				"key.eyemine.ride_or_dismount",
				Type.KEYSYM,
				GLFW.GLFW_KEY_F15,
				"category.eyemine.category.eyegaze_extra" // The translation key of the keybinding's category.
		));

		ClientScreenInputEvent.KEY_PRESSED_POST.register(this::onKeyInput);
	}

	public InteractionResult onKeyInput(Minecraft minecraft, Screen screen, int keyCode, int scanCode, int modifiers) {
		if (ModUtils.hasActiveGui()) { return InteractionResult.FAIL; }

		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 292)) { return InteractionResult.FAIL; }

		if(mDismountKB.consumeClick()) {
			LocalPlayer player = Minecraft.getInstance().player;
			if (player.isPassenger()) {
				// Dismount player locally
				player.stopRiding();
				player.jumpFromGround();
				// Dismount player on server
//		        channel.sendToServer(new DismountPlayerMessage());
				player.connection.send(new ServerboundPlayerInputPacket(player.xxa, player.zza, false, true));
			} else {
				EntityHitResult entityResult = ModUtils.getMouseOverEntity();
				Entity entity = entityResult == null ? null : entityResult.getEntity();
				
				if (entity == null) {
					// If there's nothing under the crosshair, but there's something rideable really close, 
					// assume this was the intended target
					// (helps if mob walking away while you are dwelling)
					
					Level level = minecraft.level;
					AABB box = player.getBoundingBox().inflate(2);
					
					List<Mob> mobEntities = level.getEntitiesOfClass(Mob.class, box);
					List<Boat> boatEntities = level.getEntitiesOfClass(Boat.class, box);
					List<Minecart> minecartEntities = level.getEntitiesOfClass(Minecart.class, box);
					
					List<Entity> entities = new ArrayList<>();
					entities.addAll(mobEntities);
					entities.addAll(boatEntities);
					entities.addAll(minecartEntities);

					for (Entity e : entities) {
						LOGGER.debug(e);
					}
					if (entities.isEmpty()) {
						ModUtils.sendPlayerMessage("Nothing found to ride");
					} else if (entities.size() == 1) {
						entity = entities.get(0);
						if(entity instanceof Saddleable || entity instanceof Minecart || entity instanceof Boat) {
							ModUtils.sendPlayerMessage("Attempting to mount nearby " + entity.getName().getString());
						}
					} else {
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
					InteractionHand hand = InteractionHand.MAIN_HAND;
					if (player.getItemInHand(InteractionHand.OFF_HAND).isEmpty()) {
						hand = InteractionHand.OFF_HAND;
					}
					// special case warning (there are probably more scenarios)
					else if (entity instanceof Horse &&
							!player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
						ModUtils.sendPlayerMessage("You need an empty hand to ride a horse");
					}

					entity.interact(player, hand);
					player.connection.send(new ServerboundInteractPacket(entity, hand, player.isShiftKeyDown()));
				}
			}			
		}
		return InteractionResult.SUCCESS;
	}

}

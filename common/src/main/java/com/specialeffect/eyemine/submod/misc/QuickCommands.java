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

package com.specialeffect.eyemine.submod.misc;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;
import com.specialeffect.eyemine.client.Keybindings;
import com.specialeffect.eyemine.packets.PacketHandler;
import com.specialeffect.eyemine.packets.messages.SendCommandMessage;
import com.specialeffect.eyemine.packets.messages.TeleportPlayerToSpawnPointMessage;
import com.specialeffect.eyemine.submod.SubMod;
import com.specialeffect.utils.ModUtils;
import me.shedaniel.architectury.event.events.client.ClientRawInputEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.BooleanValue;
import org.lwjgl.glfw.GLFW;

public class QuickCommands extends SubMod {
	public final String MODID = "quickcommands";

	private static KeyMapping mNightVisionKB;
	private static KeyMapping mDayNightKB;
	private static KeyMapping mRespawnKB;
	private static KeyMapping mDropItemKB;
	
	public void onInitializeClient() {
		// Register key bindings
		Keybindings.keybindings.add(mNightVisionKB = new KeyMapping(
				"key.eyemine.night_vision",
				Type.KEYSYM,
				GLFW.GLFW_KEY_F12,
				"category.eyemine.category.eyegaze_extra" // The translation key of the keybinding's category.
		));

		Keybindings.keybindings.add(mDayNightKB = new KeyMapping(
				"key.eyemine.day_cycle",
				Type.KEYSYM,
				GLFW.GLFW_KEY_F14,
				"category.eyemine.category.eyegaze_extra" // The translation key of the keybinding's category.
		));

		Keybindings.keybindings.add(mRespawnKB = new KeyMapping(
				"key.eyemine.respawn",
				Type.KEYSYM,
				GLFW.GLFW_KEY_HOME,
				"category.eyemine.category.eyegaze_extra" // The translation key of the keybinding's category.
		));

		Keybindings.keybindings.add(mDropItemKB = new KeyMapping(
				"key.eyemine.drop_item",
				Type.KEYSYM,
				GLFW.GLFW_KEY_MINUS,
				"category.eyemine.category.eyegaze_extra" // The translation key of the keybinding's category.
		));

		ClientRawInputEvent.KEY_PRESSED.register(this::onKeyInput);
	}

	private InteractionResult onKeyInput(Minecraft minecraft, int keyCode, int scanCode, int action, int modifiers) {
		if (ModUtils.hasActiveGui()) { return InteractionResult.PASS; }

		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 292)) { return InteractionResult.PASS; }

		final LocalPlayer player = Minecraft.getInstance().player;
		final ClientLevel level = minecraft.level;
		if (mNightVisionKB.matches(keyCode, scanCode) && mNightVisionKB.consumeClick()) {
			// Toggle night vision effect
			MobEffect nightVision = MobEffects.NIGHT_VISION;
			
			if (player.hasEffect(nightVision)) {
				player.removeEffect(nightVision);
			}
			else {
				player.addEffect(new MobEffectInstance(nightVision));
				NightVisionHelper.cancelAndHide();
			}		
		}
		
		if (ModUtils.hasActiveGui()) { return InteractionResult.PASS; }

		if (mDropItemKB.matches(keyCode, scanCode) && mDropItemKB.consumeClick()) {
			// Drop item 
			// This is a duplicate key binding to the built-in one, so we can use the same for discarding
			// an item while the inventory is open. The inventory keybinding needs to be a key not used
			// for typing.
			ItemStack stack = player.inventory.getSelected();
			player.drop(stack, true); //TODO: see if this still drops all?
		}
		
		if (mDayNightKB.matches(keyCode, scanCode) && mDayNightKB.consumeClick()) {
			GameRules rules = level.getGameRules();

			GameRules.Key<BooleanValue> gameRule = GameRules.RULE_DAYLIGHT;
			boolean newBool = !rules.getBoolean(gameRule);
			
			String cmd = "/gamerule " + gameRule + " " + newBool;
			PacketHandler.CHANNEL.sendToServer(new SendCommandMessage(cmd));
		}
		
		if (mRespawnKB.matches(keyCode, scanCode) && mRespawnKB.consumeClick()) {
            PacketHandler.CHANNEL.sendToServer(new TeleportPlayerToSpawnPointMessage());
            NightVisionHelper.cancelAndHide();
        }
		return InteractionResult.PASS;
	}
}

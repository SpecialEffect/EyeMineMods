/**
 * Copyright (C) 2016-2020 Kirsty McNaught
 * <p>
 * Developed for SpecialEffect, www.specialeffect.org.uk
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.eyemine.submod.misc;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;
import com.specialeffect.eyemine.client.Keybindings;
import com.specialeffect.eyemine.packets.messages.SendCommandMessage;
import com.specialeffect.eyemine.packets.messages.TeleportPlayerToSpawnPointMessage;
import com.specialeffect.eyemine.submod.SubMod;
import com.specialeffect.eyemine.submod.KeyInputUtil;
import com.specialeffect.utils.ModUtils;
import com.specialeffect.eyemine.event.EventResult;
import com.specialeffect.eyemine.event.EyeMineEvents;
import com.specialeffect.eyemine.packets.NetworkService;
import com.specialeffect.eyemine.platform.Services;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;
import org.lwjgl.glfw.GLFW;

public class QuickCommands extends SubMod {
	public final String MODID = "quickcommands";

	// Track day/night cycle toggle state locally since GameRules aren't accessible on client in 26.1.1
	private boolean dayNightCycleEnabled = true;

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
				Keybindings.EYEGAZE_EXTRA // The translation key of the keybinding's category.
		));

		Keybindings.keybindings.add(mDayNightKB = new KeyMapping(
				"key.eyemine.day_cycle",
				Type.KEYSYM,
				GLFW.GLFW_KEY_F14,
				Keybindings.EYEGAZE_EXTRA // The translation key of the keybinding's category.
		));

		Keybindings.keybindings.add(mRespawnKB = new KeyMapping(
				"key.eyemine.respawn",
				Type.KEYSYM,
				GLFW.GLFW_KEY_HOME,
				Keybindings.EYEGAZE_EXTRA // The translation key of the keybinding's category.
		));

		Keybindings.keybindings.add(mDropItemKB = new KeyMapping(
				"key.eyemine.drop_item",
				Type.KEYSYM,
				GLFW.GLFW_KEY_MINUS,
				Keybindings.EYEGAZE_EXTRA // The translation key of the keybinding's category.
		));

		EyeMineEvents.KEY_PRESSED.register(this::onKeyInput);
	}

	private EventResult onKeyInput(Minecraft minecraft, int keyCode, int scanCode, int action, int modifiers) {
		if (KeyInputUtil.shouldIgnoreKeyInput(minecraft)) {
			return EventResult.pass();
		}

		final LocalPlayer player = Minecraft.getInstance().player;
		if (mNightVisionKB.matches(new net.minecraft.client.input.KeyEvent(keyCode, scanCode, modifiers)) && mNightVisionKB.consumeClick()) {
			// Toggle night vision effect
			Holder<MobEffect> nightVision = MobEffects.NIGHT_VISION;

			if (player.hasEffect(nightVision)) {
				player.removeEffect(nightVision);
			} else {
				player.addEffect(new MobEffectInstance(nightVision));
				NightVisionHelper.cancelAndHide();
			}
		}

		if (ModUtils.hasActiveGui()) {
			return EventResult.pass();
		}

		if (mDropItemKB.matches(new net.minecraft.client.input.KeyEvent(keyCode, scanCode, modifiers)) && mDropItemKB.consumeClick()) {
			// Drop item 
			// This is a duplicate key binding to the built-in one, so we can use the same for discarding
			// an item while the inventory is open. The inventory keybinding needs to be a key not used
			// for typing.
			ItemStack stack = player.getInventory().getSelectedItem();
			player.drop(stack, true); //TODO: see if this still drops all?
		}

		if (mDayNightKB.matches(new net.minecraft.client.input.KeyEvent(keyCode, scanCode, modifiers)) && mDayNightKB.consumeClick()) {
			// Toggle the day/night cycle - track state locally since GameRules
			// are not accessible on the client side in 26.1.1
			dayNightCycleEnabled = !dayNightCycleEnabled;

			String cmd = "/gamerule " + GameRules.ADVANCE_TIME.id() + " " + dayNightCycleEnabled;
			Services.NETWORK.sendToServer(new SendCommandMessage(cmd));
		}

		if (mRespawnKB.matches(new net.minecraft.client.input.KeyEvent(keyCode, scanCode, modifiers)) && mRespawnKB.consumeClick()) {
			Services.NETWORK.sendToServer(new TeleportPlayerToSpawnPointMessage());
			NightVisionHelper.cancelAndHide();
		}
		return EventResult.pass();
	}
}

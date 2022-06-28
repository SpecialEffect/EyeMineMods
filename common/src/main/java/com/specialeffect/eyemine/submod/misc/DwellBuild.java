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
import com.specialeffect.eyemine.mixin.KeyMappingAccessor;
import com.specialeffect.eyemine.submod.utils.DwellAction;
import com.specialeffect.eyemine.submod.utils.TargetBlock;
import com.specialeffect.utils.ModUtils;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientRawInputEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class DwellBuild extends DwellAction {

	public DwellBuild() {
		super("DWELL BUILDING");
	}

	public final String MODID = "dwellbuild";

	private static KeyMapping mDwellBuildKB;
	private static KeyMapping mDwellBuildOnceKB;

	public void onInitializeClient() {
		// Register key bindings
		Keybindings.keybindings.add(mDwellBuildKB = new KeyMapping(
				"key.eyemine.toggle_dwell_build",
				Type.KEYSYM,
				GLFW.GLFW_KEY_KP_3,
				"category.eyemine.category.eyegaze_extra" // The translation key of the keybinding's category.
		));
		Keybindings.keybindings.add(mDwellBuildOnceKB = new KeyMapping(
				"key.eyemine.dwell_build_once",
				Type.KEYSYM,
				GLFW.GLFW_KEY_KP_7,
				"category.eyemine.category.eyegaze_extra" // The translation key of the keybinding's category.
		));

		ClientTickEvent.CLIENT_PRE.register(this::onClientTick);
		ClientRawInputEvent.KEY_PRESSED.register(this::onKeyInput);

		//Initialize variables
		super.onInitializeClient();
	}

	@Override
	public void performAction(TargetBlock block) {
		final KeyMapping useItemKeyBinding = Minecraft.getInstance().options.keyUse;
		KeyMapping.click(((KeyMappingAccessor) useItemKeyBinding).getActualKey());
	}

	private EventResult onKeyInput(Minecraft minecraft, int keyCode, int scanCode, int action, int modifiers) {
		if (ModUtils.hasActiveGui()) {
			return EventResult.pass();
		}

		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 292)) {
			return EventResult.pass();
		}

		if (mDwellBuildKB.matches(keyCode, scanCode) && mDwellBuildKB.consumeClick()) {
			Player player = Minecraft.getInstance().player;
			if (mDwelling) {
				// Turn off dwell build
				setDwelling(false);
				ModUtils.sendPlayerMessage("Dwell building: OFF");
			} else {
				// Turn on dwell build
				ItemStack itemStack = player.getInventory().getSelected();
				if (itemStack.isEmpty()) {
					player.sendSystemMessage(Component.literal("Nothing in hand to use"));
					return EventResult.pass();
				}

				setDwelling(true);
				ModUtils.sendPlayerMessage("Dwell building: ON");
			}
		}
		if (mDwellBuildOnceKB.matches(keyCode, scanCode) && mDwellBuildOnceKB.consumeClick()) {
			Player player = Minecraft.getInstance().player;

			// Turn on dwell once
			ItemStack itemStack = player.getInventory().getSelected();
			if (itemStack.isEmpty()) {
				player.sendSystemMessage(Component.literal("Nothing in hand to use"));
				return EventResult.pass();
			}

			dwellOnce();
		}
		return EventResult.pass();
	}
}

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
import com.specialeffect.eyemine.submod.SubMod;
import com.specialeffect.utils.ModUtils;
import me.shedaniel.architectury.event.events.client.ClientScreenInputEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.InteractionResult;
import org.lwjgl.glfw.GLFW;

public class OpenChat extends SubMod {
	public final String MODID = "openchat";

	private static KeyMapping mOpenChatKB;

	public void onInitializeClient() {
		// Register key bindings
		Keybindings.keybindings.add(mOpenChatKB = new KeyMapping(
				"key.eyemine.open_chat",
				Type.KEYSYM,
				GLFW.GLFW_KEY_END,
				"category.eyemine.category.eyegaze_extra" // The translation key of the keybinding's category.
		));

		ClientScreenInputEvent.KEY_PRESSED_POST.register(this::onKeyInput);
	}

	public InteractionResult onKeyInput(Minecraft minecraft, Screen screen, int keyCode, int scanCode, int modifiers) {
		if (ModUtils.hasActiveGui()) { return InteractionResult.FAIL; }

		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 292)) { return InteractionResult.FAIL; }

		if (mOpenChatKB.consumeClick()) {
			final InputConstants.Key chatKeyCode = minecraft.options.keyChat.getDefaultKey();
			KeyMapping.click(chatKeyCode);
		}
		return InteractionResult.SUCCESS;
	}
}

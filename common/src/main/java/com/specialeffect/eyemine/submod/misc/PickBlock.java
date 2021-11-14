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

public class PickBlock extends SubMod {
	public final String MODID = "pickblock";

	public static KeyMapping mPickBlockKB;

	public void onInitializeClient() {
        // Register key bindings
		Keybindings.keybindings.add(mPickBlockKB = new KeyMapping(
				"key.eyemine.pick_block",
				Type.KEYSYM,
				GLFW.GLFW_KEY_KP_2,
				"category.eyemine.category.eyegaze_common" // The translation key of the keybinding's category.
		));

		ClientScreenInputEvent.KEY_PRESSED_POST.register(this::onKeyInput);
    }

	public InteractionResult onKeyInput(Minecraft minecraft, Screen screen, int keyCode, int scanCode, int modifiers) {
		if (ModUtils.hasActiveGui()) { return InteractionResult.FAIL; }

		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 292)) { return InteractionResult.FAIL; }
		
		if (mPickBlockKB.consumeClick()) {
			KeyMapping.click(minecraft.options.keyPickItem.getDefaultKey());
		}
		return InteractionResult.SUCCESS;
    }
}

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
import com.specialeffect.eyemine.submod.SubMod;
import com.specialeffect.utils.ModUtils;
import com.specialeffect.eyemine.event.EventResult;
import com.specialeffect.eyemine.event.EyeMineEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
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
				Keybindings.EYEGAZE_COMMON // The translation key of the keybinding's category.
		));

		EyeMineEvents.KEY_PRESSED.register(this::onKeyInput);
	}

	private EventResult onKeyInput(Minecraft minecraft, int keyCode, int scanCode, int action, int modifiers) {
		if (ModUtils.hasActiveGui()) {
			return EventResult.pass();
		}

		if (InputConstants.isKeyDown(minecraft.getWindow(), 292)) {
			return EventResult.pass();
		}

		if (mPickBlockKB.matches(new net.minecraft.client.input.KeyEvent(keyCode, scanCode, modifiers)) && mPickBlockKB.consumeClick()) {
			KeyMapping pickBlockKey = minecraft.options.keyPickItem;
			KeyMapping.click(((KeyMappingAccessor) pickBlockKey).getActualKey());
		}
		return EventResult.pass();
	}
}

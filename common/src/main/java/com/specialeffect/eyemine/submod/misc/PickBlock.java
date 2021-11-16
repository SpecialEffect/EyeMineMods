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
import com.specialeffect.eyemine.mixin.KeyMappingAccessor;
import com.specialeffect.eyemine.submod.SubMod;
import com.specialeffect.utils.ModUtils;
import me.shedaniel.architectury.event.events.client.ClientRawInputEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
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

		ClientRawInputEvent.KEY_PRESSED.register(this::onKeyInput);
    }

	private InteractionResult onKeyInput(Minecraft minecraft, int keyCode, int scanCode, int action, int modifiers) {
		if (ModUtils.hasActiveGui()) { return InteractionResult.PASS; }

		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 292)) { return InteractionResult.PASS; }
		
		if (mPickBlockKB.matches(keyCode, scanCode) && mPickBlockKB.consumeClick()) {
			KeyMapping.click(((KeyMappingAccessor)minecraft.options.keyPickItem).getActualKey());
		}
		return InteractionResult.PASS;
    }
}

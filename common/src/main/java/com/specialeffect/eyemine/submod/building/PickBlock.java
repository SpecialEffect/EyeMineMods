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

package com.specialeffect.eyemine.submod.building;

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
				"key.eyemine.pick_block",                  // this needs to be a unique name
				Type.KEYSYM,                               // this is always KEYSYM
				GLFW.GLFW_KEY_KP_2,                        // this selects the default key. try autocompleting GLFW.GLFW_KEY... to see more options
				"category.eyemine.category.eyegaze_common" // this sets the translation key for the name of the category in the controls list 
				                                           // (we use eyegaze_common, eyegaze_extra and eyegaze_settings depending on the mod)
		));

		ClientRawInputEvent.KEY_PRESSED.register(this::onKeyInput);
    }

	private InteractionResult onKeyInput(Minecraft minecraft, int keyCode, int scanCode, int action, int modifiers) {
		if (ModUtils.hasActiveGui()) { return InteractionResult.PASS; }

		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 292)) { return InteractionResult.PASS; }
		
		if (mPickBlockKB.matches(keyCode, scanCode) && mPickBlockKB.consumeClick()) {
			// When key is pressed, trigger the "pick block" key
			// (this mod is necessary since the default key binding is a mouse action, but we want a key shortcut)
			KeyMapping pickBlockKey = minecraft.options.keyPickItem;
			KeyMapping.click(((KeyMappingAccessor)pickBlockKey).getActualKey());
		}
		return InteractionResult.PASS;
    }
}

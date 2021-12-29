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

package com.specialeffect.eyemine.submod.mining;

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
import org.lwjgl.glfw.GLFW;

public class DwellMine extends DwellAction {
	public final String MODID = "dwellbuild";
	
	public DwellMine() {
		super("DWELL MINING", 15); 
	}
		
	private static KeyMapping mDwellMineKB;
	private static KeyMapping mDwellMineOnceKB;

	public void onInitializeClient() {
		// Register key bindings
		Keybindings.keybindings.add(mDwellMineKB = new KeyMapping(
				"key.eyemine.toggle_dwell",
				Type.KEYSYM,
				GLFW.GLFW_KEY_KP_6,
				"category.eyemine.category.eyegaze_extra" // The translation key of the keybinding's category.
		));

		Keybindings.keybindings.add(mDwellMineOnceKB = new KeyMapping(
				"key.eyemine.dwell_once",
				Type.KEYSYM,
				GLFW.GLFW_KEY_KP_8,
				"category.eyemine.category.eyegaze_extra" // The translation key of the keybinding's category.
		));

		ClientTickEvent.CLIENT_PRE.register(this::onClientTick);
		ClientRawInputEvent.KEY_PRESSED.register(this::onKeyInput);

		//Initialize variables
		super.onInitializeClient();
	}

	@Override
	public void performAction(TargetBlock block) {
		final KeyMapping attackBinding = Minecraft.getInstance().options.keyAttack;
		KeyMapping.click(((KeyMappingAccessor)attackBinding).getActualKey());
	}

	private EventResult onKeyInput(Minecraft minecraft, int keyCode, int scanCode, int action, int modifiers) {
		if (ModUtils.hasActiveGui()) { return EventResult.pass(); }

		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 292)) { return EventResult.pass(); }

		if (mDwellMineKB.matches(keyCode, scanCode) && mDwellMineKB.consumeClick()) {
			if (mDwelling) {
				// Turn off dwell mine
				this.setDwelling(false);
				ModUtils.sendPlayerMessage("Dwell mining: OFF");
			}
			else {
				// Turn on dwell mine
				this.setDwelling(true);
				ModUtils.sendPlayerMessage("Dwell mining: ON");
			}
			return EventResult.pass();
		}

		if (mDwellMineOnceKB.matches(keyCode, scanCode) && mDwellMineOnceKB.consumeClick()) {
			this.dwellOnce();
			return EventResult.pass();
		}
		return EventResult.pass();
	}
}

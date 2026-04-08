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
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.platform.InputConstants.Type;
import com.specialeffect.eyemine.client.Keybindings;
import com.specialeffect.eyemine.mixin.KeyMappingAccessor;
import com.specialeffect.eyemine.submod.SubMod;
import com.specialeffect.eyemine.submod.KeyInputUtil;
import com.specialeffect.utils.ModUtils;
import com.specialeffect.eyemine.event.EventResult;
import com.specialeffect.eyemine.event.EyeMineEvents;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.glfw.GLFW;

public class SwapMinePlace extends SubMod {
	public void onInitializeClient() {
		// Register key bindings
		Keybindings.keybindings.add(mSwapKB = new KeyMapping(
				"key.eyemine.swap_mine_place",
				Type.KEYSYM,
				GLFW.GLFW_KEY_F10,
				Keybindings.EYEGAZE_EXTRA // The translation key of the keybinding's category.
		));

		EyeMineEvents.KEY_PRESSED.register(this::onKeyInput);
		EyeMineEvents.RENDER_HUD.register(this::onRenderGameOverlayEvent);
	}

	private static KeyMapping mSwapKB;

	private EventResult onKeyInput(Minecraft minecraft, int keyCode, int scanCode, int action, int modifiers) {
		if (KeyInputUtil.shouldIgnoreKeyInput(minecraft)) {
			return EventResult.pass();
		}

		if (mSwapKB.matches(new net.minecraft.client.input.KeyEvent(keyCode, scanCode, modifiers)) && mSwapKB.consumeClick()) {

			Key attackInput = ((KeyMappingAccessor) Minecraft.getInstance().options.keyAttack).getActualKey();
			Key useInput = ((KeyMappingAccessor) Minecraft.getInstance().options.keyUse).getActualKey();

			Minecraft.getInstance().options.keyAttack.setKey(useInput);
			Minecraft.getInstance().options.keyUse.setKey(attackInput);

			// It's important to force a reload
			Minecraft.getInstance().options.save();
			Minecraft.getInstance().options.load();

			ModUtils.sendPlayerMessage("Swapping mine and place keys");

		}
		return EventResult.pass();
	}

	public void onRenderGameOverlayEvent(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
		// If these are swapped, show a warning message
		KeyMapping attackBinding = Minecraft.getInstance().options.keyAttack;
		KeyMapping useBinding = Minecraft.getInstance().options.keyUse;

		if (attackBinding.isDefault() || useBinding.isDefault()) {
			return;
		} else {
			Key attackDefault = attackBinding.getDefaultKey();
			Key useDefault = useBinding.getDefaultKey();

			// if there's a straight-up swap, show message
			if (((KeyMappingAccessor) attackBinding).getActualKey().getValue() == useDefault.getValue() &&
					((KeyMappingAccessor) useBinding).getActualKey().getValue() == attackDefault.getValue()) {
				Minecraft mc = Minecraft.getInstance();
				int w = mc.getWindow().getGuiScaledWidth();
				int h = mc.getWindow().getGuiScaledHeight();

				String msg1 = "Mine / place";
				int msg1width = mc.font.width(msg1);
				String msg2 = "are swapped";
				int msg2width = mc.font.width(msg2);

				int delta = (msg1width - msg2width) / 2;
				final Font font = mc.font;

				guiGraphics.text(font, msg1, w - msg2width - delta - 10, h - 22, 0xffFFFFFF);
				guiGraphics.text(font, msg2, w - msg2width - 10, h - 12, 0xffFFFFFF);

			}
		}
	}
}

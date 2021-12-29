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

package com.specialeffect.eyemine.submod.movement;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;
import com.specialeffect.eyemine.client.Keybindings;
import com.specialeffect.eyemine.client.gui.crosshair.StateOverlay;
import com.specialeffect.eyemine.platform.EyeMineConfig;
import com.specialeffect.eyemine.submod.IConfigListener;
import com.specialeffect.eyemine.submod.SubMod;
import com.specialeffect.utils.ModUtils;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientRawInputEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import org.lwjgl.glfw.GLFW;

public class AutoJump extends SubMod implements IConfigListener {
	public final String MODID = "autojump";

	public static KeyMapping autoJumpKeyBinding;

	private boolean mAutoJumpDisabled = false;
	private boolean mDoingAutoJump = true;
	private int mIconIndex;

	public void onInitializeClient() {
		// Register key bindings
		Keybindings.keybindings.add(autoJumpKeyBinding = new KeyMapping(
				"key.eyemine.toggle_auto_jump",
				Type.KEYSYM,
				GLFW.GLFW_KEY_J,
				"category.eyemine.category.eyegaze_common" // The translation key of the keybinding's category.
		));

		// Register an icon for the overlay
		mIconIndex = StateOverlay.registerTextureLeft("eyemine:textures/icons/jump.png");

		ClientTickEvent.CLIENT_PRE.register(this::onClientTick);
		ClientRawInputEvent.KEY_PRESSED.register(this::onKeyInput);
	}
	
	private void updateSettings(boolean autoJump) {
		Options options = Minecraft.getInstance().options;
		if(options != null) {
			options.autoJump = autoJump;
			options.save();
			options.load();
		}
	}

	@Override
	public void syncConfig() {
		this.mAutoJumpDisabled = EyeMineConfig.getDisableAutoJumpFixes();
		this.mDoingAutoJump = EyeMineConfig.getDefaultDoAutoJump();
		this.updateSettings(mDoingAutoJump);
		StateOverlay.setStateLeftIcon(mIconIndex, mDoingAutoJump);
	}

    public void onClientTick(Minecraft minecraft) {
		LocalPlayer player = minecraft.player;
    	if (player != null) {
			// We can't rely solely on the vanilla autojump implementation,
			// since there are a few scenarios where it doesn't work correctly, see
			//
			// 
			// We'll keep it in sync though so that keyboard-play is consistent
			// with our autojump state (if you're moving with the keyboard you
			// get visually-nicer autojump behaviour).
			if(!mAutoJumpDisabled) {
				if (mDoingAutoJump) {
					player.maxUpStep = 1.0f;
				}
				else {
					player.maxUpStep = 0.6f;
				}
			}
    	}
    }

	private EventResult onKeyInput(Minecraft minecraft, int keyCode, int scanCode, int action, int modifiers) {
		if (ModUtils.hasActiveGui()) { return EventResult.pass(); }

		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 292)) { return EventResult.pass(); }

		if (!mAutoJumpDisabled && autoJumpKeyBinding.matches(keyCode, scanCode) && autoJumpKeyBinding.consumeClick()) {
			mDoingAutoJump = !mDoingAutoJump;
			this.updateSettings(mDoingAutoJump);
			StateOverlay.setStateLeftIcon(mIconIndex, mDoingAutoJump);			
			ModUtils.sendPlayerMessage("Auto jump: " + (mDoingAutoJump ? "ON" : "OFF"));
		}
		return EventResult.pass();
	}
}

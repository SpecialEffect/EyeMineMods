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

package com.specialeffect.eyemine.submod.movement;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;
import com.specialeffect.eyemine.client.Keybindings;
import com.specialeffect.eyemine.client.gui.crosshair.StateOverlay;
import com.specialeffect.eyemine.mixin.KeyMappingAccessor;
import com.specialeffect.eyemine.submod.SubMod;
import com.specialeffect.utils.ModUtils;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientRawInputEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.lwjgl.glfw.GLFW;

public class Swim extends SubMod {
	public final String MODID = "swimtoggle";

	private static KeyMapping mSwimKB;
	private static boolean mSwimmingTurnedOn = true;

	public Swim() {
	}

	public void onInitializeClient() {
		// Register key bindings
		Keybindings.keybindings.add(mSwimKB = new KeyMapping(
				"key.eyemine.toggle_swimming",
				Type.KEYSYM,
				GLFW.GLFW_KEY_V,
				"category.eyemine.category.eyegaze_extra" // The translation key of the keybinding's category.
		));

		// Register an icon for the overlay
		mIconIndex = StateOverlay.registerTextureLeft("eyemine:textures/icons/swim.png");

		StateOverlay.setStateLeftIcon(mIconIndex, mSwimmingTurnedOn);

		ClientTickEvent.CLIENT_PRE.register(this::onClientTick);
		ClientRawInputEvent.KEY_PRESSED.register(this::onKeyInput);
	}

	public static void stopActivelySwimming() {
		final KeyMapping swimBinding =
				Minecraft.getInstance().options.keyJump;
		KeyMapping.set(((KeyMappingAccessor) swimBinding).getActualKey(), false);
		jumpkeyTimer = jumpkeyCooldown;
	}

	public static boolean isSwimmingOn() {
		return mSwimmingTurnedOn;
	}

	private int mIconIndex;
	private boolean mJumpKeyOverridden = false;

	private static int jumpkeyTimer = 0;
	private static int jumpkeyCooldown = 6;

	private boolean isPlayerInAir(LocalPlayer player) {
		Level level = Minecraft.getInstance().level;
		BlockPos playerPos = player.blockPosition();
		return level.getBlockState(playerPos).isAir();
	}

	public void onClientTick(Minecraft event) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player != null) {
			if (jumpkeyTimer > 0) {
				jumpkeyTimer -= 1;
			}

			if (isSwimmingOn()) {
				final KeyMapping swimBinding = minecraft.options.keyJump;

				// Switch on swim key when in water
				if ((player.isInWater() || player.isInLava()) &&
						!swimBinding.isDown() &&
						jumpkeyTimer == 0) {
					KeyMapping.set(((KeyMappingAccessor) swimBinding).getActualKey(), true);
					mJumpKeyOverridden = true;
				}

				// Switch off when on land
				else if ((player.isOnGround() || isPlayerInAir(player)) &&
						swimBinding.isDown()) {

					if (mJumpKeyOverridden) {
						KeyMapping.set(((KeyMappingAccessor) swimBinding).getActualKey(), false);
						mJumpKeyOverridden = false;
						// don't turn back on until timer finished - otherwise we can trigger 'fly'.
						jumpkeyTimer = jumpkeyCooldown;
					}
				}
			}
		}
	}

	private EventResult onKeyInput(Minecraft minecraft, int keyCode, int scanCode, int action, int modifiers) {
		if (ModUtils.hasActiveGui()) {
			return EventResult.pass();
		}

		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 292)) {
			return EventResult.pass();
		}

		if (mSwimKB.matches(keyCode, scanCode) && mSwimKB.consumeClick()) {
			final KeyMapping swimBinding = minecraft.options.keyJump;

			mSwimmingTurnedOn = !mSwimmingTurnedOn;

			StateOverlay.setStateLeftIcon(mIconIndex, mSwimmingTurnedOn);

			if (!mSwimmingTurnedOn) {
				KeyMapping.set(((KeyMappingAccessor) swimBinding).getActualKey(), false);
			}

			ModUtils.sendPlayerMessage("Swimming: " + (mSwimmingTurnedOn ? "ON" : "OFF"));
		}
		return EventResult.pass();
	}
}

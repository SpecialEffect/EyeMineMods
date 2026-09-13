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
import com.specialeffect.eyemine.client.gui.crosshair.StateOverlay;
import com.specialeffect.eyemine.mixin.KeyMappingAccessor;
import com.specialeffect.eyemine.packets.messages.AddItemToHotbar;
import com.specialeffect.eyemine.platform.EyeMineConfig;
import com.specialeffect.eyemine.submod.IConfigListener;
import com.specialeffect.eyemine.submod.KeyInputUtil;
import com.specialeffect.eyemine.submod.SubMod;
import com.specialeffect.eyemine.submod.KeyInputUtil;
import com.specialeffect.eyemine.submod.mining.ContinuouslyMine;
import com.specialeffect.eyemine.submod.KeyInputUtil;
import com.specialeffect.eyemine.utils.MouseHelper;
import com.specialeffect.utils.ModUtils;
import com.specialeffect.eyemine.event.EventResult;
import com.specialeffect.eyemine.event.EyeMineEvents;
import com.specialeffect.eyemine.packets.NetworkService;
import com.specialeffect.eyemine.platform.Services;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.phys.EntityHitResult;
import org.lwjgl.glfw.GLFW;

public class ContinuouslyAttack extends SubMod implements IConfigListener {
	public final String MODID = "continuouslyattack";

	private boolean mAutoSelectSword = true;
	private static int mIconIndex;
	private static KeyMapping mAttackKB;

	private boolean mWaitingForSword = false;
	public static boolean mIsAttacking = false;

	public void onInitializeClient() {
		// Register key bindings
		Keybindings.keybindings.add(mAttackKB = new KeyMapping(
				"key.eyemine.continious_attack",
				Type.KEYSYM,
				GLFW.GLFW_KEY_R,
				Keybindings.EYEGAZE_COMMON // The translation key of the keybinding's category.
		));

		// Register an icon for the overlay
		mIconIndex = StateOverlay.registerTextureRight("eyemine:textures/icons/attack.png");

		EyeMineEvents.CLIENT_TICK.register(this::onClientTick);
		EyeMineEvents.KEY_PRESSED.register(this::onKeyInput);
	}

	@Override
	public void syncConfig() {
		mAutoSelectSword = EyeMineConfig.getAutoSelectSword();
	}

	public static void stop() {
		mIsAttacking = false;
		StateOverlay.setStateRightIcon(mIconIndex, false);
	}

	public void onClientTick(Minecraft minecraft) {
		LocalPlayer player = minecraft.player;
		if (player != null) {
			// Pause attacking when gaze is at keyboard area or outside window
			// Per EyeGazeGirl: "toggle mining meant to stop too" (applies to attack as well)
			boolean gazeAtKeyboard = MouseHelper.isGazeBelowHotbar || MouseHelper.isGazeOutsideWindow;
			if (mIsAttacking && gazeAtKeyboard) {
				return; // Skip attacking while gaze is at keyboard
			}

			if (mIsAttacking) {
				if (player.isCreative() &&
						mAutoSelectSword) {
					boolean haveSword = chooseWeapon(player.getInventory());
					if (haveSword) {
						mWaitingForSword = false;
					} else if (!mWaitingForSword) {
						requestCreateSword();
						mWaitingForSword = true;
					}
				}

				// Get entity being looked at
				EntityHitResult entityResult = ModUtils.getMouseOverEntity();
				boolean recharging = false;
				if (null != entityResult) {
					Entity entity = entityResult.getEntity();

					// Attack locally and on server
					if (player.getAttackStrengthScale(0) > 0.95) {
						// Server message won't work unless EyeMine is installed remotely
						// Fallback to pressing the attack key
						// FIXME: it's possible this is sufficient and simpler than the previous strategy -
						// but haven't tested enough to replace wholesale at this point
						if (EyeMineConfig.getServerCompatibilityMode()) {
							final KeyMapping attackBinding = Minecraft.getInstance().options.keyAttack;
							KeyMapping.click(((KeyMappingAccessor) attackBinding).getActualKey());
						} else {
							// Use gameMode.attack which handles both local and server-side
							MultiPlayerGameMode gameMode = Minecraft.getInstance().gameMode;
							if (gameMode != null) {
								gameMode.attack(player, entity);
							}
						}
					} else {
						recharging = true;
					}
				}

				// When attacking programmatically, the player doesn't swing unless
				// an attackable-block is in reach. We fix that here, for better feedback.
				if (!player.swinging && !recharging) {
					player.swing(InteractionHand.MAIN_HAND);
				}
			}
		}
	}

	private EventResult onKeyInput(Minecraft minecraft, int keyCode, int scanCode, int action, int modifiers) {
		if (KeyInputUtil.shouldIgnoreKeyInput(minecraft)) {
			return EventResult.pass();
		}

		if (mAttackKB.matches(new net.minecraft.client.input.KeyEvent(keyCode, scanCode, modifiers)) && mAttackKB.consumeClick()) {
			mIsAttacking = !mIsAttacking;
			StateOverlay.setStateRightIcon(mIconIndex, mIsAttacking);

			// Don't allow mining *and* attacking at same time
			ContinuouslyMine.stop();
		}
		return EventResult.pass();
	}

	//returns true if successful
	private boolean chooseWeapon(Inventory inventory) {

		// In creative mode, we can either select a sword from the hotbar
		// or just rustle up a new one
		if (inventory.getSelectedItem().is(ItemTags.SWORDS)) {
			return true;
		} else {
			int swordId = ModUtils.findItemInHotbar(inventory, (item) -> item.builtInRegistryHolder().is(ItemTags.SWORDS));
			if (swordId > -1) {
				inventory.setSelectedSlot(swordId);
				return true;
			} else {
				return false;
			}
		}
	}

	private void requestCreateSword() {
		// Ask server to put new item in hotbar
		Services.NETWORK.sendToServer(new AddItemToHotbar(new ItemStack(Items.DIAMOND_SWORD)));
	}
}

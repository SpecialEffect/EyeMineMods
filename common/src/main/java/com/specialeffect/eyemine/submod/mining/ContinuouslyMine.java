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
import com.specialeffect.eyemine.client.gui.crosshair.StateOverlay;
import com.specialeffect.eyemine.packets.PacketHandler;
import com.specialeffect.eyemine.packets.messages.AddItemToHotbar;
import com.specialeffect.eyemine.platform.EyeMineConfig;
import com.specialeffect.eyemine.submod.IConfigListener;
import com.specialeffect.eyemine.submod.SubMod;
import com.specialeffect.eyemine.submod.misc.ContinuouslyAttack;
import com.specialeffect.eyemine.submod.mouse.MouseHandlerMod;
import com.specialeffect.utils.ModUtils;
import me.shedaniel.architectury.event.events.client.ClientRawInputEvent;
import me.shedaniel.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import org.lwjgl.glfw.GLFW;

public class ContinuouslyMine extends SubMod implements IConfigListener {
	public final String MODID = "continuouslydestroy";

	private static int mIconIndex;
	private static KeyMapping mDestroyKB;
	private boolean mAutoSelectTool = true;
	private boolean mWaitingForPickaxe = false;
	private int miningTimer = 0;
	private int miningCooldown = 10; // update from config

	private static boolean mIsAttacking = false;
	private boolean mMouseEventLastTick = false;

	private static ContinuouslyMine instance;

	public ContinuouslyMine() {
		instance = this;
	}

	public void onInitializeClient() {
		// Register key bindings
		Keybindings.keybindings.add(mDestroyKB = new KeyMapping(
				"key.eyemine.continious_destroy",
				Type.KEYSYM,
				GLFW.GLFW_KEY_M,
				"category.eyemine.category.eyegaze_common" // The translation key of the keybinding's category.
		));

		// Register an icon for the overlay
		mIconIndex = StateOverlay.registerTextureRight("eyemine:textures/icons/mine.png");

		ClientTickEvent.CLIENT_PRE.register(this::onClientTick);
		ClientRawInputEvent.KEY_PRESSED.register(this::onKeyInput);

		//Initialize variables
		this.syncConfig();
	}

	@Override
	public void syncConfig() {
		mAutoSelectTool = EyeMineConfig.getAutoSelectTool();
		miningCooldown = EyeMineConfig.getTicksBetweenMining();
	}

	public static void stop() {
		mIsAttacking = false;

		final KeyMapping attackBinding = Minecraft.getInstance().options.keyAttack;
		KeyMapping.set(attackBinding.getDefaultKey(), false);

		StateOverlay.setStateRightIcon(mIconIndex, false);
	}

	public void onClientTick(Minecraft event) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player != null) {
			final KeyMapping attackBinding =
					Minecraft.getInstance().options.keyAttack;

			if (mIsAttacking) {
				if (player.isCreative()) {
					// always select tool - first time we might need to ask server to
					// create a new one
					if (mAutoSelectTool) {
						boolean havePickaxe = choosePickaxe(player.inventory);
						if (havePickaxe) {
							mWaitingForPickaxe = false;
						}
						else if(!mWaitingForPickaxe)
						{
							requestCreatePickaxe();
							mWaitingForPickaxe = true;
						}
					}

					// Timer to limit excessive destruction
					// TODO: should we use wallclock time instead of ticks?
					if (miningTimer > 0) {
						miningTimer -= 1;
					}

					// Set mouse in correct state - shouldn't attack unless there's an
					// accompanying mouse movement.
					if (MouseHandlerMod.hasPendingEvent() || mMouseEventLastTick) {
						if (miningTimer == 0) {
							System.out.println("attack");
							KeyMapping.click(attackBinding.getDefaultKey());
							if (player.isCreative()) {
								miningTimer = miningCooldown;
							}
						}
						else {
							if (player.attackAnim == 0) {
								System.out.println("swing");
								player.swing(InteractionHand.MAIN_HAND);
							}
						}
					}
				} else {
					// Survival mode
					// Set mouse in correct state - shouldn't attack unless there's an
					// accompanying mouse movement.
					if (MouseHandlerMod.hasPendingEvent() || mMouseEventLastTick) {
						KeyMapping.set(attackBinding.getDefaultKey(), true);
					}
					else {
						KeyMapping.set(attackBinding.getDefaultKey(), false);
					}
				}
			}

			// Remember mouse status so we can have one tick of grace
			// (necessary if minecraft running faster than eye tracker).
			mMouseEventLastTick = MouseHandlerMod.hasPendingEvent();
		}
	}

	private InteractionResult onKeyInput(Minecraft minecraft, int keyCode, int scanCode, int action, int modifiers) {
		if (ModUtils.hasActiveGui()) { return InteractionResult.PASS; }

		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 292)) { return InteractionResult.PASS; }

		if (mDestroyKB.consumeClick()) {
			mIsAttacking = !mIsAttacking;
			StateOverlay.setStateRightIcon(mIconIndex, mIsAttacking);

			LocalPlayer player = Minecraft.getInstance().player;
			if (player != null) {
				final KeyMapping attackBinding =
						Minecraft.getInstance().options.keyAttack;

				// In creative mode we handle throttled attacking in onClientTick
				if (!player.isCreative()) {
					// In survival, we hold down the attack binding for continuous mining
					KeyMapping.set(attackBinding.getDefaultKey(), mIsAttacking);
				}
			}

			// Don't allow mining *and* attacking at same time
			if (mIsAttacking) {
				ContinuouslyAttack.stop();
			}
		}
		return InteractionResult.PASS;
	}

	// returns true if successful
	static boolean choosePickaxe(Inventory inventory) {

		// In creative mode, we can either select a pickaxe from the hotbar
		// or just rustle up a new one
		if (inventory.getSelected().getItem() instanceof PickaxeItem) {
			return true;
		}
		else {
			int pickaxeId = ModUtils.findItemInHotbar(inventory, (item -> item instanceof PickaxeItem));
			if (pickaxeId > -1) {
				inventory.selected = pickaxeId;
				return true;
			}
			else {
				return false;
			}
		}
	}

	static void requestCreatePickaxe() {
		// Ask server to put new item in hotbar
		PacketHandler.CHANNEL.sendToServer(new AddItemToHotbar(new ItemStack(Items.DIAMOND_PICKAXE)));
	}
}

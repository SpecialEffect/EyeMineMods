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
import com.specialeffect.eyemine.callbacks.DelayedOnLivingCallback;
import com.specialeffect.eyemine.callbacks.OnLivingCallback;
import com.specialeffect.eyemine.client.Keybindings;
import com.specialeffect.eyemine.packets.PacketHandler;
import com.specialeffect.eyemine.packets.messages.AddItemToHotbar;
import com.specialeffect.eyemine.submod.SubMod;
import com.specialeffect.utils.ModUtils;
import me.shedaniel.architectury.event.events.client.ClientScreenInputEvent;
import me.shedaniel.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import org.lwjgl.glfw.GLFW;

import java.util.Iterator;
import java.util.LinkedList;

public class AutoPillar extends SubMod {
	public final String MODID = "autopillar";
	
	public static AutoPillar instance;
	
	public static KeyMapping autoPlaceKeyBinding;

	private LinkedList<OnLivingCallback> mOnLivingQueue;

	public AutoPillar() {
		instance = this;
	}

	public void onInitializeClient() {
		mOnLivingQueue = new LinkedList<>();

		// Register key bindings
		Keybindings.keybindings.add(autoPlaceKeyBinding = new KeyMapping(
				"key.eyemine.pillar",
				Type.KEYSYM,
				GLFW.GLFW_KEY_0,
				"category.eyemine.category.eyegaze_extra" // The translation key of the keybinding's category.
		));

		ClientTickEvent.CLIENT_PRE.register(this::onClientTick);
		ClientScreenInputEvent.KEY_PRESSED_POST.register(this::onKeyInput);
	}

	private float lastPlayerPitch;

	public void onClientTick(Minecraft event) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (null != player) {
			synchronized (mOnLivingQueue) {
				lastPlayerPitch = player.xRot;
			}

			// Process any events which were queued by key events
			synchronized (mOnLivingQueue) {
				Iterator<OnLivingCallback> it = mOnLivingQueue.iterator();
				while (it.hasNext()) {
					OnLivingCallback item = it.next();
					item.onClientTick(event);
					if (item.hasCompleted()) {
						it.remove();
					}
				}
			}
		}
	}

	protected void queueOnLivingCallback(OnLivingCallback onLivingCallback) {
		synchronized (mOnLivingQueue) {
			mOnLivingQueue.add(onLivingCallback);
		}
	}

	public InteractionResult onKeyInput(Minecraft minecraft, Screen screen, int keyCode, int scanCode, int modifiers) {
		if (ModUtils.hasActiveGui()) { return InteractionResult.FAIL; }

		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 292)) { return InteractionResult.FAIL; }

		// Auto place is implemented as:
		// - Make sure you're holding a block (in creative mode; in survival you're on your own)
		// - Next onLiving tick: look at floor, jump, place current item below
		// - next few ticks, gradually reset view
		// Technically there is no need to change player's view, but the user experience
		// is weird if you don't (you don't really know what just happened).
		if (autoPlaceKeyBinding.consumeClick()) {
			float origPitchTemp;
			synchronized (mOnLivingQueue) {
				origPitchTemp = lastPlayerPitch;
			}
			final float origPitch = origPitchTemp;
			final float pillarPitch = 85; // 90 = look straight down
			final int numTicksReset = 5;

			final float deltaPitch = (pillarPitch - origPitch) / numTicksReset;

			this.queueOnLivingCallback(new DelayedOnLivingCallback(delayedEvent -> {
				Minecraft mc = Minecraft.getInstance();
				LocalPlayer player = mc.player;

				player.connection.send(new ServerboundPlayerInputPacket(player.xxa, player.zza, true, player.input.shiftKeyDown));
				player.jumpFromGround();

				player.xRot = 90;
			}, 1));

			for(int i = 3; i <= 6; i++) {
				this.queueOnLivingCallback(new DelayedOnLivingCallback(delayedEvent -> {
					Minecraft mc = Minecraft.getInstance();
					LocalPlayer player = mc.player;
					ClientLevel level = (ClientLevel)player.level;
					// It's important to make sure we're approximately - but not
					// exactly - centred
					// on a block here, so that the block always ends up under
					// us (if you do this
					// with integer positions you often find your position
					// alternating between 2 blocks)
					// Also look down, purely for effect.

					player.xRot = 90;
					if(!player.isOnGround() && player.xRot == 90) {
						if(mc.hitResult instanceof BlockHitResult) {
							BlockHitResult blockHitResult = (BlockHitResult) mc.hitResult;
							if(blockHitResult.getBlockPos().getY() < player.getY()) {
								InteractionResult result = mc.gameMode.useItemOn(player, level, InteractionHand.MAIN_HAND, blockHitResult); //TODO: Test if this actually works on server
								// Make sure we get the animation
								player.swing(InteractionHand.MAIN_HAND);
							}
						}
					}
				}, i));
			}

			// gradually move head back up to original pitch
			for (int i = 1; i < numTicksReset + 1; i++) {
				final int j = i;
				this.queueOnLivingCallback(new DelayedOnLivingCallback(delayedEvent -> {
					Minecraft mc = Minecraft.getInstance();
					LocalPlayer player = mc.player;

					player.xRot = pillarPitch - deltaPitch * j;
				}, 10 + 2 * j));
			}
		}
		return InteractionResult.SUCCESS;
	}

	//Currently goes unused
	static void chooseBlock(Inventory inventory) {
		// In creative mode, we can either select a block from the hotbar
		// or just rustle up a new one
		int blockId = ModUtils.findItemInHotbar(inventory, (item -> item instanceof BlockItem));
		if (blockId > -1) {
			inventory.selected = blockId;
		} else {
			// Ask server to put new item in hotbar
			PacketHandler.CHANNEL.sendToServer(new AddItemToHotbar(new ItemStack(Blocks.GRASS)));
		}
	}
}

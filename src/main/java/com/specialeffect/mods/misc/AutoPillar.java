/**
 * Copyright (C) 2016 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.mods.misc;

import java.util.Iterator;
import java.util.LinkedList;

import org.lwjgl.glfw.GLFW;

import com.specialeffect.callbacks.DelayedOnLivingCallback;
import com.specialeffect.callbacks.IOnLiving;
import com.specialeffect.callbacks.OnLivingCallback;
import com.specialeffect.messages.AddItemToHotbar;
import com.specialeffect.messages.JumpMessage;
import com.specialeffect.messages.SetPositionAndRotationMessage;
import com.specialeffect.messages.UseItemAtPositionMessage;
import com.specialeffect.mods.ChildMod;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class AutoPillar extends ChildMod {
	public final String MODID = "autopillar";
	
	public static AutoPillar instance;
	
	public static KeyBinding autoPlaceKeyBinding;

	private LinkedList<OnLivingCallback> mOnLivingQueue;

	public AutoPillar() {
		instance = this;
	}
	
	public void setup(final FMLCommonSetupEvent event) {

		mOnLivingQueue = new LinkedList<OnLivingCallback>();

		// setup channel for comms
		this.setupChannel(MODID, 1);

		int id = 0;
		channel.registerMessage(id++, UseItemAtPositionMessage.class, UseItemAtPositionMessage::encode,
				UseItemAtPositionMessage::decode, UseItemAtPositionMessage.Handler::handle);

		channel.registerMessage(id++, AddItemToHotbar.class, AddItemToHotbar::encode, AddItemToHotbar::decode,
				AddItemToHotbar.Handler::handle);

		channel.registerMessage(id++, SetPositionAndRotationMessage.class, SetPositionAndRotationMessage::encode,
				SetPositionAndRotationMessage::decode, SetPositionAndRotationMessage.Handler::handle);

		channel.registerMessage(id++, JumpMessage.class, JumpMessage::encode, JumpMessage::decode,
				JumpMessage.Handler::handle);

		// Register key bindings
		autoPlaceKeyBinding = new KeyBinding("Jump and place block below", GLFW.GLFW_KEY_0,
				CommonStrings.EYEGAZE_EXTRA);
		ClientRegistry.registerKeyBinding(autoPlaceKeyBinding);

	}

	private float lastPlayerPitch;

	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {
		PlayerEntity player = Minecraft.getInstance().player;
		if (null != player && event.phase == TickEvent.Phase.START) {
			synchronized (mOnLivingQueue) {
				this.lastPlayerPitch = player.rotationPitch;
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

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {

		if (ModUtils.hasActiveGui()) { return; }	    
	    if (event.getAction() != GLFW.GLFW_PRESS) { return; }

		// Auto place is implemented as:
		// - Make sure you're holding a block (in creative mode; in survival you're on
		// your own)
		// - Next onLiving tick: look at floor, jump, place current item below
		// - next few ticks, gradually reset view
		// Technically there is no need to change player's view, but the user
		// experience
		// is weird if you don't (you don't really know what just happened).
		if (autoPlaceKeyBinding.getKey().getKeyCode() == event.getKey()) {
			float origPitchTemp = 0;
			synchronized (mOnLivingQueue) {
				origPitchTemp = this.lastPlayerPitch;
			}
			final float origPitch = origPitchTemp;
			final float pillarPitch = 85; // 90 = look straight down
			final int numTicksReset = 5;

			final float deltaPitch = (pillarPitch - origPitch) / numTicksReset;

			this.queueOnLivingCallback(new DelayedOnLivingCallback(new IOnLiving() {
				@Override
				public void onClientTick(ClientTickEvent event) {
					PlayerEntity player = Minecraft.getInstance().player;
					// It's important to make sure we're approximately - but not
					// exactly - centred
					// on a block here, so that the block always ends up under
					// us (if you do this
					// with integer positions you often find your position
					// alternating between 2 blocks)
					// Also look down, purely for effect.

					double jumpHeight = 1.5;
					double x = Math.floor(player.posX) + 0.4;
					double y = Math.floor(player.posY);
					double z = Math.floor(player.posZ) + 0.4;

					channel.sendToServer(new SetPositionAndRotationMessage(player.getName().toString(), x,
							y + jumpHeight, z, player.rotationYaw, pillarPitch));

					// Ask server to use item
					BlockPos blockPos = new BlockPos(x, y, z);
					channel.sendToServer(new UseItemAtPositionMessage(player, blockPos));

					// Make sure we get the animation
					player.swingArm(Hand.MAIN_HAND);
				}
			}, 1));

			// gradually move head back up to original pitch
			for (int i = 1; i < numTicksReset + 1; i++) {
				final int j = i;
				this.queueOnLivingCallback(new DelayedOnLivingCallback(new IOnLiving() {
					@Override
					public void onClientTick(ClientTickEvent event) {
						PlayerEntity player = Minecraft.getInstance().player;

						channel.sendToServer(new SetPositionAndRotationMessage(player.getName().toString(), player.posX,
								player.posY, player.posZ, player.rotationYaw, pillarPitch - deltaPitch * j));
					}
				}, 1 + 2 * j));
			}
		}
	}

	static void chooseBlock(PlayerInventory inventory) {

		// In creative mode, we can either select a block from the hotbar
		// or just rustle up a new one

		int blockId = ModUtils.findItemInHotbar(inventory, BlockItem.class);
		if (blockId > -1) {
			inventory.currentItem = blockId;
		} else {
			// Ask server to put new item in hotbar
			instance.channel.sendToServer(new AddItemToHotbar(new ItemStack(Blocks.GRASS)));
		}
	}

}

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

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.callbacks.DelayedOnLivingCallback;
import com.specialeffect.callbacks.IOnLiving;
import com.specialeffect.messages.AddItemToHotbar;
import com.specialeffect.messages.JumpMessage;
import com.specialeffect.messages.SetPositionAndRotationMessage;
import com.specialeffect.messages.UseItemAtPositionMessage;
import com.specialeffect.mods.EyeGaze;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.java.games.input.Keyboard;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.SubscribeEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod(AutoPillar.MODID)
public class AutoPillar extends BaseClassWithCallbacks {
	public static final String MODID = "specialeffect.autopillar";
	public static final String NAME = "AutoPillar";

	public static KeyBinding autoPlaceKeyBinding;
	public static KeyBinding openChatKB;

	//FIXME for 1.14 public static SimpleNetworkWrapper network;

	@SubscribeEvent
	@SuppressWarnings("static-access")
	public void preInit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);

		ModUtils.setupModInfo(event, this.MODID, this.NAME, "Add key binding to create pillar, or 'nerd-pole'.");
		ModUtils.setAsParent(event, EyeGaze.MODID);

		//FIXME network = NetworkRegistry.INSTANCE.newSimpleChannel(this.NAME);
		//FIXME network.registerMessage(UseItemAtPositionMessage.Handler.class, UseItemAtPositionMessage.class, 0, Side.SERVER);
		//FIXME network.registerMessage(AddItemToHotbar.Handler.class, AddItemToHotbar.class, 1, Side.SERVER);
		//FIXME network.registerMessage(SetPositionAndRotationMessage.Handler.class, SetPositionAndRotationMessage.class, 2, Side.SERVER);
		//FIXME network.registerMessage(JumpMessage.Handler.class, JumpMessage.class, 3, Side.SERVER);
	}

	@SubscribeEvent
	public void init(FMLInitializationEvent event) {

		// Register key bindings
		autoPlaceKeyBinding = new KeyBinding("Jump and place block below", GLFW.GLFW_KEY_L, CommonStrings.EYEGAZE_EXTRA);
		ClientRegistry.registerKeyBinding(autoPlaceKeyBinding);

	}

	private float lastPlayerPitch;

	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (ModUtils.entityIsMe(event.getEntityLiving())) {
			PlayerEntity player = (PlayerEntity) event.getEntityLiving();
			synchronized (mOnLivingQueue) {
				this.lastPlayerPitch = player.rotationPitch;
			}

			// Process any events which were queued by key events
			this.processQueuedCallbacks(event);
		}
	}

	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {

		// Auto place is implemented as:
		// - Make sure you're holding a block (in creative mode; in survival you're on your own)
		// - Next onLiving tick: look at floor, jump, place current item below
		// - next few ticks, gradually reset view
		// Technically there is no need to change player's view, but the user
		// experience
		// is weird if you don't (you don't really know what just happened).
		if (autoPlaceKeyBinding.isPressed()) {
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
				public void onLiving(LivingUpdateEvent event) {
					// It's important to make sure we're approximately - but not
					// exactly - centred
					// on a block here, so that the block always ends up under
					// us (if you do this
					// with integer positions you often find your position
					// alternating between 2 blocks)
					// Also look down, purely for effect.
					PlayerEntity player = (PlayerEntity) event.getEntityLiving();

					double jumpHeight = 1.5;
					double x = Math.floor(player.posX) + 0.4;
					double y = Math.floor(player.posY);
					double z = Math.floor(player.posZ) + 0.4;
					
					//FIXME AutoPillar.network.sendToServer(
							/*new SetPositionAndRotationMessage(
									player.getName(),
									x, y + jumpHeight, z,
									player.rotationYaw, pillarPitch));*/

					// Ask server to use item
					BlockPos blockPos = new BlockPos(x, y, z);
					//FIXME AutoPillar.network.sendToServer(new UseItemAtPositionMessage(player, blockPos));
					
					// Make sure we get the animation
					player.swingArm(Hand.MAIN_HAND);
				}
			}, 1));

			// gradually move head back up to original pitch
			for (int i = 1; i < numTicksReset+1; i++) {
				final int j = i;
				this.queueOnLivingCallback(new DelayedOnLivingCallback(new IOnLiving() {
					@Override
					public void onLiving(LivingUpdateEvent event) {
						PlayerEntity player = (PlayerEntity) event.getEntityLiving();

						//FIXME AutoPillar.network.sendToServer(
								new SetPositionAndRotationMessage(
										player.getName(), 
										player.posX, player.posY, player.posZ,
										player.rotationYaw,
										pillarPitch - deltaPitch * j));
					}
				}, 1 + 2*j));
			}
		}
	}
	
	static void chooseBlock(PlayerInventory inventory) {
		
		// In creative mode, we can either select a block from the hotbar 
		// or just rustle up a new one

		int blockId = ModUtils.findItemInHotbar(inventory, ItemBlock.class);
		if (blockId > -1) {
			inventory.currentItem = blockId;
		}
		else {
			// Ask server to put new item in hotbar
			// FIXME for 1.14			AutoPillar.network.sendToServer(new AddItemToHotbar(new ItemStack(Blocks.GRASS)));
		}
	}

}

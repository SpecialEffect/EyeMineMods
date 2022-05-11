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
import com.mojang.blaze3d.vertex.PoseStack;
import com.specialeffect.eyemine.client.Keybindings;
import com.specialeffect.eyemine.mixin.KeyMappingAccessor;
import com.specialeffect.eyemine.platform.EyeMineConfig;
import com.specialeffect.eyemine.submod.utils.DwellAction;
import com.specialeffect.eyemine.submod.utils.TargetBlock;
import com.specialeffect.utils.ModUtils;
import me.shedaniel.architectury.event.events.client.ClientRawInputEvent;
import me.shedaniel.architectury.event.events.client.ClientTickEvent;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class UseItem extends DwellAction {

	public UseItem() {
		super("Use item");
	}

	public final String MODID = "useitem";

	public void onInitializeClient() {
		// Register key bindings
		Keybindings.keybindings.add(mUseItemOnceKB = new KeyMapping(
				"key.eyemine.use_item",
				Type.KEYSYM,
				GLFW.GLFW_KEY_KP_0,
				"category.eyemine.category.eyegaze_common" // The translation key of the keybinding's category.
		));

		Keybindings.keybindings.add(mUseItemContinuouslyKB = new KeyMapping(
				"key.eyemine.use_item_continuously",
				Type.KEYSYM,
				GLFW.GLFW_KEY_KP_1,
				"category.eyemine.category.eyegaze_extra" // The translation key of the keybinding's category.
		));

		Keybindings.keybindings.add(mPrevItemKB = new KeyMapping(
				"key.eyemine.select_previous_item",
				Type.KEYSYM,
				GLFW.GLFW_KEY_KP_4,
				"category.eyemine.category.eyegaze_extra" // The translation key of the keybinding's category.
		));

		Keybindings.keybindings.add(mNextItemKB = new KeyMapping(
				"key.eyemine.select_next_item",
				Type.KEYSYM,
				GLFW.GLFW_KEY_KP_5,
				"category.eyemine.category.eyegaze_extra" // The translation key of the keybinding's category.
		));

		ClientTickEvent.CLIENT_PRE.register(this::onClientTick);
		ClientRawInputEvent.KEY_PRESSED.register(this::onKeyInput);

		//Initialize variables
		super.onInitializeClient();
	}

	private static KeyMapping mUseItemOnceKB;
	private static KeyMapping mUseItemContinuouslyKB;
	private static KeyMapping mPrevItemKB;
	private static KeyMapping mNextItemKB;
	
	// State for 'continuously use'
	private boolean mUsingItem = false;
	
	// State for firing bows
	private long lastTime = 0;
	private int bowTime = 2000; //ms
	private int bowCountdown = 0;
	private boolean needBowFire = false;

	public void syncConfig() {
		super.syncConfig();
        this.bowTime = (int) (1000 * EyeMineConfig.getBowDrawTime());
	}

	public void onClientTick(Minecraft minecraft) {
		super.onClientTick(minecraft);
		long time = System.currentTimeMillis();
		long dt = time - lastTime;
		lastTime = time;

		// Behaviour for shootable items (bows)
		if (bowCountdown > 0 ) {
			bowCountdown -= dt;
			if (bowCountdown < 1) {
				// Release bow if count-down complete
				final KeyMapping useItemKeyBinding = Minecraft.getInstance().options.keyUse;
				KeyMapping.set(((KeyMappingAccessor)useItemKeyBinding).getActualKey(), false);

				// If it was a crossbow we'll need to re-click to actually fire it
				Player player = Minecraft.getInstance().player;
				Item item = player.inventory.getSelected().getItem();
				if (item instanceof CrossbowItem) {
					// Crossbows don't fire on mouse-release, they need another 'click' on the next tick to be shot
					needBowFire = true;
				}
			}
		}
		else {
			if (needBowFire) {
				final KeyMapping useItemKeyBinding = Minecraft.getInstance().options.keyUse;
				KeyMapping.click(((KeyMappingAccessor)useItemKeyBinding).getActualKey());
				needBowFire = false;
			}
		}
	}

	private InteractionResult onKeyInput(Minecraft minecraft, int keyCode, int scanCode, int action, int modifiers) {
		if (ModUtils.hasActiveGui()) { return InteractionResult.PASS; }

		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 292)) { return InteractionResult.PASS; }

		final KeyMapping useItemKeyBinding = Minecraft.getInstance().options.keyUse;
		Player player = Minecraft.getInstance().player;

		if (mUseItemContinuouslyKB.matches(keyCode, scanCode) && mUseItemContinuouslyKB.consumeClick()) {
			if (mUsingItem) {
				// Turn off
				mUsingItem = false;
				ModUtils.sendPlayerMessage("Using item: OFF");
				KeyMapping.set(((KeyMappingAccessor)useItemKeyBinding).getActualKey(), mUsingItem);
			}
			else {
				// Turn on continuous-building

				ItemStack itemStack = player.inventory.getSelected();
				if (itemStack.isEmpty()) {
					player.sendMessage(new TextComponent("Nothing in hand to use"), Util.NIL_UUID);
					return InteractionResult.PASS;
				}

				mUsingItem = true;
				KeyMapping.set(((KeyMappingAccessor)useItemKeyBinding).getActualKey(), mUsingItem);

				ModUtils.sendPlayerMessage("Using item: ON");
			}
		} else if (mUseItemOnceKB.matches(keyCode, scanCode) && mUseItemOnceKB.consumeClick()) {

			ItemStack stack = player.inventory.getSelected();
			Item item = stack.getItem();

			// Special case for shootable items
			if (item instanceof CrossbowItem) {
				// Crossbows need charging separately to firing. If already charged, shoot it.
				// Otherwise start chargin.
				if (CrossbowItem.isCharged(stack)) {
					KeyMapping.click(((KeyMappingAccessor)useItemKeyBinding).getActualKey());
				}
				else {
					int crossbowTime = 1500;
					ModUtils.sendPlayerMessage("Firing bow");
					KeyMapping.set(((KeyMappingAccessor)useItemKeyBinding).getActualKey(), true);
					bowCountdown = Math.max(crossbowTime, bowTime);
				}
			}
			else if (item instanceof BowItem) {
				// Bows need charging + firing all in one go
				bowTime = 1500;
				ModUtils.sendPlayerMessage("Firing bow");
				KeyMapping.set(((KeyMappingAccessor)useItemKeyBinding).getActualKey(), true);
				bowCountdown = bowTime;
			}
			else {
				boolean useDwelling = EyeMineConfig.getUseDwellForSingleUseItem();
				if (useDwelling)
					dwellOnce();
				else // shortcut the dwell and act immediately
					this.performAction(null);
			}

		} else if (mPrevItemKB.matches(keyCode, scanCode) && mPrevItemKB.consumeClick()) {
			player.inventory.swapPaint(1);
		} else if (mNextItemKB.matches(keyCode, scanCode) && mNextItemKB.consumeClick()) {
			player.inventory.swapPaint(-1);
		}
		return InteractionResult.PASS;
	}

	public void onRenderGameOverlayEvent(PoseStack poseStack, float partialTicks) {
		super.onRenderGameOverlayEvent(poseStack, partialTicks);

		// If use-item is on, show a warning message
		if (mUsingItem) {
			PoseStack matrixStack = poseStack;
			String msg = "USING";
			Minecraft mc = Minecraft.getInstance();
			int w = mc.getWindow().getGuiScaledWidth();
			int h = mc.getWindow().getGuiScaledHeight();

			int msgWidth = mc.font.width(msg);

			mc.font.drawShadow(matrixStack, msg, (int)(w/2.0) - (int)(msgWidth/2.0), (int)(h/2.0) - 20, 0xffFFFFFF);
		}
	}

	@Override
	public void performAction(TargetBlock block) {
		final KeyMapping useItemKeyBinding = Minecraft.getInstance().options.keyUse;
		KeyMapping.click(((KeyMappingAccessor)useItemKeyBinding).getActualKey());
	}
}

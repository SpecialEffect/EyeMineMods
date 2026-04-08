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

package com.specialeffect.eyemine.submod.mining;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;
import com.specialeffect.eyemine.client.Keybindings;
import com.specialeffect.eyemine.packets.messages.GatherBlockMessage;
import com.specialeffect.eyemine.submod.SubMod;
import com.specialeffect.utils.ModUtils;
import com.specialeffect.eyemine.event.EventResult;
import com.specialeffect.eyemine.event.EyeMineEvents;
import com.specialeffect.eyemine.packets.NetworkService;
import com.specialeffect.eyemine.platform.Services;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class GatherDrops extends SubMod {
	public final String MODID = "gatherdrops";

	private static KeyMapping mGatherKB;

	private static GatherDrops instance;

	public GatherDrops() {
		instance = this;
	}

	public void onInitializeClient() {
		Keybindings.keybindings.add(mGatherKB = new KeyMapping(
				"key.eyemine.gather",
				Type.KEYSYM,
				GLFW.GLFW_KEY_KP_MULTIPLY,
				Keybindings.EYEGAZE_EXTRA // The translation key of the keybinding's category.
		));

		EyeMineEvents.KEY_PRESSED.register(this::onKeyInput);
	}

	private EventResult onKeyInput(Minecraft minecraft, int keyCode, int scanCode, int action, int modifiers) {
		if (ModUtils.hasActiveGui()) {
			return EventResult.pass();
		}

		if (InputConstants.isKeyDown(minecraft.getWindow(), 292)) {
			return EventResult.pass();
		}


		if (mGatherKB.matches(new net.minecraft.client.input.KeyEvent(keyCode, scanCode, modifiers)) && mGatherKB.consumeClick()) {
			LocalPlayer player = minecraft.player;
			gatherBlocks(player);
		}
		return EventResult.pass();
	}

	public static void gatherBlocks(LocalPlayer player) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null) return;

		Vec3 playerPos = Vec3.atLowerCornerOf(player.blockPosition());
		AABB AaBb = new AABB(playerPos.subtract(5, 5, 5), playerPos.add(5, 5, 5));
		ArrayList<ItemEntity> items = (ArrayList<ItemEntity>) level.getEntitiesOfClass(ItemEntity.class, AaBb);

		if (items != null && !items.isEmpty()) {
			LOGGER.debug("gathering " + items.size() + " nearby items");
			// Ask server to move items
			for (ItemEntity itemEntity : items) {
				Services.NETWORK.sendToServer(new GatherBlockMessage(itemEntity.getId()));
			}
		}
	}
}

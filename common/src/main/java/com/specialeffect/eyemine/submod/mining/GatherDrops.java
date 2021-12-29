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
import com.specialeffect.eyemine.packets.PacketHandler;
import com.specialeffect.eyemine.packets.messages.GatherBlockMessage;
import com.specialeffect.eyemine.submod.SubMod;
import com.specialeffect.utils.ModUtils;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientRawInputEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
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
				"category.eyemine.category.eyegaze_extra" // The translation key of the keybinding's category.
		));

		ClientRawInputEvent.KEY_PRESSED.register(this::onKeyInput);
	}

	private EventResult onKeyInput(Minecraft minecraft, int keyCode, int scanCode, int action, int modifiers) {
		if (ModUtils.hasActiveGui()) { return EventResult.pass(); }

		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 292)) { return EventResult.pass(); }


		if (mGatherKB.matches(keyCode, scanCode) && mGatherKB.consumeClick()) {
			LocalPlayer player = minecraft.player;
			gatherBlocks(player);
		}
		return EventResult.pass();
	}

	public static void gatherBlocks(LocalPlayer player) {
		ClientLevel level = Minecraft.getInstance().level;
		BlockPos playerPos = player.blockPosition();
		AABB AaBb = new AABB(playerPos.subtract(new Vec3i(5, 5, 5)),
				playerPos.offset(new Vec3i(5, 5, 5)));
		ArrayList<ItemEntity> items = (ArrayList<ItemEntity>)level.getEntitiesOfClass(ItemEntity.class, AaBb);

		if(items != null && !items.isEmpty()) {
			LOGGER.debug("gathering " + items.size() + " nearby items");
			// Ask server to move items
			for (ItemEntity itemEntity : items) {
                PacketHandler.CHANNEL.sendToServer(new GatherBlockMessage(itemEntity.getId()));
			}
		}
	}
}

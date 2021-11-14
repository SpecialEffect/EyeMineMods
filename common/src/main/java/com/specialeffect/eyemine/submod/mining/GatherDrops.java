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
import com.specialeffect.eyemine.submod.SubMod;
import com.specialeffect.utils.ModUtils;
import me.shedaniel.architectury.event.events.client.ClientScreenInputEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionResult;
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

		ClientScreenInputEvent.KEY_PRESSED_POST.register(this::onKeyInput);
	}

	public InteractionResult onKeyInput(Minecraft minecraft, Screen screen, int keyCode, int scanCode, int modifiers) {
		if (ModUtils.hasActiveGui()) { return InteractionResult.FAIL; }

		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 292)) { return InteractionResult.FAIL; }


		if (mGatherKB.consumeClick()) {
			LocalPlayer player = minecraft.player;
			gatherBlocks(player);
		}
		return InteractionResult.SUCCESS;
	}

	public static void gatherBlocks(LocalPlayer player) {
		ClientLevel level = Minecraft.getInstance().level;
		BlockPos playerPos = player.blockPosition();
		AABB aaBb = new AABB(playerPos.subtract(new Vec3i(5, 5, 5)),
				playerPos.offset(new Vec3i(5, 5, 5)));
		ArrayList<ItemEntity> items = (ArrayList<ItemEntity>)level.getEntitiesOfClass(ItemEntity.class, aaBb);

		if(items != null && !items.isEmpty()) {
			LOGGER.debug("gathering " + items.size() + " nearby items");
			// Ask server to move items
			for (ItemEntity itemEntity : items) {
//                instance.channel.sendToServer(new GatherBlockMessage(items.get(i).getEntityId())); TODO: Figure out what's the best way to do this more friendly
			}
		}
	}

}

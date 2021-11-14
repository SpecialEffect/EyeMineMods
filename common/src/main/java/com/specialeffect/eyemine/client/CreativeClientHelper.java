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

package com.specialeffect.eyemine.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.specialeffect.eyemine.EyeMine;
import com.specialeffect.eyemine.mixin.AbstractContainerScreenAccessor;
import com.specialeffect.inventory.manager.ChestInventoryManager;
import com.specialeffect.inventory.manager.CreativeInventoryManager;
import com.specialeffect.inventory.manager.SurvivalInventoryManager;
import com.specialeffect.utils.ModUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.InteractionResult;

public class CreativeClientHelper {
	public static InteractionResult onKeyInput(Minecraft minecraft, Screen screen, int keyCode, int scanCode, int modifiers) {
		if (ModUtils.hasActiveGui()) { return InteractionResult.PASS; }

		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 292)) { return InteractionResult.PASS; }

		int key = keyCode;
		EyeMine.LOGGER.debug(key);
		Screen currentScreen = Minecraft.getInstance().screen;
		if (currentScreen != null) {
			if (currentScreen instanceof CreativeModeInventoryScreen) {
				CreativeModeInventoryScreen gui = (CreativeModeInventoryScreen)currentScreen;
				AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor)gui;
				CreativeInventoryManager con = CreativeInventoryManager.getInstance(
						accessor.getLeftPos(), accessor.getTopPos(),
						accessor.getXSize(), accessor.getYSize(),
						gui.getSelectedTab(),
						gui.getMenu());
				boolean handled = con.acceptKey(key);
				if (handled) {
					return InteractionResult.PASS;
				}
			}
			else if (currentScreen instanceof ContainerScreen) {
				ContainerScreen gui = (ContainerScreen)currentScreen;
				AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor)gui;
				ChestInventoryManager con = ChestInventoryManager.getInstance(
						accessor.getLeftPos(), accessor.getTopPos(),
						accessor.getXSize(), accessor.getYSize(),
						gui.getMenu());
				con.acceptKey(key);
			}
			else if (currentScreen instanceof InventoryScreen) {
				InventoryScreen gui = (InventoryScreen)currentScreen;
				AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor)gui;
				SurvivalInventoryManager con = SurvivalInventoryManager.getInstance(
						accessor.getLeftPos(), accessor.getTopPos(),
						accessor.getXSize(), accessor.getYSize(), gui.getMenu());

				con.acceptKey(key);
			}
		}
		return InteractionResult.PASS;
	}
}

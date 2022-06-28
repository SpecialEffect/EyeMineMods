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

package com.specialeffect.eyemine.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.specialeffect.eyemine.EyeMine;
import com.specialeffect.eyemine.mixin.AbstractContainerScreenAccessor;
import com.specialeffect.inventory.manager.ChestInventoryManager;
import com.specialeffect.inventory.manager.CreativeInventoryManager;
import com.specialeffect.inventory.manager.SurvivalInventoryManager;
import dev.architectury.event.EventResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.lwjgl.glfw.GLFW;

public class CreativeClientHelper {
	public static EventResult onKeyInput(Minecraft minecraft, int keyCode, int scanCode, int action, int modifiers) {
		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 292)) {
			return EventResult.pass();
		}

		if (action == GLFW.GLFW_RELEASE) {
			int key = keyCode;
			EyeMine.LOGGER.debug(key);
			Screen currentScreen = minecraft.screen;
			if (currentScreen != null) {
				if (currentScreen instanceof CreativeModeInventoryScreen gui) {
					AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) gui;
					CreativeInventoryManager con = CreativeInventoryManager.getInstance(
							accessor.getLeftPos(), accessor.getTopPos(),
							accessor.getXSize(), accessor.getYSize(),
							gui.getSelectedTab(),
							gui.getMenu());
					boolean handled = con.acceptKey(key);
					if (handled) {
						return EventResult.pass();
					}
				} else if (currentScreen instanceof ContainerScreen gui) {
					AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) gui;
					ChestInventoryManager con = ChestInventoryManager.getInstance(
							accessor.getLeftPos(), accessor.getTopPos(),
							accessor.getXSize(), accessor.getYSize(),
							gui.getMenu());
					con.acceptKey(key);
				} else if (currentScreen instanceof InventoryScreen gui) {
					AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) gui;
					SurvivalInventoryManager con = SurvivalInventoryManager.getInstance(
							accessor.getLeftPos(), accessor.getTopPos(),
							accessor.getXSize(), accessor.getYSize(), gui.getMenu());

					con.acceptKey(key);
				}
			}
		}
		return EventResult.pass();
	}
}

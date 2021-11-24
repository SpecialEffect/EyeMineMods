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

import com.mojang.blaze3d.vertex.PoseStack;
import com.specialeffect.eyemine.EyeMineClient;
import com.specialeffect.eyemine.client.gui.CustomCreateWorldScreen;
import com.specialeffect.eyemine.client.gui.crosshair.ICrosshairOverlay;
import com.specialeffect.eyemine.client.gui.crosshair.StateOverlay;
import com.specialeffect.eyemine.mixin.AbstractContainerScreenAccessor;
import com.specialeffect.eyemine.platform.EyeMineConfig;
import com.specialeffect.inventory.manager.CreativeInventoryManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.world.InteractionResultHolder;

import java.util.ArrayList;
import java.util.List;

public class MainClientHandler {
	public static final List<ICrosshairOverlay> crosshairOverlayList = new ArrayList<>();
	private static StateOverlay mStateOverlay;

	public static void onRenderGameOverlayEvent(PoseStack poseStack, float partialTicks) {
		if(!MainClientHandler.crosshairOverlayList.isEmpty()) {
			Minecraft minecraft = Minecraft.getInstance();
			for(ICrosshairOverlay overlay : MainClientHandler.crosshairOverlayList) {
				overlay.renderOverlay(poseStack, minecraft);
			}
		}
	}

	// Replace / augment some GUIs
	public static InteractionResultHolder<Screen> onGuiOpen(Screen screen) {
		System.out.println(screen);
		Screen currentScreen = Minecraft.getInstance().screen;
		if (!EyeMineClient.disableCustomNewWorld && screen instanceof CreateWorldScreen && !(currentScreen instanceof CustomCreateWorldScreen)) {
			CreateWorldScreen createWorldScreen = (CreateWorldScreen)screen;
			if (!EyeMineClient.allowMoreOptions) {
				// override the CreateWorldScreen, unless it's been requested from within our own CustomCreateWorldScreen
				return InteractionResultHolder.success(CustomCreateWorldScreen.create(screen));
			}
			EyeMineClient.allowMoreOptions = false;
		}
		if (screen instanceof CreativeModeInventoryScreen) {
			// Make sure mouse starts outside container, so we have a sensible reference point
			CreativeModeInventoryScreen gui = (CreativeModeInventoryScreen)screen ;
			AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor)gui;
			CreativeInventoryManager con = CreativeInventoryManager.getInstance(
					accessor.getLeftPos(), accessor.getTopPos(),
					accessor.getXSize(), accessor.getYSize(),
					gui.getSelectedTab(),
					gui.getMenu());
			con.resetMouse();
		}
		return InteractionResultHolder.pass(screen);
	}

	public static void initialize() {
		mStateOverlay = new StateOverlay();
		addOverlayToRender(mStateOverlay);
	}

	public static void addOverlayToRender(ICrosshairOverlay overlay) {
		crosshairOverlayList.add(overlay);
	}

	public static void saveWalkingSpeed(float speed) {
		EyeMineConfig.setCustomSpeedFactor(speed);
		EyeMineClient.refresh();
	}
}

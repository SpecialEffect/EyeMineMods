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

import com.specialeffect.eyemine.EyeMineClient;
import com.specialeffect.eyemine.client.gui.crosshair.ICrosshairOverlay;
import com.specialeffect.eyemine.client.gui.crosshair.StateOverlay;
import com.specialeffect.eyemine.mixin.AbstractContainerScreenAccessor;
import com.specialeffect.eyemine.platform.EyeMineConfig;
import com.specialeffect.inventory.manager.CreativeInventoryManager;
import com.specialeffect.eyemine.event.ScreenSetResult;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;

import java.util.ArrayList;
import java.util.List;

public class MainClientHandler {
	public static final List<ICrosshairOverlay> crosshairOverlayList = new ArrayList<>();
	private static StateOverlay mStateOverlay;

	public static void onRenderGameOverlayEvent(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
		if (!MainClientHandler.crosshairOverlayList.isEmpty()) {
			Minecraft minecraft = Minecraft.getInstance();
			for (ICrosshairOverlay overlay : MainClientHandler.crosshairOverlayList) {
				overlay.renderOverlay(guiGraphics, minecraft);
			}
		}
	}

	// Replace / augment some GUIs
	public static ScreenSetResult onGuiOpen(Screen screen) {
		if (screen instanceof CreativeModeInventoryScreen gui) {
			// Make sure mouse starts outside container, so we have a sensible reference point
			AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) gui;
			CreativeInventoryManager con = CreativeInventoryManager.getInstance(
					accessor.getLeftPos(), accessor.getTopPos(),
					accessor.getXSize(), accessor.getYSize(),
					gui.selectedTab,
					gui.getMenu());
			con.resetMouse();
		}
		return ScreenSetResult.pass();
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

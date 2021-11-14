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

package com.specialeffect.eyemine.submod.misc;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;
import com.specialeffect.eyemine.client.Keybindings;
import com.specialeffect.eyemine.client.MainClientHandler;
import com.specialeffect.eyemine.client.gui.crosshair.IconOverlay;
import com.specialeffect.eyemine.platform.EyeMineConfig;
import com.specialeffect.eyemine.submod.IConfigListener;
import com.specialeffect.eyemine.submod.SubMod;
import com.specialeffect.utils.ModUtils;
import me.shedaniel.architectury.event.events.client.ClientRawInputEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import org.lwjgl.glfw.GLFW;

public class IronSights extends SubMod implements IConfigListener {
	public final String MODID = "ironsights";

	private KeyMapping mToggleIronsight;
	private IconOverlay mIcon;
	private boolean ironsightsOn = false;
	
	// options
	private int fovReduction = 20;
	private float sensitivityReduction = 0.2f;


	public void onInitializeClient() {
		// Register key bindings
		Keybindings.keybindings.add(mToggleIronsight = new KeyMapping(
				"key.eyemine.toggle_ironsights",
				Type.KEYSYM,
				GLFW.GLFW_KEY_P,
				"category.eyemine.category.eyegaze_extra" // The translation key of the keybinding's category.
		));
		
		// Set up icon rendering		
		mIcon = new IconOverlay(Minecraft.getInstance(), "eyemine:icons/ironsights.png");
		mIcon.setPosition(0.5f,  0.5f, 0.6f, 1.0f);
		mIcon.fadeTime = 0;
		mIcon.setAlpha(EyeMineConfig.getFullscreenOverlayAlpha());
		mIcon.setVisible(false);
		MainClientHandler.addOverlayToRender(mIcon);

		ClientRawInputEvent.KEY_PRESSED.register(this::onKeyInput);
	}

	private InteractionResult onKeyInput(Minecraft minecraft, int keyCode, int scanCode, int action, int modifiers) {
		if (ModUtils.hasActiveGui()) { return InteractionResult.PASS; }

		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 292)) { return InteractionResult.PASS; }
	    
		if (mToggleIronsight.consumeClick()) {
			System.out.println(Minecraft.getInstance().options.fov);
			
			ironsightsOn = !ironsightsOn;
			if (ironsightsOn) {				
				Minecraft.getInstance().options.fov -= fovReduction;				
				Minecraft.getInstance().options.sensitivity -= sensitivityReduction;
			}
			else {
				Minecraft.getInstance().options.fov += fovReduction;
				Minecraft.getInstance().options.sensitivity += sensitivityReduction;
			}
			mIcon.setVisible(ironsightsOn);	
		}
		return InteractionResult.PASS;
	}


	@Override
	public void syncConfig() {
		this.fovReduction = EyeMineConfig.getIronsightsFovReduction();
		this.sensitivityReduction = ((float) EyeMineConfig.getIronsightsSensitivityReduction()) / 100.0f;
	}	
}

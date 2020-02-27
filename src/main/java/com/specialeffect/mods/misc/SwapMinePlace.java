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

import org.lwjgl.glfw.GLFW;

import com.specialeffect.mods.ChildMod;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings.Input;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class SwapMinePlace  extends ChildMod {
	public final String MODID = "swapmineplace";

	public void setup(final FMLCommonSetupEvent event) {
		// Register key bindings
		mSwapKB = new KeyBinding("Swap mine/place keys", GLFW.GLFW_KEY_F10, CommonStrings.EYEGAZE_EXTRA);
		ClientRegistry.registerKeyBinding(mSwapKB);		
	}

	private static KeyBinding mSwapKB;

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {	
		if (ModUtils.hasActiveGui()) { return; }
		
		if (mSwapKB.isPressed()) {
			
			Input attackInput = Minecraft.getInstance().gameSettings.keyBindAttack.getKey();
			Input useInput = Minecraft.getInstance().gameSettings.keyBindUseItem.getKey();
			
			Minecraft.getInstance().gameSettings.setKeyBindingCode(Minecraft.getInstance().gameSettings.keyBindAttack, useInput);
			Minecraft.getInstance().gameSettings.setKeyBindingCode(Minecraft.getInstance().gameSettings.keyBindUseItem, attackInput);
			
			// It's important to force a reload
			Minecraft.getInstance().gameSettings.saveOptions();
			Minecraft.getInstance().gameSettings.loadOptions();
			
			ModUtils.sendPlayerMessage("Swapping mine and place keys");			
			
		}
	}
}

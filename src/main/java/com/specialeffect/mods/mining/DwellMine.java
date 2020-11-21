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

package com.specialeffect.mods.mining;

import org.lwjgl.glfw.GLFW;

import com.specialeffect.mods.utils.DwellAction;
import com.specialeffect.mods.utils.KeyWatcher;
import com.specialeffect.mods.utils.TargetBlock;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class DwellMine 
extends DwellAction {
	
	public DwellMine() {
		super("DWELL MINING", 15); 
	}

	public final String MODID = "dwellbuild";
		
	private static KeyBinding mDwellMineKB;
	
	
	public void setup(final FMLCommonSetupEvent event) {

		// Register key bindings
		mDwellMineKB = new KeyBinding("Dwell mining", GLFW.GLFW_KEY_KP_6,
				CommonStrings.EYEGAZE_EXTRA);
		ClientRegistry.registerKeyBinding(mDwellMineKB);

		this.syncConfig();
	}
	
	
	@Override
	public void performAction(TargetBlock block) {
		final KeyBinding attackBinding = Minecraft.getInstance().gameSettings.keyBindAttack;
		KeyBinding.onTick(attackBinding.getKey());
	}
	
	
	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {
		
		if (ModUtils.hasActiveGui()) { return; }	    
	    if (event.getAction() != GLFW.GLFW_PRESS) { return; }

	    if (KeyWatcher.f3Pressed) { return; }
				
		if (event.getKey() == mDwellMineKB.getKey().getKeyCode()) {
			if (mDwelling) {				
				// Turn off dwell build
				this.setDwelling(false);
		        ModUtils.sendPlayerMessage("Dwell mining: OFF");		        
			}
			else {
				// Turn on dwell build 															
				this.setDwelling(true);															
				ModUtils.sendPlayerMessage("Dwell mining: ON");					      
			}
		} 
	}	
}

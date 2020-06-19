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

import com.specialeffect.gui.IconOverlay;
import com.specialeffect.mods.ChildMod;
import com.specialeffect.mods.EyeMineConfig;
import com.specialeffect.mods.utils.KeyWatcher;
import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class IronSights  
extends ChildMod implements ChildModWithConfig
{
	public final String MODID = "ironsights";

	private KeyBinding mToggleIronsight;
	private IconOverlay mIcon;
	private boolean ironsightsOn = false;
	
	// options
	private int fovReduction = 20;
	private float sensitivityReduction = 0.2f;
	
	
	public void setup(final FMLCommonSetupEvent event) {
		// Register key bindings
		mToggleIronsight = new KeyBinding("Turn ironsights on/off", GLFW.GLFW_KEY_P, CommonStrings.EYEGAZE_EXTRA);
		ClientRegistry.registerKeyBinding(mToggleIronsight);		
		
		// Set up icon rendering		
		mIcon = new IconOverlay(Minecraft.getInstance(), "specialeffect:icons/ironsights.png");
		mIcon.setPosition(0.5f,  0.5f, 0.6f, 1.0f);
		mIcon.fadeTime = 0;
		mIcon.setAlpha(0.2f);
		mIcon.setVisible(false);		
		
		MinecraftForge.EVENT_BUS.register(mIcon);

	}
	

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {	

		if (ModUtils.hasActiveGui()) { return; }	    
	    if (event.getAction() != GLFW.GLFW_PRESS) { return; }
	    	    
	    if (KeyWatcher.f3Pressed) { return; }
	    
		if (event.getKey() == mToggleIronsight.getKey().getKeyCode()) {		
			
			System.out.println(Minecraft.getInstance().gameSettings.fov);
			
			ironsightsOn = !ironsightsOn;
			if (ironsightsOn) {				
				Minecraft.getInstance().gameSettings.fov -= fovReduction;				
				Minecraft.getInstance().gameSettings.mouseSensitivity -= sensitivityReduction;
			}
			else {
				Minecraft.getInstance().gameSettings.fov += fovReduction;
				Minecraft.getInstance().gameSettings.mouseSensitivity += sensitivityReduction;
			}
			mIcon.setVisible(ironsightsOn);	
		}
	}


	@Override
	public void syncConfig() {
		// TODO Auto-generated method stub
		this.fovReduction = EyeMineConfig.ironsightsFovReduction.get();
		this.sensitivityReduction = EyeMineConfig.ironsightsSensitivityReduction.get().floatValue()/100.0f;

	}	
}

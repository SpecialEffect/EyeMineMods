/**
 * Copyright (C) 2016 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.mods.utils;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.gui.StateOverlay;
import com.specialeffect.mods.EyeGaze;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourcePackInfo.Priority;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ModStateGui.MODID)
public class ModStateGui extends BaseClassWithCallbacks {

	public static final String MODID = "modstategui";
	public static final String NAME = "modstategui";

	private StateOverlay mStateOverlay;

	public ModStateGui() {
		// highest priority so this is set up before any mods try to register against it
        FMLJavaModLoadingContext.get().getModEventBus().addListener(EventPriority.HIGHEST, this::setup);
        
        mStateOverlay = new StateOverlay(Minecraft.getInstance());
	}
	
	private void setup(final FMLCommonSetupEvent event) {
		System.out.println("ModStateGui::setup begins");
		
		MinecraftForge.EVENT_BUS.register(this);

		ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Overlay icons to show state of mods.");
    	ModUtils.setAsParent(event, EyeGaze.MODID);

		
		
		MinecraftForge.EVENT_BUS.register(mStateOverlay);
		System.out.println("ModStateGui::setup ends");
	}

}

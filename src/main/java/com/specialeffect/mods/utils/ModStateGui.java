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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ModStateGui.MODID)
public class ModStateGui extends BaseClassWithCallbacks {

	public static final String MODID = "modstategui";
	public static final String NAME = "modstategui";

	private StateOverlay mStateOverlay;

	public ModStateGui() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
	}
	
	private void setup(final FMLCommonSetupEvent event) {
		//pre-init
		MinecraftForge.EVENT_BUS.register(this);

		ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Overlay icons to show state of mods.");
    	ModUtils.setAsParent(event, EyeGaze.MODID);

		// This needs to be initialised in preinit because other mods will 
		// try to register with it in postinit.
		mStateOverlay = new StateOverlay(Minecraft.getInstance());
		
		// post init
		MinecraftForge.EVENT_BUS.register(mStateOverlay);
	}

}

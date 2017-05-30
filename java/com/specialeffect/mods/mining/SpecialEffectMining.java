/**
 * Copyright (C) 2016 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.mods.mining;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.utils.ModUtils;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

// This mod is purely a wrapper to cluster a few
// child mods. It doesn't even have any config.
@Mod(modid = SpecialEffectMining.MODID, 
  	 version = ModUtils.VERSION, 
	 name = SpecialEffectMining.NAME)
public class SpecialEffectMining extends BaseClassWithCallbacks {

	public static final String MODID = "specialeffect.Mining";	
	public static final String NAME = "SpecialEffectMining";

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(this);
		
		ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"A selection of mods for accessible mining methods");

	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// Subscribe to event buses
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
	}
}

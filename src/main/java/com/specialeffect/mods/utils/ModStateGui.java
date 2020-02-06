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
import com.specialeffect.mods.ChildMod;
import com.specialeffect.mods.EyeGaze;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ModStateGui implements ChildMod  {

	public static final String MODID = "modstategui";
	public static final String NAME = "modstategui";

	private StateOverlay mStateOverlay;

	public ModStateGui() {
		//FIXME: this stateoverlay should live in top level eyegaze mod, not its own mod        
        mStateOverlay = new StateOverlay(Minecraft.getInstance());
	}

	public void setup(final FMLCommonSetupEvent event) {
		MinecraftForge.EVENT_BUS.register(mStateOverlay);
	}

}

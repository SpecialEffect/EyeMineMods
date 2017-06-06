/**
 * Copyright (C) 2016 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.gui;

import com.specialeffect.mods.misc.SpecialEffectMisc;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;

public class GuiConfigMisc extends GuiConfig {

    public GuiConfigMisc(GuiScreen parent) 
    {
        super(parent,
                new ConfigElement(
                		SpecialEffectMisc.mConfig.getCategory(Configuration.CATEGORY_GENERAL))
                            .getChildElements(),
                            SpecialEffectMisc.MODID, 
                        false, 
		                false, 
		                "Options for " + SpecialEffectMisc.NAME);
    }
}
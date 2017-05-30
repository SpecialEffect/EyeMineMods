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

import com.specialeffect.mods.utils.SpecialEffectUtils;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;

public class GuiConfigUtils extends GuiConfig {

    public GuiConfigUtils(GuiScreen parent) 
    {
        super(parent,
                new ConfigElement(
                		SpecialEffectUtils.mConfig.getCategory(Configuration.CATEGORY_GENERAL))
                            .getChildElements(),
                            SpecialEffectUtils.MODID, 
                        false, 
		                false, 
		                "Options for " + SpecialEffectUtils.NAME);
    }
}
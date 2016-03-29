package com.specialeffect.gui;

import com.specialeffect.mods.WalkIncrements;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

public class GuiConfigSpecialEffect extends GuiConfig {

    public GuiConfigSpecialEffect(GuiScreen parent) 
    {
        super(parent,
                new ConfigElement(
                		WalkIncrements.mConfig.getCategory(Configuration.CATEGORY_GENERAL))
                            .getChildElements(),
                        WalkIncrements.MODID, 
		                false, 
		                false, 
		                "Options for " + WalkIncrements.NAME);
    }
}
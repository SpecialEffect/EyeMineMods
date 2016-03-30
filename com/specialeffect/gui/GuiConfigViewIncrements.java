package com.specialeffect.gui;

import com.specialeffect.mods.ViewIncrements;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

public class GuiConfigViewIncrements extends GuiConfig {

    public GuiConfigViewIncrements(GuiScreen parent) 
    {
        super(parent,
                new ConfigElement(
                		ViewIncrements.mConfig.getCategory(Configuration.CATEGORY_GENERAL))
                            .getChildElements(),
                        ViewIncrements.MODID, 
		                false, 
		                false, 
		                "Options for " + ViewIncrements.NAME);
    }
}
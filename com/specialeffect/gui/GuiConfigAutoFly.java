package com.specialeffect.gui;

import com.specialeffect.mods.AutoFly;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

public class GuiConfigAutoFly extends GuiConfig {

    public GuiConfigAutoFly(GuiScreen parent) 
    {
        super(parent,
                new ConfigElement(
                		AutoFly.mConfig.getCategory(Configuration.CATEGORY_GENERAL))
                            .getChildElements(),
                        AutoFly.MODID, 
                        false, 
		                false, 
		                "Options for " + AutoFly.NAME);
    }
}
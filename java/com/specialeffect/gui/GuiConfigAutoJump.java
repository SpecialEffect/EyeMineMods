package com.specialeffect.gui;

import com.specialeffect.mods.AutoJump;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

public class GuiConfigAutoJump extends GuiConfig {

    public GuiConfigAutoJump(GuiScreen parent) 
    {
        super(parent,
                new ConfigElement(
                		AutoJump.mConfig.getCategory(Configuration.CATEGORY_GENERAL))
                            .getChildElements(),
                            AutoJump.MODID, 
                        false, 
		                false, 
		                "Options for " + AutoJump.NAME);
    }
}
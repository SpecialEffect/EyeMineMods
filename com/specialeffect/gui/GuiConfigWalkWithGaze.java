package com.specialeffect.gui;

import com.specialeffect.mods.MoveWithGaze;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

public class GuiConfigWalkWithGaze extends GuiConfig {

    public GuiConfigWalkWithGaze(GuiScreen parent) 
    {
        super(parent,
                new ConfigElement(
                		MoveWithGaze.mConfig.getCategory(Configuration.CATEGORY_GENERAL))
                            .getChildElements(),
                        MoveWithGaze.MODID, 
                        false, 
		                false, 
		                "Options for " + MoveWithGaze.NAME);
    }
}
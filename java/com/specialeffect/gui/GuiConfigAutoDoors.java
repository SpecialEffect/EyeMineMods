package com.specialeffect.gui;

import com.specialeffect.mods.AutoFly;
import com.specialeffect.mods.AutoOpenDoors;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

public class GuiConfigAutoDoors extends GuiConfig {

    public GuiConfigAutoDoors(GuiScreen parent) 
    {
        super(parent,
                new ConfigElement(
                		AutoOpenDoors.mConfig.getCategory(Configuration.CATEGORY_GENERAL))
                            .getChildElements(),
                            AutoOpenDoors.MODID, 
                        false, 
		                false, 
		                "Options for " + AutoOpenDoors.NAME);
    }
}
package com.specialeffect.gui;

import com.specialeffect.mods.OpenTablesChests;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

public class GuiConfigOpenChestsEtc extends GuiConfig {

    public GuiConfigOpenChestsEtc(GuiScreen parent) 
    {
        super(parent,
                new ConfigElement(
                		OpenTablesChests.mConfig.getCategory(Configuration.CATEGORY_GENERAL))
                            .getChildElements(),
                        OpenTablesChests.MODID, 
                        false, 
		                false, 
		                "Options for " + OpenTablesChests.NAME);
    }
}
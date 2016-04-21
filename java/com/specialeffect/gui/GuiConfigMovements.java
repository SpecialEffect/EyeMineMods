package com.specialeffect.gui;

import java.util.ArrayList;
import java.util.List;

import com.specialeffect.mods.SpecialEffectMovements;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.DummyConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries.CategoryEntry;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.ConfigElement;

public class GuiConfigMovements extends GuiConfig {

    public GuiConfigMovements(GuiScreen parent) 
    {
        super(parent,
                new ConfigElement(
                		SpecialEffectMovements.mConfig.getCategory(Configuration.CATEGORY_GENERAL))
                            .getChildElements(),
                            SpecialEffectMovements.MODID, 
                        false, 
		                false, 
		                "Options for " + SpecialEffectMovements.NAME);
    }
}
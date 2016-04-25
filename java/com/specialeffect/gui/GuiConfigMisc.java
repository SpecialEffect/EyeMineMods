package com.specialeffect.gui;

import java.util.ArrayList;
import java.util.List;

import com.specialeffect.mods.misc.SpecialEffectMisc;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.DummyConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries.CategoryEntry;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.ConfigElement;

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
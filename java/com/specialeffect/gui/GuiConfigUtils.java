package com.specialeffect.gui;

import java.util.ArrayList;
import java.util.List;

import com.specialeffect.mods.utils.SpecialEffectUtils;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.DummyConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries.CategoryEntry;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.ConfigElement;

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
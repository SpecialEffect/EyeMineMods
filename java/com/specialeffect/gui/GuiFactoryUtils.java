package com.specialeffect.gui;

import net.minecraft.client.gui.GuiScreen;

public class GuiFactoryUtils extends GuiFactoryGeneric 
{
    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() 
    {
        return GuiConfigUtils.class;
    }
}

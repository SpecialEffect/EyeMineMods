package com.specialeffect.gui;

import net.minecraft.client.gui.GuiScreen;

public class GuiFactoryMisc extends GuiFactoryGeneric 
{
    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() 
    {
        return GuiConfigMisc.class;
    }
}

package com.specialeffect.gui;

import net.minecraft.client.gui.GuiScreen;

public class GuiFactoryAutoFly extends GuiFactoryGeneric 
{
    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() 
    {
        return GuiConfigAutoFly.class;
    }
}

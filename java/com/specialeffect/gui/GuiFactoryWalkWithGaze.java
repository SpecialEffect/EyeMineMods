package com.specialeffect.gui;

import net.minecraft.client.gui.GuiScreen;

public class GuiFactoryWalkWithGaze extends GuiFactoryGeneric 
{
    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() 
    {
        return GuiConfigWalkWithGaze.class;
    }
}

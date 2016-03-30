package com.specialeffect.gui;

import net.minecraft.client.gui.GuiScreen;

public class GuiFactoryWalkIncrements extends GuiFactoryGeneric 
{
    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() 
    {
        return GuiConfigWalkIncrements.class;
    }
}

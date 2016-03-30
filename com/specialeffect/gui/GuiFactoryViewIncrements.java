package com.specialeffect.gui;

import net.minecraft.client.gui.GuiScreen;

public class GuiFactoryViewIncrements extends GuiFactoryGeneric 
{
    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() 
    {
        return GuiConfigViewIncrements.class;
    }
}

package com.specialeffect.gui;

import net.minecraft.client.gui.GuiScreen;

public class GuiFactoryMovements extends GuiFactoryGeneric 
{
    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() 
    {
        return GuiConfigMovements.class;
    }
}

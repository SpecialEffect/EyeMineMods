package com.specialeffect.gui;

import net.minecraft.client.gui.GuiScreen;

public class GuiFactoryAutoDoors extends GuiFactoryGeneric 
{
    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() 
    {
        return GuiConfigAutoDoors.class;
    }
}

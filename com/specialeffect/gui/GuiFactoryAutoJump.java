package com.specialeffect.gui;

import net.minecraft.client.gui.GuiScreen;

public class GuiFactoryAutoJump extends GuiFactoryGeneric 
{
    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() 
    {
        return GuiConfigAutoJump.class;
    }
}

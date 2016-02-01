package com.specialeffect.utils;

import net.minecraft.client.gui.GuiScreen;

public class GuiFactorySpecialEffect extends GuiFactoryGeneric 
{
    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() 
    {
        return GuiConfigSpecialEffect.class;
    }
}

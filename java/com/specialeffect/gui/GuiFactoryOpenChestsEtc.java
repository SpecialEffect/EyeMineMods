package com.specialeffect.gui;

import net.minecraft.client.gui.GuiScreen;

public class GuiFactoryOpenChestsEtc extends GuiFactoryGeneric 
{
    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() 
    {
        return GuiConfigOpenChestsEtc.class;
    }
}

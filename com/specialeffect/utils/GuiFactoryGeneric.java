package com.specialeffect.utils;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

// A base class that provides the necessary overrides for IModGuiFactory.
// Generally mod-specific gui factories can extend this class and *only*
// have to override mainConfigGuiClass with a mod-specific class.
public class GuiFactoryGeneric implements IModGuiFactory 
{
    @Override
    public void initialize(Minecraft minecraftInstance) 
    {
    }
 
    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() 
    {
        return null;
    }
 
    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() 
    {
        return null;
    }
 
    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) 
    {
        return null;
    }
}
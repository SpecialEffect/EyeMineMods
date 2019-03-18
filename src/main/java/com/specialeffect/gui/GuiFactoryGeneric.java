/**
 * Copyright (C) 2016 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.gui;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

// A base class that provides the necessary overrides for IModGuiFactory.
// Generally mod-specific gui factories can extend this class and *only*
// have to override mainConfigGuiClass with a mod-specific class.
public abstract class GuiFactoryGeneric implements IModGuiFactory 
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

	@Override
	public boolean hasConfigGui() {
		return true;
	}

}
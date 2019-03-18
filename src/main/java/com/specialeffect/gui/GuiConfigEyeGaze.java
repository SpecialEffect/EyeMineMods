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

import java.util.ArrayList;
import java.util.List;

import com.specialeffect.mods.EyeGaze;

import de.skate702.craftingkeys.config.GuiConfig;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.DummyConfigElement;
import net.minecraftforge.fml.client.config.IConfigElement;

public class GuiConfigEyeGaze extends GuiConfig {

    public GuiConfigEyeGaze(GuiScreen parent) 
    {
        super(parent,
        		getConfigElements(),
                            EyeGaze.MODID, 
                        false, 
		                false, 
		                "Options for " + EyeGaze.NAME);
    }
    

    private static List<IConfigElement> getConfigElements() {
	    List<IConfigElement> list = new ArrayList<IConfigElement>();
	    for (String category : EyeGaze.userfacingCategories) {
	    	list.add(categoryElement(category));
	    }
	    return list;
    }
    
    // For each category, create a GUI with all child elements
	private static IConfigElement categoryElement(String category) {
		return new DummyConfigElement.DummyCategoryElement(category, category + " options",
				new ConfigElement(EyeGaze.mConfig.getCategory(category)).getChildElements());
    }
}
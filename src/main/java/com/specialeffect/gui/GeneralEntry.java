package com.specialeffect.gui;

import com.specialeffect.mods.EyeGaze;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.DummyConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiConfigEntries.CategoryEntry;
import net.minecraftforge.fml.client.config.IConfigElement;

public class GeneralEntry extends CategoryEntry {

	private String mCategoryName;
			
	public GeneralEntry(GuiConfig owningScreen,
					    GuiConfigEntries owningEntryList, 
					    IConfigElement prop) {
		super(owningScreen, owningEntryList, prop);
		mCategoryName = "xyz";
	}
	
	protected GuiScreen buildChildScreen() {
		 return new GuiConfig(this.owningScreen, 
				 new ConfigElement(EyeGaze.mConfig.getCategory("xyz")).getChildElements(),
	                EyeGaze.MODID, 
	                false, false, 
	                "xyz options for " + EyeGaze.NAME);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static IConfigElement categoryElement(String category, String name, String tool_tip) {
		return new DummyConfigElement.DummyCategoryElement(name, tool_tip,
				new ConfigElement(EyeGaze.mConfig.getCategory(category)).getChildElements());
	}
}
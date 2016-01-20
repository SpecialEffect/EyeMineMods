package com.specialeffect.utils;

import com.specialeffect.mods.EyeGaze;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

public class GuiConfigSpecialEffect extends GuiConfig {

    public GuiConfigSpecialEffect(GuiScreen parent) 
    {
    	
//    	Configuration configuration = MBEConfiguration.getConfig();
//        ConfigElement cat_general = new ConfigElement(configuration.getCategory(MBEConfiguration.CATEGORY_NAME_GENERAL));
//        List<IConfigElement> propertiesOnThisScreen = cat_general.getChildElements();
//        String windowTitle = configuration.toString();
        
        super(parent,
                new ConfigElement(
                		EyeGaze.mConfig.getCategory(Configuration.CATEGORY_GENERAL))
                            .getChildElements(),
                        EyeGaze.MODID, 
		                false, 
		                false, 
		                "Play Magic Beans Any Way You Want");
        System.out.println("SpecialEffectEyeGaze.mConfig: "+EyeGaze.mConfig);

        //titleLine2 = SpecialEffectEyeGaze.configFile.getAbsolutePath();
    }
    
    @Override
    public void initGui()
    {
        // You can add buttons and initialize fields here
        super.initGui();
    }

    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        // You can do things like create animations, draw additional elements, etc. here
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        // You can process any additional buttons you may have added here
        super.actionPerformed(button);
    }
}
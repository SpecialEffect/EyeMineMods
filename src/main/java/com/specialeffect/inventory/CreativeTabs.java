/**
 * Copyright (C) 2016 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.inventory;

import org.lwjgl.glfw.GLFW;

import com.specialeffect.mods.EyeMineConfig;
//fIXME import de.skate702.craftingkeys.config.GuiConfigHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(CreativeTabs.MODID)
public class CreativeTabs 
{

	public static final String MODID = "creativetabs";
	public static final String NAME = "CreativeTabs";

	/**
	 * Current Instance.
	 */
	//@Instance(value = MODID)
//	public static CreativeTabs instance;
	
	public static EyeMineConfig mConfig;

	//public static final Minecraft client = FMLClientHandler.instance().getClient();

	public CreativeTabs() {
		// Register ourselves for server and other game events we are interested in
		

		// Config setup
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, InventoryConfig.CLIENT_CONFIG);
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, InventoryConfig.COMMON_CONFIG);

		InventoryConfig.loadConfig(InventoryConfig.CLIENT_CONFIG,
				FMLPaths.CONFIGDIR.get().resolve("inventory-client.toml"));
		InventoryConfig.loadConfig(InventoryConfig.COMMON_CONFIG,
				FMLPaths.CONFIGDIR.get().resolve("inventory-common.toml"));
		
		// FIXME: did we lose any bus registering when refactoring??
		MinecraftForge.EVENT_BUS.register(this);
		//FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);			
		 
		}
//
//	public void setup(final FMLCommonSetupEvent event) {
//
//		ModUtils.setupModInfo(event, this.MODID, this.NAME,
//				"Add key bindings to access tabs in creative inventory");
//		ModUtils.setAsParent(event, EyeGaze.MODID);	
//
//		
//		//fIXME NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiConfigHandler());
//
//	}


    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) { 
        if (event.getAction() == GLFW.GLFW_RELEASE) {
	    	int key = event.getKey();
	        System.out.println(key);
	        Screen currentScreen = Minecraft.getInstance().currentScreen;
			if (currentScreen != null) {
				if (currentScreen instanceof CreativeScreen) {
					CreativeScreen gui = (CreativeScreen)currentScreen;
					CreativeInventoryManager con = CreativeInventoryManager.getInstance(
							gui.getGuiLeft(), gui.getGuiTop(), 
							gui.getXSize(), gui.getYSize(),
							gui.getSelectedTabIndex());            	
					con.acceptKey(key);					
				}
				else if (currentScreen instanceof ChestScreen)// InventoryScreen
				{
					ChestScreen gui = (ChestScreen)currentScreen;
					ChestInventoryManager con = ChestInventoryManager.getInstance(
							gui.getGuiLeft(), gui.getGuiTop(), 
							gui.getXSize(), gui.getYSize());            	
					con.acceptKey(key);					
				}
			}
			
        }
		
    }
}

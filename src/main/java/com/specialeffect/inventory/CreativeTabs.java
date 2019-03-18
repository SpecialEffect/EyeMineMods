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

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.mods.EyeGaze;
import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.utils.ModUtils;

import de.skate702.craftingkeys.config.GuiConfigHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

@Mod(modid = CreativeTabs.MODID, version = ModUtils.VERSION, name = CreativeTabs.NAME)
public class CreativeTabs 
extends BaseClassWithCallbacks
implements ChildModWithConfig 
{

	public static final String MODID = "specialeffect.creativetabs";
	public static final String NAME = "CreativeTabs";

	/**
	 * Current Instance.
	 */
	@Instance(value = MODID)
	public static CreativeTabs instance;

	public static final Minecraft client = FMLClientHandler.instance().getClient();

	@EventHandler
	@SuppressWarnings("static-access")
	public void preInit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);

		ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Add key bindings to access tabs in creative inventory");
		ModUtils.setAsParent(event, EyeGaze.MODID);

		Config.loadConfig(event);

	}

	@EventHandler
	public void load(FMLInitializationEvent event) {

		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiConfigHandler());

	}

	/*
	 * This is where we handle key inputs within the creative inventory
	 */
	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent tick) {

		GuiScreen currentScreen = client.currentScreen;
		if (currentScreen != null) {
			if (currentScreen instanceof GuiContainerCreative) {
				GuiContainerCreative gui = (GuiContainerCreative)currentScreen;	
				CreativeInventoryManager con = CreativeInventoryManager.getInstance(
						gui.getGuiLeft(), gui.getGuiTop(), 
						gui.getXSize(), gui.getYSize(),
						gui.getSelectedTabIndex());            	
				con.acceptKey();
			}
		}
	}

	@Override
	public void syncConfig() {
		Config.syncConfig();		
	}

}

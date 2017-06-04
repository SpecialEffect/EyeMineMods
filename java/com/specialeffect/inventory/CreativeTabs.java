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

import org.lwjgl.input.Keyboard;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.callbacks.IOnLiving;
import com.specialeffect.callbacks.SingleShotOnLivingCallback;
import com.specialeffect.gui.StateOverlay;
import com.specialeffect.messages.ChangeFlyingStateMessage;
import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.utils.ModUtils;

import de.skate702.craftingkeys.config.GuiConfigHandler;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiEnchantment;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.gui.GuiRepair;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiBrewingStand;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiDispenser;
import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import scala.swing.TextComponent;

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
	@SuppressWarnings("WeakerAccess")
	@Instance(value = MODID)
	public static CreativeTabs instance;

	public static final Minecraft client = FMLClientHandler.instance().getClient();

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(this);

		ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Add key bindings to access tabs in creative inventory");
		ModUtils.setAsParent(event, SpecialEffectInventory.MODID);

		Config.loadConfig(event);

	}

	@EventHandler
	public void load(FMLInitializationEvent event) {

		// Registering
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiConfigHandler());

	}

	/*
	 * This is where we handle key inputs within the creative inventory
	 */
	@SuppressWarnings("UnusedParameters")
	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent tick) {

		GuiScreen currentScreen = client.currentScreen;
		if (currentScreen != null) {
			if (currentScreen instanceof GuiContainerCreative) {
				GuiContainerCreative gui = (GuiContainerCreative)currentScreen;				
				CreativeInventoryManager con = CreativeInventoryManager.getInstance(
						gui.getGuiLeft(), gui.getGuiTop(), 
						gui.getXSize(), gui.getYSize());            	
				con.acceptKey();
			}
		}
	}

	@Override
	public void syncConfig() {
		Config.syncConfig();		
	}

}

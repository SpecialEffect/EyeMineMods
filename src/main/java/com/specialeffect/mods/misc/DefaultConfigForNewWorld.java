/**
 * Copyright (C) 2016 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.mods.misc;

import com.specialeffect.messages.AddItemToHotbar;
import com.specialeffect.messages.SendCommandMessage;
import com.specialeffect.mods.EyeGaze;
import com.specialeffect.utils.ModUtils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemAir;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.GameRules;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = DefaultConfigForNewWorld.MODID, version = ModUtils.VERSION, name = DefaultConfigForNewWorld.NAME)
public class DefaultConfigForNewWorld {
	public static final String MODID = "specialeffect.defaultconfigworld";
	public static final String NAME = "DefaultConfigWorld";

	public static SimpleNetworkWrapper network;
	private boolean firstWorldLoad = false;
	private boolean firstOnLivingTick = true;	
	
	@EventHandler
	@SuppressWarnings("static-access")
	public void preInit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);

		ModUtils.setupModInfo(event, this.MODID, this.NAME, "Apply default config to new worlds");
		ModUtils.setAsParent(event, EyeGaze.MODID);
		
		network = NetworkRegistry.INSTANCE.newSimpleChannel(this.NAME);
		network.registerMessage(AddItemToHotbar.Handler.class, AddItemToHotbar.class, 0, Side.SERVER);
		network.registerMessage(SendCommandMessage.Handler.class, SendCommandMessage.class, 1, Side.SERVER);

	}


	@SubscribeEvent
	public void onSpawn(EntityJoinWorldEvent event) {
		Entity entity = event.getEntity();
		if (entity != null && entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)entity;
			if (ModUtils.entityIsMe(player)) {
				firstOnLivingTick = true;
			}
		}
	}
	
	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (ModUtils.entityIsMe(event.getEntityLiving())) {
			EntityPlayer player = (EntityPlayer) event.getEntityLiving();

			// First onliving tick, we check inventory and fill it with default set
			// of items if it's empty
			if (firstOnLivingTick) {
				firstOnLivingTick = false;
			
				if (player.capabilities.isCreativeMode) {
					NonNullList<ItemStack> inventory = player.inventory.mainInventory;
					boolean hasSomeItems = false;
					for (ItemStack itemStack : inventory) {
						if (itemStack != null && !(itemStack.getItem() instanceof ItemAir) ) {
							hasSomeItems = true;
							break;
						}
					}
	
					if (!hasSomeItems) {
						equipPlayer(player.inventory);
					}
				}
			}
			// The first time the world loads, we set our preferred game rules
			// Users may override them manually later.
			if (firstWorldLoad) {	
				if (player.capabilities.isCreativeMode) {
					WorldServer worldServer = DimensionManager.getWorld(0); // default world
					if (worldServer.getTotalWorldTime() < 10) {
						GameRules gameRules = worldServer.getGameRules();
						printGameRules(gameRules);
						setDefaultGameRules(gameRules);
					}
				}
				firstWorldLoad = false;
			}
		}
	}

	@EventHandler
	public void onWorldLoad(FMLServerStartedEvent event) {
		// Note first time world loads, we'll make changes on next
		// onliving tick
		WorldServer worldServer = DimensionManager.getWorld(0); // default world
		if (worldServer.getTotalWorldTime() < 10) {
			firstWorldLoad = true;
		}
	}

	private void setDefaultGameRules(GameRules rules) {
		rules.setOrCreateGameRule("doDaylightCycle", "False");
		rules.setOrCreateGameRule("doWeatherCycle", "False");
		rules.setOrCreateGameRule("keepInventory", "True");

		// we've just turned off daylightcycle while time = morning... 
		// we prefer full daylight!
		sendCommand("/time set day");

	}

	private void printGameRules(GameRules rules) {
		System.out.println("Game rules:");
		String[] keys = rules.getRules();
		for (String key : keys) {
			System.out.println(key + ": " + rules.getString(key));
		}
	}
	
	private void sendCommand(String cmd ) {
		DefaultConfigForNewWorld.network.sendToServer(new SendCommandMessage(cmd));
	}
	
	private void equipPlayer(InventoryPlayer inventory) {
		// Ask server to put new item in hotbar		

		DefaultConfigForNewWorld.network.sendToServer(new AddItemToHotbar(
				new ItemStack(Blocks.BRICK_BLOCK)));
		DefaultConfigForNewWorld.network.sendToServer(new AddItemToHotbar(
				new ItemStack(Blocks.SANDSTONE)));
		DefaultConfigForNewWorld.network.sendToServer(new AddItemToHotbar(
				new ItemStack(Blocks.GLASS_PANE)));
		DefaultConfigForNewWorld.network.sendToServer(new AddItemToHotbar(
				new ItemStack(Blocks.MOSSY_COBBLESTONE)));
		
		DefaultConfigForNewWorld.network.sendToServer(new AddItemToHotbar(
				new ItemStack(Blocks.TORCH), 6));
		DefaultConfigForNewWorld.network.sendToServer(new AddItemToHotbar(
				new ItemStack(Items.DIAMOND_PICKAXE), 7));
		DefaultConfigForNewWorld.network.sendToServer(new AddItemToHotbar(
				new ItemStack(Items.DIAMOND_SWORD), 8));
		
		inventory.currentItem = 0;
	}
}

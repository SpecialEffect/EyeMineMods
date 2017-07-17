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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.specialeffect.messages.AddItemToHotbar;
import com.specialeffect.messages.UseDoorAtPositionMessage;
import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.utils.ModUtils;
import com.specialeffect.utils.OpenableBlock;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemAir;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = DefaultConfigForNewWorld.MODID, version = ModUtils.VERSION, name = DefaultConfigForNewWorld.NAME)
public class DefaultConfigForNewWorld {
	public static final String MODID = "specialeffect.defaultconfig";
	public static final String NAME = "DefaultConfig";

	public static SimpleNetworkWrapper network;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(this);

		ModUtils.setupModInfo(event, this.MODID, this.NAME, "Apply default config to new worlds");
		ModUtils.setAsParent(event, SpecialEffectMisc.MODID);
		
		network = NetworkRegistry.INSTANCE.newSimpleChannel(this.NAME);
		network.registerMessage(AddItemToHotbar.Handler.class, AddItemToHotbar.class, 0, Side.SERVER);

	}

	private boolean firstOnLivingTick = true;	

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
		if (firstOnLivingTick &&
				ModUtils.entityIsMe(event.getEntityLiving())) {
			System.out.println("first tick");
			EntityPlayer player = (EntityPlayer) event.getEntityLiving();
			
			if (player.capabilities.isCreativeMode) {
				// Check inventory - if empty, we'll fill it with a default
				// set of items
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
			firstOnLivingTick = false;
		}
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// Subscribe to event buses
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@EventHandler
	public void onWorldLoad(FMLServerStartedEvent event) {

		WorldServer worldServer = DimensionManager.getWorld(0); // default world
		GameRules gameRules = worldServer.getGameRules();
		printGameRules(gameRules);

		// The first time the world loads, we set our preferred game rules
		// Users may override them manually later.
		if (worldServer.getTotalWorldTime() < 10) {
			setDefaultGameRules(gameRules);
		}
	}

	private void setDefaultGameRules(GameRules rules) {
		rules.setOrCreateGameRule("doDaylightCycle", "False");
		rules.setOrCreateGameRule("doWeatherCycle", "False");
		rules.setOrCreateGameRule("keepInventory", "True");
	}

	private void printGameRules(GameRules rules) {
		System.out.println("Game rules:");
		String[] keys = rules.getRules();
		for (String key : keys) {
			System.out.println(key + ": " + rules.getString(key));
		}
	}
	
	private void equipPlayer(InventoryPlayer inventory) {
		// Ask server to put new item in hotbar
		DefaultConfigForNewWorld.network.sendToServer(new AddItemToHotbar(
				new ItemStack(Items.DIAMOND_PICKAXE)));
		DefaultConfigForNewWorld.network.sendToServer(new AddItemToHotbar(
				new ItemStack(Items.DIAMOND_SWORD)));
		DefaultConfigForNewWorld.network.sendToServer(new AddItemToHotbar(
				new ItemStack(Blocks.SANDSTONE)));
		DefaultConfigForNewWorld.network.sendToServer(new AddItemToHotbar(
				new ItemStack(Blocks.GLASS_PANE)));
		DefaultConfigForNewWorld.network.sendToServer(new AddItemToHotbar(
				new ItemStack(Blocks.COBBLESTONE_WALL)));
		DefaultConfigForNewWorld.network.sendToServer(new AddItemToHotbar(
				new ItemStack(Blocks.TORCH)));
	}
}

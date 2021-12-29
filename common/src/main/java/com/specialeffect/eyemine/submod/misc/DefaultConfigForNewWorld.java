/**
 * Copyright (C) 2016-2020 Kirsty McNaught
 *
 * Developed for SpecialEffect, www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.eyemine.submod.misc;

import com.specialeffect.eyemine.EyeMineClient;
import com.specialeffect.eyemine.packets.PacketHandler;
import com.specialeffect.eyemine.packets.messages.AddItemToHotbar;
import com.specialeffect.eyemine.platform.EyeMineConfig;
import com.specialeffect.eyemine.submod.IConfigListener;
import com.specialeffect.eyemine.submod.SubMod;
import com.specialeffect.utils.ModUtils;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.LevelData;

import java.lang.reflect.Field;

public class DefaultConfigForNewWorld extends SubMod implements IConfigListener {
    public final String MODID = "specialeffect.defaultconfigworld";
    
    private boolean firstOnLivingTick = true;

	private boolean haveEquippedPlayer = false;   
	
	private static boolean alwaysDayTimeSetting = false;
	private static boolean alwaysSunnySetting = false;
	private static boolean keepInventorySetting = false;
    
	public static void setNewWorldOptions(boolean daytime, boolean sunny, boolean keepInventory) {
		// allow "new world" gui to cache user preferences ready for new world creation
		alwaysDayTimeSetting = daytime;
		alwaysSunnySetting = sunny;
		keepInventorySetting = keepInventory;
	}

	public void onInitializeClient() {
		EntityEvent.ADD.register(this::onSpawn);
		TickEvent.PLAYER_POST.register(this::onLiving);
		LifecycleEvent.SERVER_LEVEL_LOAD.register(this::onWorldLoad);
    }

	@Override
	public void syncConfig() {
		EyeMineClient.disableCustomNewWorld = EyeMineConfig.getDisableCustomNewWorld();
	}

	private EventResult onSpawn(Entity entity, Level level) {
		if(!EyeMineClient.disableCustomNewWorld) {
			if (entity != null && entity instanceof Player player) {
				if (ModUtils.entityIsMe(player)) {
					firstOnLivingTick = true;
					haveEquippedPlayer = false;
				}
			}
		}
		return EventResult.pass();
	}

	private void onLiving(Player player) {
		if (!EyeMineClient.disableCustomNewWorld && ModUtils.entityIsMe(player) && player instanceof LocalPlayer) {
			// First onliving tick, we check inventory and fill it with default set
			// of items if it's empty
			if (firstOnLivingTick && !haveEquippedPlayer) {
				firstOnLivingTick = false;

				if (player.isCreative()) {
					NonNullList<ItemStack> inventory = player.getInventory().items;
					boolean hasSomeItems = false;
					for (ItemStack itemStack : inventory) {
						if (itemStack != null && !(itemStack.getItem() instanceof AirItem) ) {
							hasSomeItems = true;
							break;
						}
					}

					if (!hasSomeItems) {
						equipPlayer(player.getInventory());
					}
				}
				haveEquippedPlayer = true;
			}
		}
	}


	private void onWorldLoad(ServerLevel serverLevel) {
		if(!EyeMineClient.disableCustomNewWorld) {
			LOGGER.debug("onWorldLoad: " + alwaysDayTimeSetting + ", " + alwaysSunnySetting + ", " + keepInventorySetting);

			MinecraftServer server = serverLevel.getServer();
			LevelData info = serverLevel.getLevelData();
			GameRules rules = info.getGameRules();

			if (info.getGameTime() < 60) {
				// First time loading, set rules according to user preference
				if (server != null && server.getDefaultGameType() == GameType.CREATIVE) {
					rules.getRule(GameRules.RULE_DAYLIGHT).set(!alwaysDayTimeSetting, server);
					rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(!alwaysSunnySetting, server);
					rules.getRule(GameRules.RULE_KEEPINVENTORY).set(keepInventorySetting, server);

					// Extra settings as a result of the above
					if (alwaysDayTimeSetting) {
						// we've just turned off daylightcycle while time = morning...
						// we prefer full daylight!
						for(ServerLevel level : server.getAllLevels()) {
							level.setDayTime(level.getDayTime() + (long)2000);
						}
					}
				}
			}
		}
	}

    @SuppressWarnings("unused")
	private void printGameRules(GameRules rules) {
        System.out.println("Game rules:");        
        
        // We use reflection to print out any relevant fields 
        Field[] fields = rules.getClass().getFields();
        for(Field f : fields){        	           
			try {
				Object v = f.get(rules);
				
				if (v instanceof GameRules.Key<?> key) {
					LOGGER.debug(key + ": " + rules.getRule(key).toString());
		        }    
			} catch (Exception e) {
				e.printStackTrace();
			}        
        }        
    }    
    
    private void equipPlayer(Inventory inventory) {
        // Ask server to put new item in hotbar     
        PacketHandler.CHANNEL.sendToServer(new AddItemToHotbar(
                new ItemStack(Blocks.BRICKS), 0));
		PacketHandler.CHANNEL.sendToServer(new AddItemToHotbar(
                new ItemStack(Blocks.SANDSTONE), 1));
		PacketHandler.CHANNEL.sendToServer(new AddItemToHotbar(
                new ItemStack(Blocks.GLASS_PANE), 2));
		PacketHandler.CHANNEL.sendToServer(new AddItemToHotbar(
                new ItemStack(Blocks.MOSSY_COBBLESTONE), 3));

		PacketHandler.CHANNEL.sendToServer(new AddItemToHotbar(
                new ItemStack(Blocks.TORCH), 6));
		PacketHandler.CHANNEL.sendToServer(new AddItemToHotbar(
                new ItemStack(Items.DIAMOND_PICKAXE), 7));
		PacketHandler.CHANNEL.sendToServer(new AddItemToHotbar(
                new ItemStack(Items.DIAMOND_SWORD), 8));
        
        inventory.selected = 1;
    }
}
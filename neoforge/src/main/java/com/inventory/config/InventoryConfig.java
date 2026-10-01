/**
 * Copyright (C) 2016-2020 Kirsty McNaught
 * <p>
 * Developed for SpecialEffect, www.specialeffect.org.uk
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.inventory.config;

import com.mojang.blaze3d.platform.InputConstants;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InventoryConfig {
	// Based on McJty/YouTubeModding14 tutorial, MIT license:
	// https://github.com/McJty/YouTubeModding14/blob/master/LICENSE

	// Directly reference a log4j logger.
	private static final Logger LOGGER = LogManager.getLogger();

	public static final String CATEGORY_GENERAL = "general";

	private static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();

	public static ModConfigSpec CLIENT_CONFIG;

	public static ModConfigSpec.ConfigValue<Integer> key0, key1, key2, key3, key4,
			key5, key6, key7, key8, key9,
			keyNext, keyPrev, keySearch,
			keyScrollUp, keyScrollDown,
			keyNextItemRow, keyNextItemCol, keyDrop;

	public static ModConfigSpec.ConfigValue<Integer> keySurvNextTab, keySurvPrevTab, keySurvRecipes, keySurvCraftable,
			keySurvPrevPage, keySurvNextPage, keySurvOutput;


	static {
		CLIENT_BUILDER.comment("Inventory shortcut keys").push("Keys");
		setupConfigKeys();
		CLIENT_BUILDER.pop();

		CLIENT_BUILDER.comment("Navigation keys").push("Keys");
		setupNavKeys();
		CLIENT_BUILDER.pop();

		CLIENT_BUILDER.comment("Survival inventory keys").push("Keys");
		setupSurvivalKeys();
		CLIENT_BUILDER.pop();

		CLIENT_CONFIG = CLIENT_BUILDER.build();
	}


	private static void setupSurvivalKeys() {

		keySurvPrevTab = CLIENT_BUILDER.comment("recipes: prev tab").define("keySurvPrevTab", InputConstants.KEY_NUMPAD0);
		keySurvNextTab = CLIENT_BUILDER.comment("recipes: next tab").define("keySurvNextTab", InputConstants.KEY_NUMPAD1);

		keySurvRecipes = CLIENT_BUILDER.comment("open/close recipe book").define("keySurvRecipes", InputConstants.KEY_NUMPAD2);
		keySurvCraftable = CLIENT_BUILDER.comment("toggle all/craftable").define("keySurvCraftable", InputConstants.KEY_NUMPAD3);

		keySurvPrevPage = CLIENT_BUILDER.comment("recipes: prev page").define("keySurvPrevPage", InputConstants.KEY_NUMPAD4);
		keySurvNextPage = CLIENT_BUILDER.comment("recipes: next page").define("keySurvNextPage", InputConstants.KEY_NUMPAD5);

		keySurvOutput = CLIENT_BUILDER.comment("hover output").define("keySurvOutput", InputConstants.KEY_NUMPAD6);

	}

	private static void setupConfigKeys() {

		key0 = CLIENT_BUILDER.comment("key0").define("key0", InputConstants.KEY_NUMPAD0);
		key1 = CLIENT_BUILDER.comment("key1").define("key1", InputConstants.KEY_NUMPAD1);
		key2 = CLIENT_BUILDER.comment("key2").define("key2", InputConstants.KEY_NUMPAD2);
		key3 = CLIENT_BUILDER.comment("key3").define("key3", InputConstants.KEY_NUMPAD3);
		key4 = CLIENT_BUILDER.comment("key4").define("key4", InputConstants.KEY_NUMPAD4);
		key5 = CLIENT_BUILDER.comment("key5").define("key5", InputConstants.KEY_NUMPAD5);
		key6 = CLIENT_BUILDER.comment("key6").define("key6", InputConstants.KEY_NUMPAD6);
		key7 = CLIENT_BUILDER.comment("key7").define("key7", InputConstants.KEY_NUMPAD7);
		key8 = CLIENT_BUILDER.comment("key8").define("key8", InputConstants.KEY_NUMPAD8);
		key9 = CLIENT_BUILDER.comment("key9").define("key9", InputConstants.KEY_NUMPAD9);

	}

	private static void setupNavKeys() {

		keyPrev = CLIENT_BUILDER.comment("keyPrev").define("keyPrev", InputConstants.KEY_LEFT);
		keyNext = CLIENT_BUILDER.comment("keyNext").define("keyNext", InputConstants.KEY_RIGHT);
		keyNextItemRow = CLIENT_BUILDER.comment("keyNextItemRow").define("keyNextItemRow", InputConstants.KEY_F6);
		keyNextItemCol = CLIENT_BUILDER.comment("keyNextItemCol").define("keyNextItemCol", InputConstants.KEY_F7);

		keyScrollUp = CLIENT_BUILDER.comment("keyScrollUp").define("keyScrollUp", InputConstants.KEY_F8);
		keyScrollDown = CLIENT_BUILDER.comment("keyScrollDown").define("keyScrollDown", InputConstants.KEY_F9);

		keySearch = CLIENT_BUILDER.comment("keySearch").define("keySearch", InputConstants.KEY_DOWN);
		keyDrop = CLIENT_BUILDER.comment("keyDrop2").define("keyDrop2", InputConstants.KEY_MINUS);
	}

	@SubscribeEvent
	public static void onLoad(final ModConfigEvent.Loading configEvent) {
		LOGGER.info("Inventory config onLoad");
	}

	@SubscribeEvent
	public static void onReload(final ModConfigEvent.Reloading configEvent) {
		LOGGER.info("Inventory config onReload");

		if (configEvent.getConfig() != null && configEvent.getConfig().getSpec() == CLIENT_CONFIG) {
			// the configspec values are updated for us, but we may want to hook into here too?
		}
	}

}
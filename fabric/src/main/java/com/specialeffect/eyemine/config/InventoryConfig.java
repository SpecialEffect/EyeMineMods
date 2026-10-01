package com.specialeffect.eyemine.config;

import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.CollapsibleObject;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = "eyemine-inventory")
public class InventoryConfig implements ConfigData {
	@CollapsibleObject
	public Survival survival = new Survival();

	public static class Survival {
		@Comment("recipes: prev tab")
		public int keySurvPrevTab = InputConstants.KEY_NUMPAD0;
		@Comment("recipes: next tab")
		public int keySurvNextTab = InputConstants.KEY_NUMPAD1;

		@Comment("open/close recipe book")
		public int keySurvRecipes = InputConstants.KEY_NUMPAD2;
		@Comment("toggle all/craftable")
		public int keySurvCraftable = InputConstants.KEY_NUMPAD3;

		@Comment("recipes: prev page")
		public int keySurvPrevPage = InputConstants.KEY_NUMPAD4;
		@Comment("recipes: next page")
		public int keySurvNextPage = InputConstants.KEY_NUMPAD5;

		@Comment("hover output")
		public int keySurvOutput = InputConstants.KEY_NUMPAD6;
	}

	@CollapsibleObject
	public ConfigKeys configKeys = new ConfigKeys();

	public static class ConfigKeys {
		@Comment("key0")
		public int key0 = InputConstants.KEY_NUMPAD0;
		@Comment("key1")
		public int key1 = InputConstants.KEY_NUMPAD1;
		@Comment("key2")
		public int key2 = InputConstants.KEY_NUMPAD2;
		@Comment("key3")
		public int key3 = InputConstants.KEY_NUMPAD3;
		@Comment("key4")
		public int key4 = InputConstants.KEY_NUMPAD4;
		@Comment("key5")
		public int key5 = InputConstants.KEY_NUMPAD5;
		@Comment("key6")
		public int key6 = InputConstants.KEY_NUMPAD6;
		@Comment("key7")
		public int key7 = InputConstants.KEY_NUMPAD7;
		@Comment("key8")
		public int key8 = InputConstants.KEY_NUMPAD8;
		@Comment("key9")
		public int key9 = InputConstants.KEY_NUMPAD9;
	}

	@CollapsibleObject
	public NavKeys navKeys = new NavKeys();

	public static class NavKeys {
		@Comment("keyPrev")
		public int keyPrev = InputConstants.KEY_LEFT;
		@Comment("keyNext")
		public int keyNext = InputConstants.KEY_RIGHT;
		@Comment("keyNextItemRow")
		public int keyNextItemRow = InputConstants.KEY_F6;
		@Comment("keyNextItemCol")
		public int keyNextItemCol = InputConstants.KEY_F7;

		@Comment("keyScrollUp")
		public int keyScrollUp = InputConstants.KEY_F8;
		@Comment("keyScrollDown")
		public int keyScrollDown = InputConstants.KEY_F9;

		@Comment("keySearch")
		public int keySearch = InputConstants.KEY_DOWN;
		@Comment("keyDrop")
		public int keyDrop = InputConstants.KEY_MINUS;
	}
}

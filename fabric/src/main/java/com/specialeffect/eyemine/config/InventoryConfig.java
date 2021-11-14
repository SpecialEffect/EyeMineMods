package com.specialeffect.eyemine.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.CollapsibleObject;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import org.lwjgl.glfw.GLFW;

@Config(name = "eyemine-inventory")
public class InventoryConfig implements ConfigData {
	@CollapsibleObject
	public Survival survival = new Survival();

	public static class Survival {
		@Comment("recipes: prev tab")
		public int keySurvPrevTab  = GLFW.GLFW_KEY_KP_0;
		@Comment("recipes: next tab")
		public int keySurvNextTab = GLFW.GLFW_KEY_KP_1;

		@Comment("open/close recipe book")
		public int keySurvRecipes  = GLFW.GLFW_KEY_KP_2;
		@Comment("toggle all/craftable")
		public int keySurvCraftable = GLFW.GLFW_KEY_KP_3;

		@Comment("recipes: prev page")
		public int keySurvPrevPage  = GLFW.GLFW_KEY_KP_4;
		@Comment("recipes: next page")
		public int keySurvNextPage = GLFW.GLFW_KEY_KP_5;

		@Comment("hover output")
		public int keySurvOutput = GLFW.GLFW_KEY_KP_6;
	}

	@CollapsibleObject
	public ConfigKeys configKeys = new ConfigKeys();

	public static class ConfigKeys {
		@Comment("key0")
		public int key0 = GLFW.GLFW_KEY_KP_0;
		@Comment("key1")
		public int key1 = GLFW.GLFW_KEY_KP_1;
		@Comment("key2")
		public int key2 = GLFW.GLFW_KEY_KP_2;
		@Comment("key3")
		public int key3 = GLFW.GLFW_KEY_KP_3;
		@Comment("key4")
		public int key4 = GLFW.GLFW_KEY_KP_4;
		@Comment("key5")
		public int key5 = GLFW.GLFW_KEY_KP_5;
		@Comment("key6")
		public int key6 = GLFW.GLFW_KEY_KP_6;
		@Comment("key7")
		public int key7 = GLFW.GLFW_KEY_KP_7;
		@Comment("key8")
		public int key8 = GLFW.GLFW_KEY_KP_8;
		@Comment("key9")
		public int key9 = GLFW.GLFW_KEY_KP_9;
	}

	@CollapsibleObject
	public NavKeys navKeys = new NavKeys();

	public static class NavKeys {
		@Comment("keyPrev")
		public int keyPrev = GLFW.GLFW_KEY_LEFT;
		@Comment("keyNext")
		public int keyNext = GLFW.GLFW_KEY_RIGHT;
		@Comment("keyNextItemRow")
		public int keyNextItemRow = GLFW.GLFW_KEY_F6;
		@Comment("keyNextItemCol")
		public int keyNextItemCol = GLFW.GLFW_KEY_F7;

		@Comment("keyScrollUp")
		public int keyScrollUp = GLFW.GLFW_KEY_F8;
		@Comment("keyScrollDown")
		public int keyScrollDown = GLFW.GLFW_KEY_F9;

		@Comment("keySearch")
		public int keySearch = GLFW.GLFW_KEY_DOWN;
		@Comment("keyDrop")
		public int keyDrop = GLFW.GLFW_KEY_MINUS;
	}
}

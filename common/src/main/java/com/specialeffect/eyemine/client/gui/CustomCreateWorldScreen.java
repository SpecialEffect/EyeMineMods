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

package com.specialeffect.eyemine.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.specialeffect.eyemine.EyeMineClient;
import com.specialeffect.eyemine.mixin.WorldGenSettingsComponentAccessor;
import com.specialeffect.eyemine.submod.misc.DefaultConfigForNewWorld;
import net.minecraft.FileUtil;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldGenSettingsComponent;
import net.minecraft.client.gui.screens.worldselection.WorldPreset;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryAccess.Frozen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.Random;

public class CustomCreateWorldScreen extends Screen {
	private final Screen parentScreen;
	private EditBox worldNameField;
	private String saveDirName;
	private boolean alreadyGenerated;
	private Button btnCreateWorld;
	private Component worldName;

	// Buttons for EyeMine options
	private BooleanButton btnDaytime;
	private BooleanButton btnSunny;
	private BooleanButton btnInventory;
	public WorldGenSettingsComponent worldGenSettingsComponent;

	public CustomCreateWorldScreen(Screen screen, WorldGenSettingsComponent settings) {
		super(new TranslatableComponent("selectWorld.create"));
		this.parentScreen = screen;
		this.worldName = new TranslatableComponent("selectWorld.newWorld");
		this.worldGenSettingsComponent = settings;
	}

	public static CustomCreateWorldScreen create(@Nullable Screen screen) {
		if(screen instanceof CreateWorldScreen) {
			return new CustomCreateWorldScreen(screen, ((CreateWorldScreen)screen).worldGenSettingsComponent);
		}
		Frozen registryaccess$frozen = (Frozen)RegistryAccess.BUILTIN.get();
		return new CustomCreateWorldScreen(screen, new WorldGenSettingsComponent(registryaccess$frozen, WorldGenSettings.makeDefault(registryaccess$frozen), Optional.of(WorldPreset.NORMAL), OptionalLong.empty()));
	}

	public void tick() {
		this.worldNameField.tick();
	}

	protected void init() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		this.worldNameField = new EditBox(this.font, this.width / 2 - 100, 50, 200, 20, new TranslatableComponent("selectWorld.enterName"));
		this.worldNameField.setValue(this.worldName.getString());
		this.worldNameField.setResponder((p_214319_1_) -> {
			this.worldName = new TextComponent(p_214319_1_);
			this.btnCreateWorld.active = !this.worldNameField.getValue().isEmpty();
			this.calcSaveDirName();
		});
		this.addWidget(this.worldNameField);

		this.btnCreateWorld = this.addRenderableWidget(new Button(this.width / 2 - 155, this.height - 28, 150, 20, new TranslatableComponent("selectWorld.create"), (p_214318_1_) -> {
			this.onCreate();
		}));

		// Buttons for EyeMine options, contain their own boolean state

		String sDaytime = "Always daytime";
		String sSunny = "Always sunny";
		String sInventory = "Keep inventory after dying";

		int w = (int) (this.font.width(sInventory) * 1.5);
		btnDaytime = new BooleanButton(sDaytime, true, this.width / 2, 110, w);
		btnSunny = new BooleanButton(sSunny, true, this.width / 2, 130, w);
		btnInventory = new BooleanButton(sInventory, true, this.width / 2, 150, w);

		this.addRenderableWidget(btnDaytime.getButton());
		this.addRenderableWidget(btnSunny.getButton());
		this.addRenderableWidget(btnInventory.getButton());

		// More options -> back to the usual minecraft screens
		this.addRenderableWidget(new Button(this.width / 2 - 75, 177, 150, 20, new TranslatableComponent("Advanced Minecraft Options"), (p_214321_1_) -> {
			EyeMineClient.allowMoreOptions = true;
			DefaultConfigForNewWorld.setNewWorldOptions(btnDaytime.getValue(), btnSunny.getValue(), btnInventory.getValue());
			this.minecraft.setScreen(CreateWorldScreen.createFresh(this));
		}));

		this.addRenderableWidget(new Button(this.width / 2 + 5, this.height - 28, 150, 20, new TranslatableComponent("gui.cancel"), (p_214317_1_) -> {
			this.minecraft.setScreen(this.parentScreen);
		}));
		this.setInitialFocus(this.worldNameField);
		this.calcSaveDirName();
	}

	static class BooleanButton {
		private int x = 0;
		private int y = 0;
		private int w = 0;
		private boolean val;
		private String label;
		private Button btn;

		BooleanButton(String label, boolean defaultVal, int xPosCentre, int yPos, int width) {
			this.label = label;
			this.val = defaultVal;
			this.x = xPosCentre - width / 2;
			this.y = yPos;
			this.w = width;

			btn = new Button(x, y, w, 20, new TextComponent(this.getLabel(val)), (b) -> {
				this.toggle(b);
			});
		}

		public Button getButton() {
			return btn;
		}

		private String getLabel(boolean b) {
			String msgBool = b ? "ON" : "OFF";
			return label + ": " + msgBool;
		}

		private void toggle(Button b) {
			val = !val;
			this.btn.setMessage(new TextComponent(this.getLabel(val)));
		}

		boolean getValue() {
			return val;
		}
	}

	/**
	 * Determine a save-directory name from the world name
	 */
	private void calcSaveDirName() {
		this.saveDirName = this.worldNameField.getValue().trim();
		if (this.saveDirName.length() == 0) {
			this.saveDirName = "World";
		}

		try {
			this.saveDirName = FileUtil.findAvailableName(this.minecraft.getLevelSource().getBaseDir(), this.saveDirName, "");
		} catch (Exception var4) {
			this.saveDirName = "World";

			try {
				this.saveDirName = FileUtil.findAvailableName(this.minecraft.getLevelSource().getBaseDir(), this.saveDirName, "");
			} catch (Exception exception) {
				throw new RuntimeException("Could not create save folder", exception);
			}
		}

	}

	public void removed() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
	}

	private void onCreate() {
		this.minecraft.forceSetScreen(new GenericDirtMessageScreen(new TranslatableComponent("createWorld.preparing")));
		DefaultConfigForNewWorld.setNewWorldOptions(btnDaytime.getValue(), btnSunny.getValue(), btnInventory.getValue());
		if (!this.alreadyGenerated) {
			this.alreadyGenerated = true;
			long i = (new Random()).nextLong();

			boolean generateStructures = true;
			boolean hardcoreMode = false;

			WorldGenSettings worldGenSettings = ((WorldGenSettingsComponentAccessor)worldGenSettingsComponent).getSettings().withSeed(false, OptionalLong.of(i));

			LevelSettings levelSettings = new LevelSettings(this.worldNameField.getValue().trim(), GameType.CREATIVE, hardcoreMode, Difficulty.NORMAL, true, new GameRules(), DataPackConfig.DEFAULT);

			GameRules gameRules = levelSettings.gameRules();
			gameRules.getRule(GameRules.RULE_DAYLIGHT).set(!btnDaytime.getValue(), (MinecraftServer)null);
			gameRules.getRule(GameRules.RULE_WEATHER_CYCLE).set(!btnSunny.getValue(), (MinecraftServer)null);
			gameRules.getRule(GameRules.RULE_KEEPINVENTORY).set(btnInventory.getValue(), (MinecraftServer)null);

			// TODO: more options here?
			levelSettings.allowCommands();

			this.minecraft.createLevel(this.saveDirName, levelSettings, worldGenSettingsComponent.registryHolder(), worldGenSettings);
		}
	}

	public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
		if (super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_)) {
			return true;
		} else if (p_keyPressed_1_ != 257 && p_keyPressed_1_ != 335) {
			return false;
		} else {
			this.onCreate();
			return true;
		}
	}

	public void render(PoseStack poseStack, int p_render_1_, int p_render_2_, float p_render_3_) {
		this.renderBackground(poseStack);
		this.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 15, -1);

		this.font.drawShadow(poseStack, new TranslatableComponent("selectWorld.enterName"), this.width / 2 - 100, 37, -6250336);
		this.font.drawShadow(poseStack, new TranslatableComponent("selectWorld.resultFolder").append(" " + this.saveDirName), this.width / 2 - 100, 75, -6250336);
		this.worldNameField.render(poseStack, p_render_1_, p_render_2_, p_render_3_);

		this.drawCenteredString(poseStack, this.font, "Extra EyeMine Options (creative only):", this.width / 2, 95, -1);

		super.render(poseStack, p_render_1_, p_render_2_, p_render_3_);
	}

}
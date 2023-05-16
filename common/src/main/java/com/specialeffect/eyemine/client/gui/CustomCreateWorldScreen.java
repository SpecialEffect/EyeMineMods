///**
// * Copyright (C) 2016-2020 Kirsty McNaught
// * <p>
// * Developed for SpecialEffect, www.specialeffect.org.uk
// * <p>
// * This program is free software; you can redistribute it and/or
// * modify it under the terms of the GNU General Public License
// * as published by the Free Software Foundation; either version 3
// * of the License, or (at your option) any later version.
// */
//
//package com.specialeffect.eyemine.client.gui;
//
//import com.mojang.blaze3d.vertex.PoseStack;
//import com.mojang.datafixers.util.Pair;
//import com.mojang.serialization.Lifecycle;
//import com.specialeffect.eyemine.EyeMine;
//import com.specialeffect.eyemine.EyeMineClient;
//import com.specialeffect.eyemine.submod.misc.DefaultConfigForNewWorld;
//import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
//import net.minecraft.FileUtil;
//import net.minecraft.Util;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.components.Button;
//import net.minecraft.client.gui.components.EditBox;
//import net.minecraft.client.gui.components.toasts.SystemToast;
//import net.minecraft.client.gui.screens.ConfirmScreen;
//import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
//import net.minecraft.client.gui.screens.Screen;
//import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
//import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
//import net.minecraft.client.gui.screens.worldselection.WorldGenSettingsComponent;
//import net.minecraft.commands.Commands.CommandSelection;
//import net.minecraft.core.RegistryAccess;
//import net.minecraft.network.chat.Component;
//import net.minecraft.server.MinecraftServer;
//import net.minecraft.server.WorldLoader;
//import net.minecraft.server.packs.PackType;
//import net.minecraft.server.packs.repository.PackRepository;
//import net.minecraft.server.packs.repository.RepositorySource;
//import net.minecraft.server.packs.repository.ServerPacksSource;
//import net.minecraft.world.Difficulty;
//import net.minecraft.world.level.DataPackConfig;
//import net.minecraft.world.level.GameRules;
//import net.minecraft.world.level.GameType;
//import net.minecraft.world.level.LevelSettings;
//import net.minecraft.world.level.levelgen.WorldGenSettings;
//import net.minecraft.world.level.levelgen.presets.WorldPresets;
//import net.minecraft.world.level.storage.LevelResource;
//import net.minecraft.world.level.storage.LevelStorageSource;
//import net.minecraft.world.level.storage.PrimaryLevelData;
//import net.minecraft.world.level.storage.WorldData;
//import org.jetbrains.annotations.Nullable;
//
//import java.io.IOException;
//import java.io.UncheckedIOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.Comparator;
//import java.util.Objects;
//import java.util.Optional;
//import java.util.OptionalLong;
//import java.util.concurrent.CompletableFuture;
//import java.util.stream.Stream;
//
//public class CustomCreateWorldScreen extends Screen { TODO: Maybe turn this into a mixin instead of a new class?
//	private static final Component GAME_MODEL_LABEL = Component.translatable("selectWorld.gameMode");
//	private static final Component SEED_LABEL = Component.translatable("selectWorld.enterSeed");
//	private static final Component SEED_INFO = Component.translatable("selectWorld.seedInfo");
//	private static final Component NAME_LABEL = Component.translatable("selectWorld.enterName");
//	private static final Component OUTPUT_DIR_INFO = Component.translatable("selectWorld.resultFolder");
//	private static final Component COMMANDS_INFO = Component.translatable("selectWorld.allowCommands.info");
//	private static final Component PREPARING_WORLD_DATA = Component.translatable("createWorld.preparing");
//
//	private final Screen parentScreen;
//	private EditBox worldNameField;
//	private String saveDirName;
//	private boolean alreadyGenerated;
//	private Button btnCreateWorld;
//	private Component worldName;
//
//	// Buttons for EyeMine options
//	private BooleanButton btnDaytime;
//	private BooleanButton btnSunny;
//	private BooleanButton btnInventory;
//	public WorldGenSettingsComponent worldGenSettingsComponent;
//	@Nullable
//	private Path tempDataPackDir;
//
//	public CustomCreateWorldScreen(Screen screen, WorldGenSettingsComponent settings) {
//		super(Component.translatable("selectWorld.create"));
//		this.parentScreen = screen;
//		this.worldName = Component.translatable("selectWorld.newWorld");
//		this.worldGenSettingsComponent = settings;
//	}
//
//	public static CustomCreateWorldScreen create(Minecraft minecraft, @Nullable Screen screen) {
//		queueLoadScreen(minecraft, PREPARING_WORLD_DATA);
//		PackRepository packRepository = new PackRepository(PackType.SERVER_DATA, new RepositorySource[]{new ServerPacksSource()});
//		WorldLoader.InitConfig initConfig = createDefaultLoadConfig(packRepository, DataPackConfig.DEFAULT);
//		CompletableFuture<WorldCreationContext> completableFuture = WorldLoader.load(initConfig, (resourceManager, dataPackConfig) -> {
//			RegistryAccess.Frozen frozen = RegistryAccess.builtinCopy().freeze();
//			WorldGenSettings worldGenSettings = WorldPresets.createNormalWorldFromPreset(frozen);
//			return Pair.of(worldGenSettings, frozen);
//		}, (closeableResourceManager, reloadableServerResources, frozen, worldGenSettings) -> {
//			closeableResourceManager.close();
//			return new WorldCreationContext(worldGenSettings, Lifecycle.stable(), frozen, reloadableServerResources);
//		}, Util.backgroundExecutor(), minecraft);
//		Objects.requireNonNull(completableFuture);
//		minecraft.managedBlock(completableFuture::isDone);
//		return new CustomCreateWorldScreen(screen, new WorldGenSettingsComponent((WorldCreationContext) completableFuture.join(), Optional.of(WorldPresets.NORMAL), OptionalLong.empty()));
//	}
//
//	private static WorldLoader.InitConfig createDefaultLoadConfig(PackRepository packRepository, DataPackConfig dataPackConfig) {
//		WorldLoader.PackConfig packConfig = new WorldLoader.PackConfig(packRepository, dataPackConfig, false);
//		return new WorldLoader.InitConfig(packConfig, CommandSelection.INTEGRATED, 2);
//	}
//
//	public void tick() {
//		this.worldNameField.tick();
//	}
//
//	protected void init() {
//		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
//		this.worldNameField = new EditBox(this.font, this.width / 2 - 100, 50, 200, 20, Component.translatable("selectWorld.enterName"));
//		this.worldNameField.setValue(this.worldName.getString());
//		this.worldNameField.setResponder((p_214319_1_) -> {
//			this.worldName = Component.literal(p_214319_1_);
//			this.btnCreateWorld.active = !this.worldNameField.getValue().isEmpty();
//			this.calcSaveDirName();
//		});
//		this.addWidget(this.worldNameField);
//
//		this.btnCreateWorld = this.addRenderableWidget(new Button(this.width / 2 - 155, this.height - 28, 150, 20, Component.translatable("selectWorld.create"), (p_214318_1_) -> {
//			this.onCreate();
//		}));
//
//		// Buttons for EyeMine options, contain their own boolean state
//
//		String sDaytime = "Always daytime";
//		String sSunny = "Always sunny";
//		String sInventory = "Keep inventory after dying";
//
//		int w = (int) (this.font.width(sInventory) * 1.5);
//		btnDaytime = new BooleanButton(sDaytime, true, this.width / 2, 110, w);
//		btnSunny = new BooleanButton(sSunny, true, this.width / 2, 130, w);
//		btnInventory = new BooleanButton(sInventory, true, this.width / 2, 150, w);
//
//		this.addRenderableWidget(btnDaytime.getButton());
//		this.addRenderableWidget(btnSunny.getButton());
//		this.addRenderableWidget(btnInventory.getButton());
//
//		// More options -> back to the usual minecraft screens
//		this.addRenderableWidget(new Button(this.width / 2 - 75, 177, 150, 20, Component.translatable("Advanced Minecraft Options"), (p_214321_1_) -> {
//			EyeMineClient.allowMoreOptions = true;
//			DefaultConfigForNewWorld.setNewWorldOptions(btnDaytime.getValue(), btnSunny.getValue(), btnInventory.getValue());
//			CreateWorldScreen.openFresh(minecraft, this);
//		}));
//
//		this.addRenderableWidget(new Button(this.width / 2 + 5, this.height - 28, 150, 20, Component.translatable("gui.cancel"), (p_214317_1_) -> {
//			this.minecraft.setScreen(this.parentScreen);
//		}));
//		this.setInitialFocus(this.worldNameField);
//		this.calcSaveDirName();
//	}
//
//	static class BooleanButton {
//		private int x = 0;
//		private int y = 0;
//		private int w = 0;
//		private boolean val;
//		private String label;
//		private Button btn;
//
//		BooleanButton(String label, boolean defaultVal, int xPosCentre, int yPos, int width) {
//			this.label = label;
//			this.val = defaultVal;
//			this.x = xPosCentre - width / 2;
//			this.y = yPos;
//			this.w = width;
//
//			btn = new Button(x, y, w, 20, Component.literal(this.getLabel(val)), (b) -> {
//				this.toggle(b);
//			});
//		}
//
//		public Button getButton() {
//			return btn;
//		}
//
//		private String getLabel(boolean b) {
//			String msgBool = b ? "ON" : "OFF";
//			return label + ": " + msgBool;
//		}
//
//		private void toggle(Button b) {
//			val = !val;
//			this.btn.setMessage(Component.literal(this.getLabel(val)));
//		}
//
//		boolean getValue() {
//			return val;
//		}
//	}
//
//	/**
//	 * Determine a save-directory name from the world name
//	 */
//	private void calcSaveDirName() {
//		this.saveDirName = this.worldNameField.getValue().trim();
//		if (this.saveDirName.length() == 0) {
//			this.saveDirName = "World";
//		}
//
//		try {
//			this.saveDirName = FileUtil.findAvailableName(this.minecraft.getLevelSource().getBaseDir(), this.saveDirName, "");
//		} catch (Exception var4) {
//			this.saveDirName = "World";
//
//			try {
//				this.saveDirName = FileUtil.findAvailableName(this.minecraft.getLevelSource().getBaseDir(), this.saveDirName, "");
//			} catch (Exception exception) {
//				throw new RuntimeException("Could not create save folder", exception);
//			}
//		}
//
//	}
//
//	public void removed() {
//		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
//	}
//
//	private void onCreate() {
//		customConfirmWorldCreation(this.worldGenSettingsComponent.settings().worldSettingsStability(), this::createNewWorld);
//	}
//
//	public void customConfirmWorldCreation(Lifecycle lifecycle, Runnable runnable) {
//		BooleanConsumer booleanConsumer = (bl) -> {
//			if (bl) {
//				runnable.run();
//			} else {
//				minecraft.setScreen(this);
//			}
//
//		};
//		if (lifecycle == Lifecycle.stable()) {
//			runnable.run();
//		} else if (lifecycle == Lifecycle.experimental()) {
//			minecraft.setScreen(new ConfirmScreen(booleanConsumer, Component.translatable("selectWorld.import_worldgen_settings.experimental.title"), Component.translatable("selectWorld.import_worldgen_settings.experimental.question")));
//		} else {
//			minecraft.setScreen(new ConfirmScreen(booleanConsumer, Component.translatable("selectWorld.import_worldgen_settings.deprecated.title"), Component.translatable("selectWorld.import_worldgen_settings.deprecated.question")));
//		}
//	}
//
//	private void createNewWorld() {
//		queueLoadScreen(this.minecraft, PREPARING_WORLD_DATA);
//		DefaultConfigForNewWorld.setNewWorldOptions(btnDaytime.getValue(), btnSunny.getValue(), btnInventory.getValue());
//		Optional<LevelStorageSource.LevelStorageAccess> optional = this.createNewWorldDirectory();
//		if (!optional.isEmpty()) {
//			boolean hardcoreMode = false;
//
//			this.removeTempDataPackDir();
//			WorldCreationContext worldCreationContext = this.worldGenSettingsComponent.createFinalSettings(false);
//			LevelSettings levelSettings = new LevelSettings(this.worldNameField.getValue().trim(), GameType.CREATIVE, hardcoreMode, Difficulty.NORMAL, true, new GameRules(), DataPackConfig.DEFAULT);
//
//			GameRules gameRules = levelSettings.gameRules();
//			gameRules.getRule(GameRules.RULE_DAYLIGHT).set(!btnDaytime.getValue(), (MinecraftServer) null);
//			gameRules.getRule(GameRules.RULE_WEATHER_CYCLE).set(!btnSunny.getValue(), (MinecraftServer) null);
//			gameRules.getRule(GameRules.RULE_KEEPINVENTORY).set(btnInventory.getValue(), (MinecraftServer) null);
//
//			// TODO: more options here?
//			levelSettings.allowCommands();
//
//			WorldData worldData = new PrimaryLevelData(levelSettings, worldCreationContext.worldGenSettings(), worldCreationContext.worldSettingsStability());
//			this.minecraft.createWorldOpenFlows().createLevelFromExistingSettings((LevelStorageSource.LevelStorageAccess) optional.get(), worldCreationContext.dataPackResources(), worldCreationContext.registryAccess(), worldData);
//		}
//	}
//
//	private void removeTempDataPackDir() {
//		if (this.tempDataPackDir != null) {
//			try {
//				Stream<Path> stream = Files.walk(this.tempDataPackDir);
//
//				try {
//					stream.sorted(Comparator.reverseOrder()).forEach((path) -> {
//						try {
//							Files.delete(path);
//						} catch (IOException var2) {
//							EyeMine.LOGGER.warn("Failed to remove temporary file {}", path, var2);
//						}
//
//					});
//				} catch (Throwable var5) {
//					if (stream != null) {
//						try {
//							stream.close();
//						} catch (Throwable var4) {
//							var5.addSuppressed(var4);
//						}
//					}
//
//					throw var5;
//				}
//
//				if (stream != null) {
//					stream.close();
//				}
//			} catch (IOException var6) {
//				EyeMine.LOGGER.warn("Failed to list temporary dir {}", this.tempDataPackDir);
//			}
//
//			this.tempDataPackDir = null;
//		}
//	}
//
//	private Optional<LevelStorageSource.LevelStorageAccess> createNewWorldDirectory() {
//		try {
//			LevelStorageSource.LevelStorageAccess levelStorageAccess = this.minecraft.getLevelSource().createAccess(this.saveDirName);
//			if (this.tempDataPackDir == null) {
//				return Optional.of(levelStorageAccess);
//			}
//
//			try {
//				Stream<Path> stream = Files.walk(this.tempDataPackDir);
//
//				Optional var4;
//				try {
//					Path path = levelStorageAccess.getLevelPath(LevelResource.DATAPACK_DIR);
//					Files.createDirectories(path);
//					stream.filter((pathx) -> {
//						return !pathx.equals(this.tempDataPackDir);
//					}).forEach((path2) -> {
//						copyBetweenDirs(this.tempDataPackDir, path, path2);
//					});
//					var4 = Optional.of(levelStorageAccess);
//				} catch (Throwable var6) {
//					if (stream != null) {
//						try {
//							stream.close();
//						} catch (Throwable var5) {
//							var6.addSuppressed(var5);
//						}
//					}
//
//					throw var6;
//				}
//
//				if (stream != null) {
//					stream.close();
//				}
//
//				return var4;
//			} catch (UncheckedIOException | IOException var7) {
//				EyeMine.LOGGER.warn("Failed to copy datapacks to world {}", this.saveDirName, var7);
//				levelStorageAccess.close();
//			}
//		} catch (UncheckedIOException | IOException var8) {
//			EyeMine.LOGGER.warn("Failed to create access for {}", this.saveDirName, var8);
//		}
//
//		SystemToast.onPackCopyFailure(this.minecraft, this.saveDirName);
//		this.popScreen();
//		return Optional.empty();
//	}
//
//	private static void copyBetweenDirs(Path path, Path path2, Path path3) {
//		try {
//			Util.copyBetweenDirs(path, path2, path3);
//		} catch (IOException var4) {
//			EyeMine.LOGGER.warn("Failed to copy datapack file from {} to {}", path3, path2);
//			throw new UncheckedIOException(var4);
//		}
//	}
//
//	public void popScreen() {
//		this.minecraft.setScreen(this.parentScreen);
//		this.removeTempDataPackDir();
//	}
//
//	private static void queueLoadScreen(Minecraft minecraft, Component component) {
//		minecraft.forceSetScreen(new GenericDirtMessageScreen(component));
//	}
//
//	public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
//		if (super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_)) {
//			return true;
//		} else if (p_keyPressed_1_ != 257 && p_keyPressed_1_ != 335) {
//			return false;
//		} else {
//			this.onCreate();
//			return true;
//		}
//	}
//
//	public void render(PoseStack poseStack, int p_render_1_, int p_render_2_, float p_render_3_) {
//		this.renderBackground(poseStack);
//		this.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 15, -1);
//
//		this.font.drawShadow(poseStack, Component.translatable("selectWorld.enterName"), this.width / 2 - 100, 37, -6250336);
//		this.font.drawShadow(poseStack, Component.translatable("selectWorld.resultFolder").append(" " + this.saveDirName), this.width / 2 - 100, 75, -6250336);
//		this.worldNameField.render(poseStack, p_render_1_, p_render_2_, p_render_3_);
//
//		this.drawCenteredString(poseStack, this.font, "Extra EyeMine Options (creative only):", this.width / 2, 95, -1);
//
//		super.render(poseStack, p_render_1_, p_render_2_, p_render_3_);
//	}
//
//}
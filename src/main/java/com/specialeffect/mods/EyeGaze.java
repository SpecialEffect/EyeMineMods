/**
 * Copyright (C) 2018 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.mods;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.specialeffect.gui.CustomCreateWorldScreen;
import com.specialeffect.gui.CustomMainMenu;
import com.specialeffect.gui.StateOverlay;
import com.specialeffect.inventory.CreativeInventoryManager;
import com.specialeffect.mods.mining.ContinuouslyMine;
import com.specialeffect.mods.mining.GatherDrops;
import com.specialeffect.mods.mining.MineOne;
import com.specialeffect.mods.misc.AutoOpenDoors;
import com.specialeffect.mods.misc.AutoPillar;
import com.specialeffect.mods.misc.ContinuouslyAttack;
import com.specialeffect.mods.misc.DefaultConfigForNewWorld;
import com.specialeffect.mods.misc.IronSights;
import com.specialeffect.mods.misc.OpenChat;
import com.specialeffect.mods.misc.OpenTablesChests;
import com.specialeffect.mods.misc.PickBlock;
import com.specialeffect.mods.misc.QuickCommands;
import com.specialeffect.mods.misc.SwapMinePlace;
import com.specialeffect.mods.misc.UseItem;
import com.specialeffect.mods.mousehandling.MouseHandler;
import com.specialeffect.mods.moving.AutoFly;
import com.specialeffect.mods.moving.AutoJump;
import com.specialeffect.mods.moving.Dismount;
import com.specialeffect.mods.moving.EasyLadderClimb;
import com.specialeffect.mods.moving.MoveWithGaze;
import com.specialeffect.mods.moving.MoveWithGaze2;
import com.specialeffect.mods.moving.Sneak;
import com.specialeffect.mods.moving.Swim;
import com.specialeffect.mods.utils.DebugAverageFps;
import com.specialeffect.mods.utils.KeyWatcher;
import com.specialeffect.overrides.MovementInputFromOptionsOverride;
import com.specialeffect.utils.ChildModWithConfig;
import at.feldim2425.moreoverlays.gui.ConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.CreateWorldScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// This mod is purely a wrapper to cluster all our smaller mods.
// The parent (this mod) handles configuration so that it can all
// be changed by the user in one place.

@Mod(EyeGaze.MODID)
public class EyeGaze {
	/*
	 * In v1.11.2, this mod was a wrapper that contained user-exposed config, and
	 * acted as the 'parent' mod to all other mods This allowed us to expose config
	 * UI in one place, and not clutter up the Mods List (while simultaneously
	 * letting us have small self- contained mods for different features). In v1.14,
	 * the parent->child relationship doesn't exist any more, so this is a bit of a
	 * ghost town. We might consider updating it as our own uber-mod, with other
	 * mods registered to it.
	 * 
	 * FIXME: figure out what to do with it architecturally :-)
	 */

	public static final String MODID = "eyemine";
    
	public static final Logger LOGGER = LogManager.getLogger();
    
	public static EyeMineConfig mConfig;	
	public static MovementInputFromOptionsOverride ownMovementOverride;
	
	private StateOverlay mStateOverlay;		
	private static boolean setupComplete;		

	// Category names for clustering config options in different UIs
	private static List<ChildModWithConfig> childrenWithConfig = new ArrayList<ChildModWithConfig>();
	private static List<ChildMod> children = new ArrayList<ChildMod>();
	
	public EyeGaze() {

		// Only load things if on client - this means if mod is loaded on server, 
		// nothing happens.
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			// Register ourselves for server and other game events we are interested in
			MinecraftForge.EVENT_BUS.register(this);
	 
			this.setupConfig();
			
			// Setup GUI for showing state overlay
	        mStateOverlay = new StateOverlay(Minecraft.getInstance());
			MinecraftForge.EVENT_BUS.register(mStateOverlay);
	
			// Setup a class to handle overridden movement controls
			ownMovementOverride = new MovementInputFromOptionsOverride( Minecraft.getInstance().gameSettings);	
	
			// Setup all other mods
			this.instantiateChildren();		
			
			// Register this setup method *after* children have registered theirs 
			// (this way the children will be fully set up before any config gets loaded)
			FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
	        
			//Make sure the mod being absent on the other network side does not cause the client to display the server as incompatible				
			ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
			
            // Hook up config gui
            ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> this::openSettings);

		});
	}
	

    public void setup(final FMLCommonSetupEvent event) {
    	setupComplete = true;
    	refresh();
	}
    
	public static boolean allowMoreOptions = false;
	
	// Replace / augment some GUIs
	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event) {
		System.out.println(event.getGui());
		if (event.getGui() instanceof MainMenuScreen && !(event.getGui() instanceof CustomMainMenu)) {
			event.setGui(new CustomMainMenu());
		}
		if (event.getGui() instanceof CreateWorldScreen && !(event.getGui() instanceof CustomCreateWorldScreen)) {
			if (!allowMoreOptions) {
				// override the CreateWorldScreen, unless it's been requested from within our own CustomCreateWorldScreen
				event.setGui(new CustomCreateWorldScreen(Minecraft.getInstance().currentScreen));
			}
			allowMoreOptions = false;
		}
		if (event.getGui() instanceof CreativeScreen) {
			// Make sure mouse starts outside container, so we have a sensible reference point
			CreativeScreen gui = (CreativeScreen)event.getGui() ;
			CreativeInventoryManager con = CreativeInventoryManager.getInstance(
					gui.getGuiLeft(), gui.getGuiTop(), 
					gui.getXSize(), gui.getYSize(),
					gui.getSelectedTabIndex(),
					gui.getContainer());            	
			con.resetMouse();
		}
	}

	public Screen openSettings(Minecraft mc, Screen modlist){
		return new ConfigScreen(modlist, EyeMineConfig.CLIENT_CONFIG, MODID);
	}
    
	@SubscribeEvent()
    public void onClientTick(ClientTickEvent event) {
		ClientPlayerEntity player = Minecraft.getInstance().player;
    	if (null != player) {
            if (event.phase == TickEvent.Phase.START) {
            	
        		// The movement input class can be re-created when respawning or moving to a 
        		// different dimension, so we need to make sure it's checked always.
        		if (!(player.movementInput instanceof MovementInputFromOptionsOverride))
    			{
        			player.movementInput = ownMovementOverride;	
    			}
        		else {
        			// This shouldn't ever happen, but during beta testing I'd like to validate this assumption :)
        			if (player.movementInput != ownMovementOverride) {
        				LOGGER.error("Movement input class has changed unexpectedly");
        			}
        		}
            }
            else if (event.phase == TickEvent.Phase.END) {
                // Here we can handle any state conflicts from child mods after they've handled tick START phase
            	
            }
    	}
	}
	
	private void setupConfig() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, EyeMineConfig.CLIENT_CONFIG);

		EyeMineConfig.loadConfig(EyeMineConfig.CLIENT_CONFIG,
				FMLPaths.CONFIGDIR.get().resolve("eyemine-client.toml"));
	}

    private void instantiateChildren() {
        // In older versions of forge we had child/parent mods, here we have to set this up manually
        this.setupChildMod((ChildMod) new KeyWatcher());
        this.setupChildMod((ChildMod) new ContinuouslyMine());
        this.setupChildMod((ChildMod) new GatherDrops());
        this.setupChildMod((ChildMod) new MineOne());
        this.setupChildMod((ChildMod) new AutoOpenDoors());
        this.setupChildMod((ChildMod) new AutoPillar());
        this.setupChildMod((ChildMod) new ContinuouslyAttack());
        this.setupChildMod((ChildMod) new OpenChat());
        this.setupChildMod((ChildMod) new OpenTablesChests());
        this.setupChildMod((ChildMod) new PickBlock());
        this.setupChildMod((ChildMod) new QuickCommands());
        this.setupChildMod((ChildMod) new SwapMinePlace());
        this.setupChildMod((ChildMod) new UseItem());
        this.setupChildMod((ChildMod) new MouseHandler());
        this.setupChildMod((ChildMod) new AutoFly());
        this.setupChildMod((ChildMod) new AutoJump());
        this.setupChildMod((ChildMod) new Dismount());
        this.setupChildMod((ChildMod) new EasyLadderClimb());
        this.setupChildMod((ChildMod) new MoveWithGaze());
        this.setupChildMod((ChildMod) new Sneak());
        this.setupChildMod((ChildMod) new Swim());
        this.setupChildMod((ChildMod) new DebugAverageFps());
        this.setupChildMod((ChildMod) new DefaultConfigForNewWorld());
        this.setupChildMod((ChildMod) new IronSights());
        this.setupChildMod((ChildMod) new MoveWithGaze2());
        
    }
    
    private void setupChildMod(ChildMod mod) {
        MinecraftForge.EVENT_BUS.register(mod);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(mod::setup);
		FMLJavaModLoadingContext.get().getModEventBus().register(this); // for @SubscribeEvent annotations ??

        children.add(mod);        
    }

	public static void refresh() {
		if (setupComplete) { 
			for (ChildMod child : children) {
				if (child instanceof ChildModWithConfig) {
					ChildModWithConfig mod = (ChildModWithConfig)child;
					mod.syncConfig();
				}
			}
		}
	}

	public static void saveWalkingSpeed(float speed) {
		// FIXME: put this on EyeMineConfig class?
		EyeMineConfig.customSpeedFactor.set((double) speed);

		EyeGaze.refresh();
	}

}

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

package com.specialeffect.mods.tutorial;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.specialeffect.mods.ChildMod;
import com.specialeffect.mods.utils.KeyWatcher;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.block.material.Material;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class BlockObstruction extends ChildMod {
	public final String MODID = "blockobstruction";
	private static KeyBinding mKB;	
	private boolean blockingOn = false;
	
	// Warning after a certain amount of mining attempts
	private int msgTick = 0;
	private int obstructions = 0;
	private boolean haveWarned = false;
	
	List<Material> allowedMaterials;

	public void setup(final FMLCommonSetupEvent event) {
		// Register key bindings
		mKB = new KeyBinding("Toggle block obstruction", GLFW.GLFW_KEY_EQUAL, CommonStrings.EYEGAZE_EXTRA);
		ClientRegistry.registerKeyBinding(mKB);	
		
		// Set up allow-list for materials
		allowedMaterials = new ArrayList<Material>();
		allowedMaterials.add(Material.BAMBOO);		
	}

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {	

		if (ModUtils.hasActiveGui()) { return; }	    
		if (event.getAction() != GLFW.GLFW_PRESS) { return; }

		if (KeyWatcher.f3Pressed) { return; }

		if (mKB.getKey().getKeyCode() == event.getKey()) {
			blockingOn = !blockingOn;
			ModUtils.sendPlayerMessage("Blocking? "+blockingOn);			
		}
	}
	
	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {
		msgTick = Math.max(0, --msgTick);
	}
	
	@SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {

		World world = (World)event.getWorld();		
		if (world instanceof ClientWorld) {
			return; // we only get the required info on the server 
		}
		
		// We hard-code details for the demo world for which we want this mod to be active
		final long demoWorldSeed = 3444682897386308827L;
		final String demoWorldName = "§6 ** EyeMine Demo World **";			
		
		long worldSeed = world.getSeed();
		String worldName = world.getWorldInfo().getWorldName(); 
		
		// are we active?
		blockingOn = (worldSeed == demoWorldSeed) &&
					 (worldName.equals(demoWorldName));
		
		// general state reset
		obstructions = 0;
		msgTick = 0;
		haveWarned = false;
	}

	@SubscribeEvent
	public void onBreakEvent(BreakEvent event)
	{
		if (!blockingOn) {
			return;
		}
				
		Material material = event.getState().getMaterial();
		
		// If material is non-blocking (like grass), or specially allowed in the
		// permit-list, then we let people mine it. 
		// Any other 'proper blocking blocks' are not allowed to be mined
		
		boolean allowMining = !material.blocksMovement() || allowedMaterials.contains(material);
		
		if (allowMining) {
			obstructions = Math.max(0, --obstructions);
		}
		else {
			event.setCanceled(true);
		
			if (msgTick == 0) { // if no message visible
				final int firstWarningThreshold = 3;
				final int repeatWarningThreshold = 10;
				obstructions = ++obstructions % (repeatWarningThreshold+1);  // count blocked mining attempts

				if ( (!haveWarned && obstructions == firstWarningThreshold) ||
						obstructions == repeatWarningThreshold) {
					haveWarned = true;
					ModUtils.sendPlayerMessage("Mining solid blocks is disabled for this tutorial world");		
					msgTick = 300;
				}			
			}
			else {
				obstructions = 0;
			}
		}
	}

}

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

import org.lwjgl.glfw.GLFW;

import com.specialeffect.mods.utils.DwellAction;
import com.specialeffect.mods.utils.KeyWatcher;
import com.specialeffect.mods.utils.TargetBlock;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class DwellBuild 
extends DwellAction {
	
	public DwellBuild() {
		super("DWELL BUILDING"); 
	}

	public final String MODID = "dwellbuild";
		
	private static KeyBinding mDwellBuildKB;
	
	
	public void setup(final FMLCommonSetupEvent event) {

		// Register key bindings
		mDwellBuildKB = new KeyBinding("Dwell build", GLFW.GLFW_KEY_KP_3,
				CommonStrings.EYEGAZE_EXTRA);
		ClientRegistry.registerKeyBinding(mDwellBuildKB);

		this.syncConfig();
	}
	
	
	@Override
	public void performAction(TargetBlock block) {
		final KeyBinding useItemKeyBinding = Minecraft.getInstance().gameSettings.keyBindUseItem;
		KeyBinding.onTick(useItemKeyBinding.getKey());
	}
	
	
	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {
		
		if (ModUtils.hasActiveGui()) { return; }	    
	    if (event.getAction() != GLFW.GLFW_PRESS) { return; }

	    if (KeyWatcher.f3Pressed) { return; }
				
		if (event.getKey() == mDwellBuildKB.getKey().getKeyCode()) {
			PlayerEntity player = Minecraft.getInstance().player;	
			if (mDwelling) {				
				// Turn off dwell build
				this.setDwelling(false);
		        ModUtils.sendPlayerMessage("Dwell building: OFF");		        
			}
			else {
				// Turn on dwell build 						
				ItemStack itemStack = player.inventory.getCurrentItem();
				if (itemStack == null || itemStack.getItem() == null) {
			        player.sendMessage(new StringTextComponent("Nothing in hand to use"));
			        return;
				}
		        						
				this.setDwelling(true);															
				ModUtils.sendPlayerMessage("Dwell building: ON");					      
			}
		} 
	}	
}

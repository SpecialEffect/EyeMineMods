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

import com.specialeffect.mods.ChildMod;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class SwapMinePlace  extends ChildMod {
	public static final String MODID = "swapmineplace";
	public static final String NAME = "SwapMinePlace";
	//FIXME for 1.14 public static SimpleNetworkWrapper network;

	public void setup(final FMLCommonSetupEvent event) {

		// Register for server messages
		//FIXME network = NetworkRegistry.INSTANCE.newSimpleChannel(this.NAME);
		//FIXME network.registerMessage(SendCommandMessage.Handler.class, SendCommandMessage.class, 0, Side.SERVER);
	
		//init

		// Register key bindings
		mSwapKB = new KeyBinding("Swap mine/place keys", GLFW.GLFW_KEY_F10, CommonStrings.EYEGAZE_EXTRA);
		ClientRegistry.registerKeyBinding(mSwapKB);		
	}
	
	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (ModUtils.entityIsMe(event.getEntityLiving())) {
			
		}
	}

	private static KeyBinding mSwapKB;

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {	
		if (ModUtils.hasActiveGui()) { return; }
		
		if (mSwapKB.isPressed()) {
			
			/* FIXME: re-instate when fixed
			Input attackInput = Minecraft.getInstance().gameSettings.keyBindAttack.getKey();
			Input useInput = Minecraft.getInstance().gameSettings.keyBindUseItem.getKey();
			
			// FIXME: test this!
			Minecraft.getInstance().gameSettings.setKeyBindingCode(Minecraft.getInstance().gameSettings.keyBindAttack, useInput);
			Minecraft.getInstance().gameSettings.setKeyBindingCode(Minecraft.getInstance().gameSettings.keyBindUseItem, attackInput);

			ModUtils.sendPlayerMessage("Swapping mine and place keys");
			
			
			ModUtils.sendPlayerMessage("Swap mine/place not implemented yet!");
			*/
		}
	}
}

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

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.mods.ChildMod;
//import com.specialeffect.messages.SendCommandMessage;
import com.specialeffect.mods.EyeGaze;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class SwapMinePlace extends BaseClassWithCallbacks implements ChildMod {
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
			this.processQueuedCallbacks(event);
		}
	}

	private static KeyBinding mSwapKB;

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {	
        
		if (mSwapKB.isPressed()) {
			
			/* FIXME: re-instate when fixed
			Input attackInput = Minecraft.getInstance().gameSettings.keyBindAttack.getKey();
			Input useInput = Minecraft.getInstance().gameSettings.keyBindUseItem.getKey();
			
			// FIXME: test this!
			Minecraft.getInstance().gameSettings.setKeyBindingCode(Minecraft.getInstance().gameSettings.keyBindAttack, useInput);
			Minecraft.getInstance().gameSettings.setKeyBindingCode(Minecraft.getInstance().gameSettings.keyBindUseItem, attackInput);

			this.queueChatMessage("Swapping mine and place keys");
			
			
			this.queueChatMessage("Swap mine/place not implemented yet!");
			*/
		}
	}
}

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
//import com.specialeffect.messages.SendCommandMessage;
import com.specialeffect.mods.EyeGaze;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.java.games.input.Keyboard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings.Input;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;

@Mod(SwapMinePlace.MODID)
public class SwapMinePlace extends BaseClassWithCallbacks {
	public static final String MODID = "swapmineplace";
	public static final String NAME = "SwapMinePlace";
	//FIXME for 1.14 public static SimpleNetworkWrapper network;

	public SwapMinePlace() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
	}
	
	private void setup(final FMLCommonSetupEvent event) {
		//preinit
		MinecraftForge.EVENT_BUS.register(this);

		ModUtils.setupModInfo(event, this.MODID, this.NAME, "Add key binding to swap mine/place key bindings.");
		ModUtils.setAsParent(event, EyeGaze.MODID);

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
    public void onClientTickEvent(final ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
		if (mSwapKB.isPressed()) {
			Input attackInput = Minecraft.getInstance().gameSettings.keyBindAttack.getKey();
			Input useInput = Minecraft.getInstance().gameSettings.keyBindUseItem.getKey();
			
			// FIXME: test this!
			Minecraft.getInstance().gameSettings.setKeyBindingCode(Minecraft.getInstance().gameSettings.keyBindAttack, useInput);
			Minecraft.getInstance().gameSettings.setKeyBindingCode(Minecraft.getInstance().gameSettings.keyBindUseItem, attackInput);

			this.queueChatMessage("Swapping mine and place keys");			
		}
	}
}

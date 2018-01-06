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

import org.lwjgl.input.Keyboard;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

@Mod(modid = UseItem.MODID, version = ModUtils.VERSION, name = UseItem.NAME)
public class UseItem extends BaseClassWithCallbacks {
	public static final String MODID = "specialeffect.useitem";
	public static final String NAME = "UseItem";

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(this);

		ModUtils.setupModInfo(event, this.MODID, this.NAME, "Add key binding to use item without mouse");
		ModUtils.setAsParent(event, SpecialEffectMisc.MODID);

	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// Subscribe to event buses
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);

		// Register key bindings
		mUseItemOnceKB = new KeyBinding("Use Item", Keyboard.KEY_NUMPAD0, CommonStrings.EYEGAZE_COMMON);
		ClientRegistry.registerKeyBinding(mUseItemOnceKB);

		mUseItemContinuouslyKB = new KeyBinding("Use Item Continuously", Keyboard.KEY_NUMPAD1, CommonStrings.EYEGAZE_EXTRA);
		ClientRegistry.registerKeyBinding(mUseItemContinuouslyKB);

	}

	private static KeyBinding mUseItemOnceKB;
	private static KeyBinding mUseItemContinuouslyKB;


	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (ModUtils.entityIsMe(event.getEntityLiving())) {
			this.processQueuedCallbacks(event);
		}
	}

	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		
		final KeyBinding useItemKeyBinding = Minecraft.getMinecraft().gameSettings.keyBindUseItem;
				
		if (mUseItemContinuouslyKB.isPressed()) {
			boolean useItemNewState = !useItemKeyBinding.isKeyDown();
			KeyBinding.setKeyBindState(useItemKeyBinding.getKeyCode(), useItemNewState);			
			this.queueChatMessage("Using item: " + (useItemNewState ? "ON" : "OFF"));
		}
		else if (mUseItemOnceKB.isPressed()) {
			KeyBinding.onTick(useItemKeyBinding.getKeyCode());
		}
	}
}

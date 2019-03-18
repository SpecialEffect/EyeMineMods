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
import com.specialeffect.mods.EyeGaze;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

@Mod(PickBlock.MODID)
public class PickBlock extends BaseClassWithCallbacks {
	public static final String MODID = "specialeffect.pickblock";
	public static final String NAME = "PickBlock";

	@EventHandler
	@SuppressWarnings("static-access")
	public void preInit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);

		ModUtils.setupModInfo(event, this.MODID, this.NAME, "Add key binding to pick block without mouse.");
		ModUtils.setAsParent(event, EyeGaze.MODID);

	}

	@EventHandler
	public void init(FMLInitializationEvent event) {

		// Register key bindings
		mPickBlockKB = new KeyBinding("Pick block", Keyboard.KEY_NUMPAD2, CommonStrings.EYEGAZE_COMMON);
		ClientRegistry.registerKeyBinding(mPickBlockKB);

	}

	private static KeyBinding mPickBlockKB;


	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (ModUtils.entityIsMe(event.getEntityLiving())) {
			this.processQueuedCallbacks(event);
		}
	}

	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		final int pickBlockKeyCode = Minecraft.getMinecraft().gameSettings.keyBindPickBlock.getKeyCode();
		if (mPickBlockKB.isPressed()) {
			KeyBinding.onTick(pickBlockKeyCode);
		}
	}
}

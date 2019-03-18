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

import java.awt.event.KeyEvent;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.mods.EyeGaze;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings.Input;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(PickBlock.MODID)
public class PickBlock extends BaseClassWithCallbacks {
	public static final String MODID = "specialeffect.pickblock";
	public static final String NAME = "PickBlock";

	
	public PickBlock() {
		// Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);        
    }
	
	
    private void setup(final FMLCommonSetupEvent event) {

    	// preinit
    
	    MinecraftForge.EVENT_BUS.register(this);
	
		ModUtils.setupModInfo(event, this.MODID, this.NAME, "Add key binding to pick block without mouse.");
		ModUtils.setAsParent(event, EyeGaze.MODID);
	
		// init
		
		// Register key bindings
		mPickBlockKB = new KeyBinding("Pick block", KeyEvent.VK_NUMPAD2, CommonStrings.EYEGAZE_COMMON);
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
		final Input pickBlockKey = Minecraft.getInstance().gameSettings.keyBindPickBlock.getKey();
		if (mPickBlockKB.isPressed()) {
			KeyBinding.onTick(pickBlockKey);
		}
	}
}

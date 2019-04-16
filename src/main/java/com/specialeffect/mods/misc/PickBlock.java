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
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

//import 
//import net.java.games.input.Keyboard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings.Input;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.MouseInputEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@EventBusSubscriber(modid = PickBlock.MODID) 
@Mod(PickBlock.MODID)
public class PickBlock extends BaseClassWithCallbacks {
	public static final String MODID = "pickblock";
	public static final String NAME = "PickBlock";

	
	public PickBlock() {
		// Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);        
             
    }
	
	
    private void setup(final FMLCommonSetupEvent event) {

    	System.out.println("pickblock setup2");
    	// preinit
    
	    MinecraftForge.EVENT_BUS.register(this);
	    
		//ModUtils.setupModInfo(event, this.MODID, this.NAME, "Add key binding to pick block without mouse.");
		//ModUtils.setAsParent(event, EyeGaze.MODID);
	
		// init
		
		// Register key bindings
		mPickBlockKB = new KeyBinding("Pick block", KeyEvent.VK_NUMPAD2, CommonStrings.EYEGAZE_COMMON);
		ClientRegistry.registerKeyBinding(mPickBlockKB);

	}

	private static KeyBinding mPickBlockKB;

	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		//System.out.println("pickblock onLiving");

		if (ModUtils.entityIsMe(event.getEntityLiving())) {
			this.processQueuedCallbacks(event);
		}
	}

	this isn't currently firing, see issue: https://github.com/MinecraftForge/MinecraftForge/issues/5481
	
	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {
		System.out.println("pickblock2 onKeyInput");
		if (mPickBlockKB.isPressed()) {
			final Input pickBlockKey = Minecraft.getInstance().gameSettings.keyBindPickBlock.getKey();
			System.out.println("pickblock onKeykey is pressed");
			KeyBinding.onTick(pickBlockKey);
		}
	}
	
	@SubscribeEvent
	public void onMouseInput(MouseInputEvent event) {
		System.out.println("pickblock2 onMouseInput");
	}
	
	@SubscribeEvent
	public void onBlock(BlockEvent event) {
//		System.out.println("pickblock2 BlockEvent");
	}

	@SubscribeEvent
	public void onInput(InputEvent event) {
		System.out.println("pickblock2 InputEvent");
	}
	
}

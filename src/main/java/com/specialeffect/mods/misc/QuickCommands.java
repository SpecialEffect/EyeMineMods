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

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class QuickCommands  extends ChildMod {
	public static final String MODID = "quickcommands";
	public static final String NAME = "QuickCommands";
    //FIXME for 1.14 public static SimpleNetworkWrapper network;

	public void setup(final FMLCommonSetupEvent event) {
		
		// Register for server messages
		//FIXME network = NetworkRegistry.INSTANCE.newSimpleChannel(this.NAME);
        //FIXME network.registerMessage(SendCommandMessage.Handler.class, 
        		//				SendCommandMessage.class, 0, Side.SERVER);


		// init
		
		// Register key bindings
		mNightVisionKB = new KeyBinding("Turn night vision on/off", GLFW.GLFW_KEY_F12, CommonStrings.EYEGAZE_EXTRA);
		ClientRegistry.registerKeyBinding(mNightVisionKB);

		mDayNightKB = new KeyBinding("Turn day/night cycle on/off", GLFW.GLFW_KEY_F14, CommonStrings.EYEGAZE_EXTRA);
		ClientRegistry.registerKeyBinding(mDayNightKB);

	}

	private static KeyBinding mNightVisionKB;
	private static KeyBinding mDayNightKB;

	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {
    PlayerEntity player = Minecraft.getInstance().player;
		if (null != player) {
			
		}
	}

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {		
		
		if (mNightVisionKB.isPressed()) {
			// Toggle night vision effect
			PlayerEntity player = Minecraft.getInstance().player;
			Effect nightVision = Effects.NIGHT_VISION;
			
			if (player.isPotionActive(nightVision)) {
				player.removePotionEffect(nightVision);
			}
			else {
				player.addPotionEffect(new EffectInstance(nightVision)); 							
			}		
		}
		
		if (ModUtils.hasActiveGui()) { return; }

		if (mDayNightKB.isPressed()) {
//			this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving() {
//				
//				@Override
//				public void onClientTick(ClientTickEvent event) {
    PlayerEntity player = Minecraft.getInstance().player;
//					String gameRule = "doDaylightCycle";
//					GameRules rules = Minecraft.getInstance().world.getGameRules();	
//					// FIXME boolean newBool = !rules.getBoolean(gameRule);
//					
//					// Ask server to change gamerule
//					// FIXME String cmd = "/gamerule " + gameRule + " " + Boolean.toString(newBool);
//					//FIXME QuickCommands.network.sendToServer(new SendCommandMessage(cmd));
//				}
//			}));			
		}
	}
}

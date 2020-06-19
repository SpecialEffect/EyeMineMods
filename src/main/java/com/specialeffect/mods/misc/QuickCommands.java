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

import com.specialeffect.messages.SendCommandMessage;
import com.specialeffect.mods.ChildMod;
import com.specialeffect.mods.utils.KeyWatcher;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.BooleanValue;
import net.minecraft.world.GameRules.RuleKey;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class QuickCommands extends ChildMod {
	public final String MODID = "quickcommands";

	public void setup(final FMLCommonSetupEvent event) {
		
		// setup channel for comms
		this.setupChannel(MODID, 1);

        int id = 0;		       
        channel.registerMessage(id++, SendCommandMessage.class, SendCommandMessage::encode, 
        		SendCommandMessage::decode, SendCommandMessage.Handler::handle);        
   
		        

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
	public void onKeyInput(KeyInputEvent event) {		
		
	    if (event.getAction() != GLFW.GLFW_PRESS) { return; }

	    if (KeyWatcher.f3Pressed) { return; }

		if (mNightVisionKB.getKey().getKeyCode() == event.getKey()) {
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

		if (mDayNightKB.getKey().getKeyCode() == event.getKey()) {
			GameRules rules = Minecraft.getInstance().world.getGameRules();
			
			
			RuleKey<BooleanValue> gameRule = GameRules.DO_DAYLIGHT_CYCLE;			
			boolean newBool = !rules.getBoolean(gameRule);
			
			String cmd = "/gamerule " + gameRule + " " + Boolean.toString(newBool);			
			channel.sendToServer(new SendCommandMessage(cmd));
		}
	}
}

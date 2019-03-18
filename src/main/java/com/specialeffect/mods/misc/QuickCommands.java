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
import com.specialeffect.callbacks.IOnLiving;
import com.specialeffect.callbacks.SingleShotOnLivingCallback;
import com.specialeffect.messages.SendCommandMessage;
import com.specialeffect.mods.EyeGaze;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.GameRules;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = QuickCommands.MODID, version = ModUtils.VERSION, name = QuickCommands.NAME)
public class QuickCommands extends BaseClassWithCallbacks {
	public static final String MODID = "specialeffect.quickcommands";
	public static final String NAME = "QuickCommands";
    public static SimpleNetworkWrapper network;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);

		ModUtils.setupModInfo(event, this.MODID, this.NAME, "Add key bindings for some useful commands.");
		ModUtils.setAsParent(event, EyeGaze.MODID);
		
		// Register for server messages
		network = NetworkRegistry.INSTANCE.newSimpleChannel(this.NAME);
        network.registerMessage(SendCommandMessage.Handler.class, 
        						SendCommandMessage.class, 0, Side.SERVER);


	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// Register key bindings
		mNightVisionKB = new KeyBinding("Turn night vision on/off", Keyboard.KEY_F12, CommonStrings.EYEGAZE_EXTRA);
		ClientRegistry.registerKeyBinding(mNightVisionKB);

		mDayNightKB = new KeyBinding("Turn day/night cycle on/off", Keyboard.KEY_F14, CommonStrings.EYEGAZE_EXTRA);
		ClientRegistry.registerKeyBinding(mDayNightKB);

	}

	private static KeyBinding mNightVisionKB;
	private static KeyBinding mDayNightKB;

	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (ModUtils.entityIsMe(event.getEntityLiving())) {
			this.processQueuedCallbacks(event);
		}
	}

	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		if (mNightVisionKB.isPressed()) {
			this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving() {
				@Override
				public void onLiving(LivingUpdateEvent event) {
					if (ModUtils.entityIsMe(event.getEntityLiving())) {
						EntityPlayer player = (EntityPlayer) event.getEntityLiving();
						Potion nightVision = MobEffects.NIGHT_VISION;
						if (player.isPotionActive(nightVision)) {
							System.out.println("clearing");
							player.removePotionEffect(nightVision);
						}
						else {
							System.out.println("night vision");
							player.addPotionEffect(new PotionEffect(nightVision));
						}
					}
				}
			}));
		}
		if (mDayNightKB.isPressed()) {
			this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving() {
				
				@Override
				public void onLiving(LivingUpdateEvent event) {
					String gameRule = "doDaylightCycle";
					GameRules rules = Minecraft.getMinecraft().world.getGameRules();	
					boolean newBool = !rules.getBoolean(gameRule);
					
					// Ask server to change gamerule
					String cmd = "/gamerule " + gameRule + " " + Boolean.toString(newBool);
					QuickCommands.network.sendToServer(new SendCommandMessage(cmd));
				}
			}));			
		}
	}
}

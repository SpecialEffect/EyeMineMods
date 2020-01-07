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
import com.specialeffect.callbacks.IOnLiving;
import com.specialeffect.callbacks.SingleShotOnLivingCallback;
//import com.specialeffect.messages.SendCommandMessage;
import com.specialeffect.mods.EyeGaze;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.java.games.input.Keyboard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Potion;
import net.minecraft.world.GameRules;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
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

@Mod(QuickCommands.MODID)
public class QuickCommands extends BaseClassWithCallbacks {
	public static final String MODID = "quickcommands";
	public static final String NAME = "QuickCommands";
    //FIXME for 1.14 public static SimpleNetworkWrapper network;

	public QuickCommands() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
	}
	
	private void setup(final FMLCommonSetupEvent event) {
		//preinit
		MinecraftForge.EVENT_BUS.register(this);

		ModUtils.setupModInfo(event, this.MODID, this.NAME, "Add key bindings for some useful commands.");
		ModUtils.setAsParent(event, EyeGaze.MODID);
		
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
	public void onLiving(LivingUpdateEvent event) {
		if (ModUtils.entityIsMe(event.getEntityLiving())) {
			this.processQueuedCallbacks(event);
		}
	}

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {
		if (mNightVisionKB.isPressed()) {
			this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving() {
				@Override
				public void onLiving(LivingUpdateEvent event) {
					if (ModUtils.entityIsMe(event.getEntityLiving())) {
						PlayerEntity player = (PlayerEntity) event.getEntityLiving();

						/* FIXME
						Potion nightVision = MobEffects.NIGHT_VISION;
						if (player.isPotionActive(nightVision)) {
							System.out.println("clearing");
							player.removePotionEffect(nightVision);
						}
						else {
							System.out.println("night vision");
							player.addPotionEffect(new PotionEffect(nightVision));
						}*/
					}
				}
			}));
		}
		if (mDayNightKB.isPressed()) {
			this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving() {
				
				@Override
				public void onLiving(LivingUpdateEvent event) {
					String gameRule = "doDaylightCycle";
					GameRules rules = Minecraft.getInstance().world.getGameRules();	
					// FIXME boolean newBool = !rules.getBoolean(gameRule);
					
					// Ask server to change gamerule
					// FIXME String cmd = "/gamerule " + gameRule + " " + Boolean.toString(newBool);
					//FIXME QuickCommands.network.sendToServer(new SendCommandMessage(cmd));
				}
			}));			
		}
	}
}

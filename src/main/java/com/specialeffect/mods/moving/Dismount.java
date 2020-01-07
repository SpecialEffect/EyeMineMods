/**
 * Copyright (C) 2016 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.mods.moving;

import org.lwjgl.glfw.GLFW;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.callbacks.IOnLiving;
import com.specialeffect.callbacks.SingleShotOnLivingCallback;
//import com.specialeffect.messages.DismountPlayerMessage;
import com.specialeffect.mods.EyeGaze;
import com.specialeffect.mods.mining.GatherDrops;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.java.games.input.Keyboard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings.Input;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
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


@Mod(Dismount.MODID)

public class Dismount extends BaseClassWithCallbacks {

	public static final String MODID = "dismount";
	public static final String NAME = "Dismount";

	private static KeyBinding mDismountKB;
	
    //FIXME for 1.14 public static SimpleNetworkWrapper network;

	public Dismount() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
	}
	
	private void setup(final FMLCommonSetupEvent event) {
		// preinit
		MinecraftForge.EVENT_BUS.register(this);

		ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Add custom key binding to mount/dismount animals");
    	ModUtils.setAsParent(event, EyeGaze.MODID);

		//FIXME network = NetworkRegistry.INSTANCE.newSimpleChannel(this.NAME);
        //FIXME network.registerMessage(DismountPlayerMessage.Handler.class, 
        						//DismountPlayerMessage.class, 0, Side.SERVER);

        // init
		
		// Register key bindings
		mDismountKB = new KeyBinding("Ride or dismount", GLFW.GLFW_KEY_C, CommonStrings.EYEGAZE_EXTRA);
		ClientRegistry.registerKeyBinding(mDismountKB);
	}

	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (ModUtils.entityIsMe(event.getEntityLiving())) {
			this.processQueuedCallbacks(event);
		}
	}

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {
        
		if(mDismountKB.isPressed()) {
			// Dismount player locally
			this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving() {
				@Override
				public void onLiving(LivingUpdateEvent event) {
					PlayerEntity player = (PlayerEntity)event.getEntityLiving();

					if (player.isPassenger()) {
						player.detach();						
						// FIXME??	player.motionY += 0.5D;
						
						// Dismount player on server
						//FIXME Dismount.network.sendToServer(
								//new DismountPlayerMessage());						
					}
					else {
						RayTraceResult mov = Minecraft.getInstance().objectMouseOver;						
						
						if (mov != null) {
							// FIXME: test for 1.14
							if (mov.getType() == Type.ENTITY) {
						
								// FIXME: see if there's a better way to do this now
								//
								// Riding entity programmatically seems to not do everything that 
								// a "Use" action would do, so we:
								// - drop current item to ensure empty hand
								// - "use" entity you're pointing at
								// - pick up dropped item again
								player.dropItem(true);
								Input useItemKeyCode = Minecraft.getInstance().gameSettings.keyBindUseItem.getKey();
								KeyBinding.onTick(useItemKeyCode);
								GatherDrops.gatherBlocks(player);							
							}
						}
					}
				}
			}));	
		}
	}

}

/**
 * Copyright (C) 2016 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.mods.mining;

import java.util.ArrayList;

import org.lwjgl.input.Keyboard;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.callbacks.IOnLiving;
import com.specialeffect.callbacks.SingleShotOnLivingCallback;
import com.specialeffect.messages.PickBlockMessage;
import com.specialeffect.mods.EyeGaze;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = GatherDrops.MODID, 
version = ModUtils.VERSION,
name = GatherDrops.NAME)
public class GatherDrops extends BaseClassWithCallbacks
{

	public static final String MODID = "specialeffect.gatherdrops";
	public static final String NAME = "GatherDrops";

	public static Configuration mConfig;
	private static KeyBinding mGatherKB;

	public static SimpleNetworkWrapper network;

	@EventHandler
	@SuppressWarnings("static-access")
	public void preInit(FMLPreInitializationEvent event) {    
		MinecraftForge.EVENT_BUS.register(this);    	

		ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Add key binding to gather nearby dropped items.");
		ModUtils.setAsParent(event, EyeGaze.MODID);

		network = NetworkRegistry.INSTANCE.newSimpleChannel(this.NAME);
		network.registerMessage(PickBlockMessage.Handler.class, 
				PickBlockMessage.class, 0, Side.SERVER);

		// Set up config
		mConfig = new Configuration(event.getSuggestedConfigurationFile());
		this.syncConfig();
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		// Register key bindings	
		mGatherKB = new KeyBinding("Gather dropped items", Keyboard.KEY_X, CommonStrings.EYEGAZE_EXTRA);
		ClientRegistry.registerKeyBinding(mGatherKB);
	}

	@SubscribeEvent
	@SuppressWarnings("static-access")
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if(eventArgs.getModID().equals(this.MODID)) {
			syncConfig();
		}
	}

	public static void syncConfig() {
		if (mConfig.hasChanged()) {
			mConfig.save();
		}
	}

	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (ModUtils.entityIsMe(event.getEntityLiving())) {
			this.processQueuedCallbacks(event);
		}			
	}

	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		if(mGatherKB.isPressed()) {

			this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving() {
				@Override
				public void onLiving(LivingUpdateEvent event) {
					EntityPlayer player = (EntityPlayer) event.getEntityLiving();
					GatherDrops.gatherBlocks(player);
				}
			}));
		}
	}
	
	public static void gatherBlocks(EntityPlayer player) {
		World world = Minecraft.getMinecraft().world;

		BlockPos playerPos = player.getPosition();
		double dx, dy, dz;
		dx = dy = dz = 5;

		AxisAlignedBB aaBb = new AxisAlignedBB(playerPos.subtract(new Vec3i(dx, dy, dz)), 
				playerPos.add(new Vec3i(dx, dy, dz)));
		ArrayList<EntityItem> items = (ArrayList<EntityItem>)world.getEntitiesWithinAABB(EntityItem.class,aaBb);

		if(items != null && !items.isEmpty()) {
			System.out.println("gathering " + items.size() + " nearby items");
			// Ask server to move items
			for (int i = 0; i < items.size(); i++) {
				GatherDrops.network.sendToServer(
						new PickBlockMessage(items.get(i).getEntityId()));
			}
		}
	}

}

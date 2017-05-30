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
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = GatherDrops.MODID, 
version = ModUtils.VERSION,
name = GatherDrops.NAME)
public class GatherDrops extends BaseClassWithCallbacks
{

	public static final String MODID = "specialeffect.GatherDrops";
	public static final String NAME = "GatherDrops";

	public static Configuration mConfig;
	private static KeyBinding mGatherKB;
	private static KeyBinding mOpenCraftingTableKB;	

	public static SimpleNetworkWrapper network;

	private static int mRadius = 5;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {    
		FMLCommonHandler.instance().bus().register(this);    	

		ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Add key binding to gather nearby dropped items.");
		ModUtils.setAsParent(event, SpecialEffectMining.MODID);

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
		// Subscribe to event buses
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);    	

		// Register key bindings	
		mGatherKB = new KeyBinding("Gather items", Keyboard.KEY_X, "SpecialEffect");
		ClientRegistry.registerKeyBinding(mGatherKB);
	}

	@SubscribeEvent
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
			}));
		}
	}

}

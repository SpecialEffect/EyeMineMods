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

import java.util.Iterator;
import java.util.LinkedList;

import com.specialeffect.messages.UseDoorAtPositionMessage;
import com.specialeffect.mods.EyeGaze;
import com.specialeffect.mods.EyeMineConfig;
import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.utils.ModUtils;
import com.specialeffect.utils.OpenableBlock;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

@Mod(AutoOpenDoors.MODID)
public class AutoOpenDoors 
implements ChildModWithConfig
{
	public static final String MODID = "autoopendoors";
	public static final String NAME = "AutoOpenDoors";
	private static final String PROTOCOL_VERSION = Integer.toString(1);

    public static SimpleChannel channel;

    public AutoOpenDoors() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }

	@SuppressWarnings("static-access")
	private void setup(final FMLCommonSetupEvent event) {
		MinecraftForge.EVENT_BUS.register(this);

		// preinit
		ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Automatically open doors/gates and close them behind you.");
		ModUtils.setAsParent(event, EyeGaze.MODID);

		// setup channel for comms
		channel = NetworkRegistry.newSimpleChannel(
                new ResourceLocation("specialeffect","autoopendoors")
                ,() -> PROTOCOL_VERSION
                , PROTOCOL_VERSION::equals
                , PROTOCOL_VERSION::equals);
        int id = 0; 
        channel.registerMessage(id++, UseDoorAtPositionMessage.class, UseDoorAtPositionMessage::encode, 
        		UseDoorAtPositionMessage::decode, UseDoorAtPositionMessage.Handler::handle);                   	       
		
		// Register for config changes from parent
		EyeGaze.registerForConfigUpdates((ChildModWithConfig)this);
		
		mOpenedDoors = new LinkedList<BlockPos>();
	}
	
	public void syncConfig() {
        mDoorRadius = EyeMineConfig.mRadiusDoors.get();
	}
	
	// A list of the position of any doors we've opened that haven't yet been closed
	private LinkedList<BlockPos> mOpenedDoors;

	private static int mDoorRadius = 3;
	private BlockPos mLastPlayerPos;
	
	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (ModUtils.entityIsMe(event.getEntityLiving())) {
			PlayerEntity player = (PlayerEntity) event.getEntityLiving();
			World world = Minecraft.getInstance().world;

			BlockPos playerPos = player.getPosition();

			// Only check/update doors when (integer) position has changed. 
			if (mLastPlayerPos == null || mLastPlayerPos != playerPos) {
				mLastPlayerPos = playerPos;

				// Open any doors within 1 block
				synchronized (mOpenedDoors) {
					for (int x = -mDoorRadius; x <= mDoorRadius; x++) {
						for (int z = -mDoorRadius; z <= mDoorRadius; z++) {
							for (int y = -1; y <= 1; y++) { // look up/down for trapdoors
								
								BlockPos blockPos = playerPos.add(x, y, z);

								// For symmetry with door closing, we actually want to test a circular
								// area, not a square.
								if (playerPos.distanceSq(new Vec3i(blockPos.getX(), blockPos.getY(), blockPos.getZ()))
										<= mDoorRadius*mDoorRadius) {

									// Check if block is door, if so, activate it.
									Block block = world.getBlockState(blockPos).getBlock();

									if (OpenableBlock.isOpenableBlock(block)) {
										boolean haveOpened = OpenableBlock.open(world, block, blockPos);
										if (haveOpened) {
											mOpenedDoors.add(blockPos);

											// Ask server to open door too
											channel.sendToServer(new UseDoorAtPositionMessage(blockPos, true));
										}
									}
								}
							}
						}
					}
				}

				// Close any doors that you've left behind
				synchronized (mOpenedDoors) {
					for (Iterator<BlockPos> iterator = mOpenedDoors.iterator(); iterator.hasNext();) {
						BlockPos pos = iterator.next();

						if (playerPos.distanceSq(new Vec3i(pos.getX(), pos.getY(), pos.getZ())) > mDoorRadius*mDoorRadius) {
							Block block = world.getBlockState(pos).getBlock();

							OpenableBlock.close(world, block, pos);

							// Ask server to close door too
							channel.sendToServer(new UseDoorAtPositionMessage(pos, false));

							// Remove from list
							iterator.remove();
						}
					}
				}
			}
			
		}
	}

}

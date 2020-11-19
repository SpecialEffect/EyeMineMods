/**
 * Copyright (C) 2016-2020 Kirsty McNaught
 * 
 * Developed for SpecialEffect, www.specialeffect.org.uk
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
import com.specialeffect.mods.ChildMod;
import com.specialeffect.mods.EyeMineConfig;
import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.utils.OpenableBlock;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class AutoOpenDoors 
extends ChildMod implements ChildModWithConfig
{
	public final String MODID = "autoopendoors";

	public void setup(final FMLCommonSetupEvent event) {
		
		// setup channel for comms
		this.setupChannel(MODID, 1);        

		int id = 0; 
        channel.registerMessage(id++, UseDoorAtPositionMessage.class, UseDoorAtPositionMessage::encode, 
        		UseDoorAtPositionMessage::decode, UseDoorAtPositionMessage.Handler::handle);                   	       
		
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
	public void onClientTick(ClientTickEvent event) {
    	PlayerEntity player = Minecraft.getInstance().player;
    	if (null != player && event.phase == TickEvent.Phase.START) {

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

						double closeRadius = mDoorRadius + 0.1; // avoids any jumpiness between states 
						if (playerPos.distanceSq(new Vec3i(pos.getX(), pos.getY(), pos.getZ())) > closeRadius*closeRadius) {
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

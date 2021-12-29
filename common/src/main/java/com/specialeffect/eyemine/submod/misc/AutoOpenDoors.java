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

package com.specialeffect.eyemine.submod.misc;

import com.specialeffect.eyemine.platform.EyeMineConfig;
import com.specialeffect.eyemine.submod.IConfigListener;
import com.specialeffect.eyemine.submod.SubMod;
import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Iterator;
import java.util.LinkedList;

public class AutoOpenDoors extends SubMod implements IConfigListener {
	public final String MODID = "autoopendoors";

	public void onInitializeClient() {
		mOpenedDoors = new LinkedList<>();

		ClientTickEvent.CLIENT_PRE.register(this::onClientTick);
	}
	
	public void syncConfig() {
        mDoorRadius = EyeMineConfig.getRadiusDoors();
	}
	
	// A list of the position of any doors we've opened that haven't yet been closed
	private LinkedList<BlockPos> mOpenedDoors;

	private static int mDoorRadius = 3;
	private BlockPos mLastPlayerPos;

	public void onClientTick(Minecraft event) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = Minecraft.getInstance().player;
		if (player != null) {
			ClientLevel level = minecraft.level;
			BlockPos playerPos = player.blockPosition();

			// Only check/update doors when (integer) position has changed.
			if (mLastPlayerPos == null || mLastPlayerPos != playerPos) {
				mLastPlayerPos = playerPos;

				// Open any doors within 1 block
				synchronized (mOpenedDoors) {
					for (int x = -mDoorRadius; x <= mDoorRadius; x++) {
						for (int z = -mDoorRadius; z <= mDoorRadius; z++) {
							for (int y = -1; y <= 1; y++) { // look up/down for trapdoors
								BlockPos blockPos = playerPos.offset(x, y, z);

								// For symmetry with door closing, we actually want to test a circular
								// area, not a square.
								if (playerPos.distSqr(new Vec3i(blockPos.getX(), blockPos.getY(), blockPos.getZ())) <= mDoorRadius*mDoorRadius) {
									// Check if block is door, if so, activate it.
									BlockState state = level.getBlockState(blockPos);

									if (state.hasProperty(BlockStateProperties.OPEN)) {
										boolean isOpen = state.getValue(BlockStateProperties.OPEN);
										if (!isOpen) {
											// Ask the server to interact with the door to open
											Vec3 hitVec = new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ());
											Direction fakeDirection = Direction.DOWN;
											for(Direction dir : Direction.values()) {
												if(!level.getBlockState(blockPos.relative(dir)).isAir()) {
													fakeDirection = dir;
													break;
												}
											}
											BlockHitResult blockHitResult = new BlockHitResult(hitVec, fakeDirection, blockPos, true);

											InteractionResult result = state.use(level, player, InteractionHand.MAIN_HAND, blockHitResult);
											if (result.consumesAction()) {
												player.connection.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, blockHitResult));
											}
											mOpenedDoors.add(blockPos);
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
						if (playerPos.distSqr(new Vec3i(pos.getX(), pos.getY(), pos.getZ())) > closeRadius*closeRadius) {
							// Ask the server to interact with the door to close

							BlockState state = level.getBlockState(pos);
							if (state.hasProperty(BlockStateProperties.OPEN)) {
								Vec3 hitVec = new Vec3(pos.getX(), pos.getY(), pos.getZ());
								Direction fakeDirection = Direction.DOWN;
								for(Direction dir : Direction.values()) {
									if(!level.getBlockState(pos.relative(dir)).isAir()) {
										fakeDirection = dir;
										break;
									}
								}
								BlockHitResult blockHitResult = new BlockHitResult(hitVec, fakeDirection, pos, true);
								InteractionResult result = state.use(level, player, InteractionHand.MAIN_HAND, blockHitResult);
								if (result.consumesAction()) {
									player.connection.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, new BlockHitResult(hitVec, Direction.UP, pos, false)));
								}

								// Remove from list
								iterator.remove();
							}
						}
					}
				}
			}
		}
	}

}

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

import com.specialeffect.mods.ChildMod;
import com.specialeffect.utils.ModUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LadderBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class EasyLadderClimb extends ChildMod {
	public static final String MODID = "easyladderclimb";
	public static final String NAME = "EasyLadderClimb";

    //FIXME: re-visit this mod logic, test a bunch, does it solve any problems?    
		
	public void setup(final FMLCommonSetupEvent event) {
	}

	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {
    PlayerEntity player = Minecraft.getInstance().player;
    	if (null != player && event.phase == TickEvent.Phase.START) {
			World world = Minecraft.getInstance().world;

			if (player.isOnLadder()) {
				LOGGER.debug("LADDER");		
				BlockRayTraceResult rayTraceBlock = ModUtils.getMouseOverBlock();
		
				if (rayTraceBlock != null) {
					// FIXME: test for 1.14
					BlockPos blockPos = rayTraceBlock.getPos();	
					Block block = world.getBlockState(blockPos).getBlock();
					if (block instanceof LadderBlock) {
						
						LadderBlock ladder = (LadderBlock)block;
						
						
						BlockState state = world.getBlockState(blockPos);						
						Direction facing = (Direction) state.get(LadderBlock.FACING);
						Vec3d playerPos = player.getPositionVector();
						
						// Rotate player to face ladder.
						player.setPositionAndRotation(playerPos.x,
								playerPos.y, playerPos.z,
								getYawFromEnumFacing(facing), player.rotationPitch);
						LOGGER.debug("facing ladder");
					}
				
				
				}
			}
		}
	}

	private float getYawFromEnumFacing(Direction facing) {
		switch (facing) {
		case NORTH:
			return 0.0f;
		case EAST:
			return 90.0f;
		case SOUTH:
			return 180.0f;
		case WEST:
			return -90.0f;
		default:
			return 0.0f;
		}
	}
}

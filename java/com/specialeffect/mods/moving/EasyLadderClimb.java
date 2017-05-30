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

import com.specialeffect.utils.ModUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = EasyLadderClimb.MODID, version = ModUtils.VERSION, name = EasyLadderClimb.NAME)
public class EasyLadderClimb {
	public static final String MODID = "specialeffect.EasyLadderClimb";
	public static final String NAME = "EasyLadderClimb";

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(this);

		ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Automatically turn to face ladders, to simplify climbing with eye control.");
    	ModUtils.setAsParent(event, SpecialEffectMovements.MODID);

	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// Subscribe to event buses
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (ModUtils.entityIsMe(event.getEntityLiving())) {
			EntityPlayer player = (EntityPlayer) event.getEntityLiving();
			World world = Minecraft.getMinecraft().theWorld;

			if (event.getEntityLiving().isOnLadder()) {
				System.out.println("LADDER");
				MovingObjectPosition mov = Minecraft.getMinecraft().objectMouseOver;
				if (mov != null) {
					BlockPos blockPos = mov.getBlockPos(); // may still be null
															// if there's an
															// entity there
					if (blockPos != null) {

						Block block = world.getBlockState(blockPos).getBlock();
						IBlockState state = world.getBlockState(blockPos);
						state = state.getBlock().getActualState(state, world, blockPos);
						if (block instanceof BlockLadder) {
							BlockLadder ladder = (BlockLadder) block;
							EnumFacing facing = (EnumFacing) state.getProperties().get(ladder.FACING);
							Vec3 playerPos = player.getPositionVector();
							
							// Rotate player to face ladder.
							player.setPositionAndRotation(playerPos.xCoord,
									playerPos.yCoord, playerPos.zCoord,
									getYawFromEnumFacing(facing), player.rotationPitch);
						}
					}
				}
			}
		}
	}

	private float getYawFromEnumFacing(EnumFacing facing) {
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

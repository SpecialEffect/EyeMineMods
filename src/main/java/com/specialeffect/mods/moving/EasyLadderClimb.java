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

import com.specialeffect.mods.EyeGaze;
import com.specialeffect.utils.ModUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LadderBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(EasyLadderClimb.MODID)
public class EasyLadderClimb {
	public static final String MODID = "specialeffect.easyladderclimb";
	public static final String NAME = "EasyLadderClimb";

	public EasyLadderClimb() {
	    // Register methods on event bus
	    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);	    
	}
		
    private void setup(final FMLCommonSetupEvent event) {

		MinecraftForge.EVENT_BUS.register(this);

		ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Automatically turn to face ladders, to simplify climbing with eye control.");
    	ModUtils.setAsParent(event, EyeGaze.MODID);

	}

	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (ModUtils.entityIsMe(event.getEntityLiving())) {
			PlayerEntity player = (PlayerEntity) event.getEntityLiving();
			World world = Minecraft.getInstance().world;

			if (event.getEntityLiving().isOnLadder()) {
				System.out.println("LADDER");
				RayTraceResult mov = Minecraft.getInstance().objectMouseOver;
				if (mov != null) {
					// FIXME: test for 1.14
					if (mov.getType() == Type.BLOCK) {
					
						BlockPos blockPos = new BlockPos(mov.getHitVec()); 					
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
							System.out.println("facing ladder");
						}
					
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

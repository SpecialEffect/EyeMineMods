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

package com.specialeffect.mods.moving;

import java.awt.Color;

import com.irtimaled.bbor.client.renderers.AbstractRenderer;
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
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class EasyLadderClimb extends ChildMod {
	public final String MODID = "easyladderclimb";

    // This mod adds some logic to help slightly with climbing ladders with eye gaze
	// You'll get nudged toward the centre of the ladder a little bit.
		
	public void setup(final FMLCommonSetupEvent event) {
	}

	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {
    PlayerEntity player = Minecraft.getInstance().player;
    	if (null != player && event.phase == TickEvent.Phase.START) {
			World world = Minecraft.getInstance().world;

			if (player.isOnLadder() && MoveWithGaze.isWalking()) {
				BlockRayTraceResult rayTraceBlock = ModUtils.getMouseOverBlock();
				
				if (rayTraceBlock != null) { 
					BlockPos blockPos = rayTraceBlock.getPos();	
					Block block = world.getBlockState(blockPos).getBlock();														
					if (block instanceof LadderBlock) {
						
						BlockState state = world.getBlockState(blockPos);						
						Direction facing = (Direction) state.get(LadderBlock.FACING);
						Vector3d playerPos = player.getPositionVec();
						
						// What yaw would point the player at the middle of the ladder?						
						Vector3d midPos = getMidPointOfFace(blockPos,  facing);
						renderPos = midPos;				
						player.rotationYaw = player.rotationYaw % 360;
						
						double dx = midPos.x - playerPos.x;
						double dz = midPos.z - playerPos.z;
						double yawToMidPoint = -(180/Math.PI)*Math.atan2(dx,  dz);
						
						// Rotate player slightly towards the ideal yaw slightly
						double gain = 0.03f;
						double newYaw = safeInterpolate(player.rotationYaw, yawToMidPoint, gain);
						player.rotationYaw = (float) newYaw;						
					}
					
				}
			}
		}
	}
	
	private Vector3d getMidPointOfFace(BlockPos pos, Direction facing) {	
		// It's possible this logic is ladder-specific, since a ladder is a block which is mainly
		// 
		switch (facing) {
		case NORTH:
			return new Vector3d(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 1.0f);
		case EAST:
			return new Vector3d(pos.getX()       , pos.getY() + 0.5f, pos.getZ() + 0.5f);
		case SOUTH:
			return new Vector3d(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ()       );
		case WEST:
			return new Vector3d(pos.getX() + 1.0f, pos.getY() + 0.5f, pos.getZ() + 0.5f);
		default:
			return new Vector3d(pos.getX(), pos.getY(), pos.getZ());
		}
	}
	
	private Vector3d renderPos;
		
	@SubscribeEvent
	public void onBlockOutlineRender(DrawHighlightEvent e)
	{
		// Turn this on to debug the positional logic - it will render block positions for you 
		boolean debugRender = false;
		
		if (debugRender) {
					
			if (Minecraft.getInstance().currentScreen != null) {			
				return;
			}
							
			if (MoveWithGaze.isWalking()) {
				Color color = new Color(0.75f, 0.25f, 0.0f);
				int opacity = 255;
				double size = 0.05;
				AbstractRenderer.renderCubeAtPosition(renderPos, color, opacity, size);			
			}
		}
	}
	

	private void assertInterpolate(double expected, double angle1, double angle2, double amount) {
		double d = safeInterpolate(angle1, angle2, amount);
		if (Math.abs(d - expected) > 0.1 ) {
			d = safeInterpolate(angle1, angle2, amount );
		}
		double d1 = safeInterpolate(angle2, angle1, 1.0 - amount);
		if (Math.abs(d1 - expected) > 0.1 ) {
			d = safeInterpolate(angle2, angle1, 1.0 - amount);
		}
	}

	@SuppressWarnings("unused")
	private void testSafeInterpolate() {
		// In-place unit test for development
		double a = 0.5;
		double d = safeInterpolate(100, 200, a);
		
		assertInterpolate(125, 100, 200, 0.25);
		assertInterpolate(175, 200, 100, 0.25);
		
		assertInterpolate(150, 100, 200, a);
		assertInterpolate(125, 100, 200, 0.25);
		assertInterpolate(10, 350, 30, a);
		assertInterpolate(25, -50, 100, a);
		assertInterpolate(10, 720, 740, a);	
		
		assertInterpolate(269, 268, 270, 0.5);			
		assertInterpolate(269, 270, 268, 0.5);			
	}
	
	private double safeInterpolate(double angle1, double angle2, double amount) {
		// Interpolate between two angles by amount [0, 1]
		// while respecting angle-wrap		
		
		angle1 = (angle1 + 360) % 360;
		angle2 = (angle2 + 360) % 360;
				
		if (angle1 > angle2) {
			double tmp = angle1;
			angle1 = angle2;
			angle2 = tmp;
			amount = 1.0 - amount;
		}
        if (angle2 - angle1 > 180) {
        	angle2 -= 360;
        }
			
		double d = (angle2 - angle1)*amount;		
		double new_angle = angle1 + d;
		return (new_angle % 360);
	}
}

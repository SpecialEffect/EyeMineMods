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

package com.specialeffect.eyemine.submod.movement;

import com.irtimaled.bbor.client.renderers.AbstractRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.specialeffect.eyemine.client.EyeMineRenderType;
import com.specialeffect.eyemine.event.BlockOutlineEvent;
import com.specialeffect.eyemine.submod.SubMod;
import com.specialeffect.utils.ModUtils;
import me.shedaniel.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.awt.Color;

public class EasyLadderClimb extends SubMod {
	public final String MODID = "easyladderclimb";

    // This mod adds some logic to help slightly with climbing ladders with eye gaze
	// You'll get nudged toward the centre of the ladder a little bit.

	public void onInitializeClient() {
		ClientTickEvent.CLIENT_PRE.register(this::onClientTick);
		BlockOutlineEvent.OUTLINE.register(this::onBlockOutlineRender);
	}

	public void onClientTick(Minecraft minecraft) {
    	LocalPlayer player = minecraft.player;
    	if (player != null) {
			Level world = minecraft.level;

			if (player.onClimbable() && MoveWithGaze.isWalking()) {
				BlockHitResult rayTraceBlock = ModUtils.getMouseOverBlock();
				
				if (rayTraceBlock != null) { 
					BlockPos blockPos = rayTraceBlock.getBlockPos();
					Block block = world.getBlockState(blockPos).getBlock();
					if (block instanceof LadderBlock) {
						BlockState state = world.getBlockState(blockPos);
						Direction facing = (Direction) state.getValue(LadderBlock.FACING);
						Vec3 playerPos = player.position();
						
						// What yaw would point the player at the middle of the ladder?						
						Vec3 midPos = getMidPointOfFace(blockPos,  facing);
						renderPos = midPos;				
						player.yRot = player.yRot % 360;
						
						double dx = midPos.x - playerPos.x;
						double dz = midPos.z - playerPos.z;
						double yawToMidPoint = -(180/Math.PI)*Math.atan2(dx,  dz);
						
						// Rotate player slightly towards the ideal yaw slightly
						double gain = 0.03f;
						double newYaw = safeInterpolate(player.getY(), yawToMidPoint, gain);
						player.yRot = (float) newYaw;
					}
					
				}
			}
		}
	}
	
	private Vec3 getMidPointOfFace(BlockPos pos, Direction facing) {	
		// It's possible this logic is ladder-specific, since a ladder is a block which is mainly
		switch (facing) {
		case NORTH:
			return new Vec3(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 1.0f);
		case EAST:
			return new Vec3(pos.getX()       , pos.getY() + 0.5f, pos.getZ() + 0.5f);
		case SOUTH:
			return new Vec3(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ());
		case WEST:
			return new Vec3(pos.getX() + 1.0f, pos.getY() + 0.5f, pos.getZ() + 0.5f);
		default:
			return new Vec3(pos.getX(), pos.getY(), pos.getZ());
		}
	}
	
	private Vec3 renderPos;

	public InteractionResult onBlockOutlineRender(MultiBufferSource bufferSource, PoseStack poseStack) {
		Minecraft minecraft = Minecraft.getInstance();
		// Turn this on to debug the positional logic - it will render block positions for you 
		boolean debugRender = false;
		
		if (debugRender) {
			if (minecraft.screen != null) {
				return InteractionResult.PASS;
			}

			if (MoveWithGaze.isWalking()) {
				poseStack.pushPose();
				Color color = new Color(0.75f, 0.25f, 0.0f);
				int opacity = 255;
				double size = 0.05;
				final RenderType cubeType = EyeMineRenderType.cubeRenderType();
				VertexConsumer vertexConsumer = bufferSource.getBuffer(cubeType);
				AbstractRenderer.renderCubeAtPosition(poseStack, vertexConsumer, renderPos, color, opacity, size);

				if (bufferSource instanceof MultiBufferSource.BufferSource) {
					((MultiBufferSource.BufferSource) vertexConsumer).endBatch();
				}

				poseStack.popPose();
			}
		}
		return InteractionResult.PASS;
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

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

package com.specialeffect.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.TrapDoorBlock;
import net.minecraft.state.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class OpenableBlock {

	// Open block if not already open. Return if you actually opened it.
	public static boolean open(World world, Block block, BlockPos blockPos) {
	    if (!OpenableBlock.isOpenableBlock(block)) {
	    	return false;
	    }

	    BlockState state = world.getBlockState(blockPos);
	    BooleanProperty openProp = getOpenProp(block);
	    boolean isOpen = state.get(openProp);
        
	    if (!isOpen) {
	    	world.setBlockState(blockPos, state.with(openProp, 
	    					Boolean.valueOf(true))); 
	    	return true;
	    }

		return false;
	}

	private static BooleanProperty getOpenProp(Block block) {
	    if (block instanceof DoorBlock) {
	    	return DoorBlock.OPEN;
	    }
	    else if (block instanceof FenceGateBlock) {
	    	return FenceGateBlock.OPEN;
	    }
	    else if (block instanceof TrapDoorBlock) {
	    	return TrapDoorBlock.OPEN;
	    }
	    else {
	    	return null;
	    }
	}
	
	public static boolean close(World world, Block block, BlockPos blockPos) {
	    if (!OpenableBlock.isOpenableBlock(block)) {
	    	return false;
	    }

	    BlockState state = world.getBlockState(blockPos);
	    BooleanProperty openProp = getOpenProp(block);
	    boolean isOpen = state.get(openProp);

	    if (isOpen) {
	    	world.setBlockState(blockPos, state.with(openProp, 
	    					Boolean.valueOf(false))); 
	    	return true;
	    }

		return false;
	}
	
	public static boolean isOpenableBlock(Block block) {
		return (block instanceof FenceGateBlock) ||
			   (block instanceof DoorBlock) ||
			   (block instanceof TrapDoorBlock);
				
	}
}

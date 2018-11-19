/**
 * Copyright (C) 2016 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class OpenableBlock {

	// Open block if not already open. Return if you actually opened it.
	public static boolean open(World world, Block block, BlockPos blockPos) {
	    if (!OpenableBlock.isOpenableBlock(block)) {
	    	return false;
	    }

	    IBlockState state = world.getBlockState(blockPos);
	    PropertyBool openProp = getOpenProp(block);
	    boolean isOpen = (Boolean) state.getProperties().get(openProp);

	    if (!isOpen) {
	    	world.setBlockState(blockPos, 
	    			world.getBlockState(blockPos).withProperty(openProp, 
	    					Boolean.valueOf(true))); 
	    	return true;
	    }

		return false;
	}

	private static PropertyBool getOpenProp(Block block) {
	    if (block instanceof BlockDoor) {
	    	return BlockDoor.OPEN;
	    }
	    else if (block instanceof BlockFenceGate) {
	    	return BlockFenceGate.OPEN;
	    }
	    else if (block instanceof BlockTrapDoor) {
	    	return BlockTrapDoor.OPEN;
	    }
	    else {
	    	return null;
	    }
	}
	
	public static boolean close(World world, Block block, BlockPos blockPos) {
	    if (!OpenableBlock.isOpenableBlock(block)) {
	    	return false;
	    }

	    IBlockState state = world.getBlockState(blockPos);
	    PropertyBool openProp = getOpenProp(block);
	    boolean isOpen = (Boolean) state.getProperties().get(openProp);

	    if (isOpen) {
	    	world.setBlockState(blockPos, 
	    			world.getBlockState(blockPos).withProperty(openProp, 
	    					Boolean.valueOf(false))); 
	    	return true;
	    }

		return false;
	}
	
	public static boolean isOpenableBlock(Block block) {
		return (block instanceof BlockFenceGate) ||
			   (block instanceof BlockDoor) ||
			   (block instanceof BlockTrapDoor);
				
	}
}

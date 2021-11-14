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

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class OpenableBlock {

	// Open block if not already open. Return if you actually opened it.
	public static boolean open(Level level, Block block, BlockPos blockPos) {
	    if (!OpenableBlock.isOpenableBlock(block)) {
	    	return false;
	    }

	    BlockState state = level.getBlockState(blockPos);
	    if(state.hasProperty(BlockStateProperties.OPEN)) {
			boolean isOpen = state.getValue(BlockStateProperties.OPEN);

			if (!isOpen) {
				level.setBlockAndUpdate(blockPos, state.setValue(BlockStateProperties.OPEN, Boolean.TRUE));
				return true;
			}
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
	
	public static boolean close(Level level, Block block, BlockPos blockPos) {
	    if (!OpenableBlock.isOpenableBlock(block)) {
	    	return false;
	    }

	    BlockState state = level.getBlockState(blockPos);
		if(state.hasProperty(BlockStateProperties.OPEN)) {
			boolean isOpen = state.getValue(BlockStateProperties.OPEN);

			if (isOpen) {
				level.setBlockAndUpdate(blockPos, state.setValue(BlockStateProperties.OPEN, Boolean.FALSE));
				return true;
			}
		}

		return false;
	}
	
	public static boolean isOpenableBlock(Block block) {
		return (block instanceof FenceGateBlock) ||
			   (block instanceof DoorBlock) ||
			   (block instanceof TrapDoorBlock);
				
	}
}

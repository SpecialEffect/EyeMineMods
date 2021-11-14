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

package com.specialeffect.eyemine.submod.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;

public class TargetBlock {
	final BlockPos pos;
	final Direction direction;
	
	public TargetBlock(BlockHitResult res) {
		this.pos = res.getBlockPos();
		this.direction = res.getDirection();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;		
		result = prime * result + ((direction == null) ? 0 : direction.hashCode());
		result = prime * result + ((pos == null) ? 0 : pos.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TargetBlock other = (TargetBlock) obj;
		if (direction != other.direction)
			return false;
		if (pos == null) {
			return other.pos == null;
		} else {
			return pos.equals(other.pos);
		}
	}
}

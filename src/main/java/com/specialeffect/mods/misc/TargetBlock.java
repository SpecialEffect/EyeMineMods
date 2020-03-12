package com.specialeffect.mods.misc;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;

class TargetBlock {

	final BlockPos pos;
	final Direction direction;
	
	public TargetBlock(BlockRayTraceResult res) {
		this.pos = res.getPos();
		this.direction = res.getFace();
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
			if (other.pos != null)
				return false;
		} else if (!pos.equals(other.pos))
			return false;
		return true;
	}
}

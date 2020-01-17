/**
 * Copyright (C) 2016 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.messages;

import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class ActivateBlockAtPosition {
    
    private BlockPos blockPos;

    public ActivateBlockAtPosition() { }

    public ActivateBlockAtPosition(BlockPos pos) {
        this.blockPos = pos;
    }
           
	public static ActivateBlockAtPosition decode(PacketBuffer buf) {    	
        BlockPos blockPos = buf.readBlockPos();
        return new ActivateBlockAtPosition(blockPos);
    }

    public static void encode(ActivateBlockAtPosition pkt, PacketBuffer buf) {
    	BlockPos blockPos = pkt.blockPos;
    	buf.writeBlockPos(blockPos);       
    }

    public static class Handler {
		@SuppressWarnings("deprecation")
		public static void handle(final ActivateBlockAtPosition pkt, Supplier<NetworkEvent.Context> ctx) {
			PlayerEntity player = ctx.get().getSender();
	        if (player == null) {
	            return;
	        }       
	            
            World world = player.getEntityWorld();			
			BlockState state = world.getBlockState(pkt.blockPos);
			Block block = state.getBlock();			
						
			// NOTE this assumes hit is not used by onBlockActivated: could be a problem with some blocks 
			BlockRayTraceResult hit = null;  			
			
			// NOTE: should use state.onBlockActivated, but this requires non-null hit, so we suppress warning
			block.onBlockActivated(state, world, pkt.blockPos, player, Hand.MAIN_HAND, hit);  
			
			ctx.get().setPacketHandled(true);
		}
	}
}

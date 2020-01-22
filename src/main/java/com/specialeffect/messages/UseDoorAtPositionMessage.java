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

import com.specialeffect.utils.OpenableBlock;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class UseDoorAtPositionMessage {
    
    private BlockPos blockPos;
    private boolean toBeOpened;

    public UseDoorAtPositionMessage() { }

    public UseDoorAtPositionMessage(BlockPos pos, boolean toOpen) {
        this.blockPos = pos;
        this.toBeOpened= toOpen;
    }

    public static UseDoorAtPositionMessage decode(PacketBuffer buf) {
    	BlockPos blockPos = buf.readBlockPos();
    	boolean toOpen = buf.readBoolean();
    	return new UseDoorAtPositionMessage(blockPos, toOpen);
    }
    
    public static void encode(UseDoorAtPositionMessage pkt, PacketBuffer buf) {
        buf.writeBlockPos(pkt.blockPos);
        buf.writeBoolean(pkt.toBeOpened);
    }

    public static class Handler {
		public static void handle(final UseDoorAtPositionMessage pkt, Supplier<NetworkEvent.Context> ctx) {
			PlayerEntity player = ctx.get().getSender();
	        if (player == null) {
	            return;
	        }       

	        World world = player.getEntityWorld();
			Block block = world.getBlockState(pkt.blockPos).getBlock();
			if (pkt.toBeOpened) {
				OpenableBlock.open(world, block, pkt.blockPos);
			}
			else {
				OpenableBlock.close(world, block, pkt.blockPos);
			}
		}
	}             
}

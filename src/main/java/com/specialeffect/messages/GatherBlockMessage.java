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

import javax.xml.ws.handler.MessageContext;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class GatherBlockMessage {
    private int entityId = 0;

    public GatherBlockMessage() { }
    
    public GatherBlockMessage(int id) {
        this.entityId = id;
    }

    public static GatherBlockMessage decode(PacketBuffer buf) {
    	return new GatherBlockMessage(buf.readInt());
    }
    
    public static void encode(GatherBlockMessage pkt, PacketBuffer buf) {
        buf.writeInt(pkt.entityId);
    }

    public static class Handler {
		public static void handle(final GatherBlockMessage pkt, Supplier<NetworkEvent.Context> ctx) {
			PlayerEntity player = ctx.get().getSender();
	        if (player == null) {
	            return;
	        }       
	            
            World world = player.getEntityWorld();
            Entity target = world.getEntityByID(pkt.entityId);
            if(target != null && target instanceof ItemEntity) {
            	// Move item next to player to be picked up automatically
                target.setPosition(player.posX,player.posY+0.5,player.posZ);
            }
			
			ctx.get().setPacketHandled(true);
		}
	}
    
}




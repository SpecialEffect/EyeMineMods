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
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.network.NetworkEvent;

public class ChangeFlyingStateMessage {
    
    private boolean shouldBeFlying;
    private int flyHeight;

    public ChangeFlyingStateMessage() { }

    public ChangeFlyingStateMessage(boolean shouldBeFlying,
    								int flyHeight) {
        this.shouldBeFlying = shouldBeFlying;
        this.flyHeight = flyHeight;
    }
    

    public static ChangeFlyingStateMessage decode(PacketBuffer buf) {
    	boolean shouldBeFlying = buf.readBoolean();
    	int flyHeight = buf.readInt();
    	return new ChangeFlyingStateMessage(shouldBeFlying, flyHeight);
    }
    
    public static void encode(ChangeFlyingStateMessage pkt, PacketBuffer buf) {
        buf.writeBoolean(pkt.shouldBeFlying);
        buf.writeInt(pkt.flyHeight);
    }

    public static class Handler {
		public static void handle(final ChangeFlyingStateMessage pkt, Supplier<NetworkEvent.Context> ctx) {
			PlayerEntity player = ctx.get().getSender();
	        if (player == null) {
	            return;
	        }       	        

    		if (player.abilities.allowFlying) {
    			if (pkt.shouldBeFlying) {
    				player.abilities.isFlying = true;    				
    				Vec3d motion = player.getMotion();
    				Vec3d addMotion = new Vec3d(0, pkt.flyHeight, 0);    				
    				player.setMotion(motion.add(addMotion));
					player.move(MoverType.SELF, new Vec3d(0, pkt.flyHeight, 0));
    			}
    			else {
    				player.abilities.isFlying = false;
    			}
    		}
			
			ctx.get().setPacketHandled(true);
		}
	}    
}

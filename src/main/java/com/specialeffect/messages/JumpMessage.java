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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.network.NetworkEvent;

public class JumpMessage {
    
	private String playerName;
	
    public JumpMessage() { }

    public JumpMessage(String playerName) {
    	this.playerName = playerName;
    } 
    
	public static JumpMessage decode(PacketBuffer buf) {    	
        String playerName = buf.readString();        
        return new JumpMessage(playerName);
    }

    public static void encode(JumpMessage pkt, PacketBuffer buf) {
    	buf.writeString(pkt.playerName);    	      
    }
    
    public static class Handler {
		public static void handle(final JumpMessage pkt, Supplier<NetworkEvent.Context> ctx) {
			PlayerEntity player = ctx.get().getSender();
	        if (player == null) {
	            return;
	        }       
	        
	        System.out.println("jumping player " + player.getName());
			player.jump();
			ctx.get().setPacketHandled(true);
		}
	}          
}

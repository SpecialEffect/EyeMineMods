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

import javax.xml.ws.handler.MessageContext;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;

public class JumpMessage implements IMessage {
    
	private String playerName;
	
    public JumpMessage() { }

    public JumpMessage(String playerName) {
    	this.playerName = playerName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    	playerName = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
    	ByteBufUtils.writeUTF8String(buf, playerName);
    }

    public static class Handler implements IMessageHandler<JumpMessage, IMessage> {        
    	@Override
        public IMessage onMessage(final JumpMessage message,final MessageContext ctx) {
            IThreadListener mainThread = (WorldServer) ctx.getServerHandler().playerEntity.world; // or Minecraft.getInstance() on the client
//            mainThread.addScheduledTask(new Runnable() {
//                @Override
//                public void run() {
//                	System.out.println("jumping player " + message.playerName);
//                    World world = ctx.getServerHandler().playerEntity.world;
//                    EntityPlayer player = world.getPlayerEntityByName(message.playerName);
//					player.jump();
//                }
//            });
            mainThread.addScheduledTask(new Runnable() {
    			World world = ctx.getServerHandler().playerEntity.world;

    			@Override
    			public void run() {
    				EntityPlayer player = world.getPlayerEntityByName(message.playerName);
    				if (null != player) {
    					System.out.println("jumping player " + player.getName());
    					player.jump();
    				}
    			}
    		});
            return null; // no response in this case
        }
    }
}

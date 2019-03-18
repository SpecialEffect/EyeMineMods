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

public class SetPositionAndRotationMessage implements IMessage {
    
	private String playerName;
	private double x;
	private double y;
	private double z;
	private float yaw;
	private float pitch;
	
    public SetPositionAndRotationMessage() { }

    public SetPositionAndRotationMessage(String playerName, 
    									 double x, double y, double z,
    									 float yaw, float pitch) {
    	this.playerName = playerName;
    	this.x = x;
    	this.y = y;
    	this.z = z;
    	this.yaw = yaw;
    	this.pitch = pitch;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    	playerName = ByteBufUtils.readUTF8String(buf);
    	x = buf.readDouble();
    	y = buf.readDouble();
    	z = buf.readDouble();
    	yaw = buf.readFloat();
    	pitch = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
    	ByteBufUtils.writeUTF8String(buf, playerName);
    	buf.writeDouble(x);
    	buf.writeDouble(y);
    	buf.writeDouble(z);
    	buf.writeFloat(yaw);
    	buf.writeFloat(pitch);
    }

    public static class Handler implements IMessageHandler<SetPositionAndRotationMessage, IMessage> {        
    	@Override
        public IMessage onMessage(final SetPositionAndRotationMessage message,final MessageContext ctx) {
            IThreadListener mainThread = (WorldServer) ctx.getServerHandler().playerEntity.world; // or Minecraft.getMinecraft() on the client
            mainThread.addScheduledTask(new Runnable() {
            	World world = ctx.getServerHandler().playerEntity.world;
            	
                @Override
                public void run() {
                    EntityPlayer player = world.getPlayerEntityByName(message.playerName);
                    if (null != player) {
                    	player.setPositionAndRotation(message.x, message.y, message.z,
												  	  message.yaw, message.pitch);
                        player.setPositionAndUpdate(message.x, message.y, message.z);

                    }
                }
            });
            return null; // no response in this case
        }
    }
}

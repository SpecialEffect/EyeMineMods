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
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SendCommandMessage implements IMessage {
    private String command = "";

    public SendCommandMessage() { }
    
    public SendCommandMessage(String cmd) {
        this.command = cmd;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    	command = ByteBufUtils.readUTF8String(buf);
    }
    
    @Override
    public void toBytes(ByteBuf buf) {
    	ByteBufUtils.writeUTF8String(buf, command);
    }

    public static class Handler implements IMessageHandler<SendCommandMessage, IMessage> {        
    	@Override
        public IMessage onMessage(final SendCommandMessage message,final MessageContext ctx) {
            IThreadListener mainThread = (WorldServer) ctx.getServerHandler().playerEntity.world; // or Minecraft.getMinecraft() on the client
            mainThread.addScheduledTask(new Runnable() {
                @Override
                public void run() {

                	MinecraftServer server;
                	server = Minecraft.getMinecraft().world.getMinecraftServer();
                	if (null == server) { // this is for non-network worlds
                		server = Minecraft.getMinecraft().getIntegratedServer();
                	}
                	if (null == server) {
                		System.out.println("Server is null, cannot send command");
                		return;
                	}
                	else {
                		ICommandManager mgr = server.getCommandManager();
                		if (null == mgr) {
                    		System.out.println("CommandManager is null, cannot send command");
                			return;
                		}
                		else {
                			mgr.executeCommand(server, message.command);
                		}
                	}
                }
            });
            return null; // no response in this case
        }
    }
}




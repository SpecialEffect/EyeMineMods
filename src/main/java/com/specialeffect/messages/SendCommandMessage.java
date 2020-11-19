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

package com.specialeffect.messages;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.command.Commands;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.network.NetworkEvent;

public class SendCommandMessage {
	private String command;
	
    public SendCommandMessage() { }

    public SendCommandMessage(String command) {
    	this.command = command;
    } 
    
	public static SendCommandMessage decode(PacketBuffer buf) {    	
        String command = buf.readString();        
        return new SendCommandMessage(command);
    }

    public static void encode(SendCommandMessage pkt, PacketBuffer buf) {
    	buf.writeString(pkt.command);    	      
    }
    
    public static class Handler {
		public static void handle(final SendCommandMessage pkt, Supplier<NetworkEvent.Context> ctx) {
			MinecraftServer server;

        	server = Minecraft.getInstance().world.getServer();
        	if (null == server) { // this is for non-network worlds
        		server = Minecraft.getInstance().getIntegratedServer();
        	}
        	if (null == server) {
        		System.out.println("Server is null, cannot send command");
        		return;
        	}
        	else {
        		Commands mgr = server.getCommandManager();
        		if (null == mgr) {
            		System.out.println("CommandManager is null, cannot send command");
        			return;
        		}
        		else {        			
        			mgr.handleCommand(server.getCommandSource(), pkt.command);        			
        		}
        	}
		}
	}          
}


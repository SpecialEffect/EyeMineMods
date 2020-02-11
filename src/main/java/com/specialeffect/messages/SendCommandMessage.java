package com.specialeffect.messages;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
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
			PlayerEntity player = ctx.get().getSender();

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


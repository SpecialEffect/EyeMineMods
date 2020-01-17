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

import javax.annotation.Nullable;
import javax.xml.ws.handler.MessageContext;

import com.specialeffect.utils.ModUtils;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.network.NetworkEvent;

public class UseItemAtPositionMessage {
    
    private BlockPos blockPos;
    private String playerName;

    public UseItemAtPositionMessage() { }

    public UseItemAtPositionMessage(PlayerEntity player, BlockPos pos) {
    	this.playerName = player.getName().getString();
        this.blockPos = pos;
    }
    
    public UseItemAtPositionMessage(String playerName, BlockPos pos) {
    	this.playerName = playerName;
        this.blockPos = pos;
    }
    
    public static UseItemAtPositionMessage decode(PacketBuffer buf) {
    	BlockPos blockPos = buf.readBlockPos();
    	String playerName = buf.readString();
        return new UseItemAtPositionMessage(playerName, blockPos);
    }

    public static void encode(UseItemAtPositionMessage pkt, PacketBuffer buf) {
    	BlockPos blockPos = pkt.blockPos;
    	buf.writeBlockPos(blockPos);       
    	buf.writeString(pkt.playerName);
    }

    public static class Handler {
		@SuppressWarnings("deprecation")
		public static void handle(final UseItemAtPositionMessage pkt, Supplier<NetworkEvent.Context> ctx) {
			System.out.println("UseItemAtPositionMessage start");
			PlayerEntity player = ctx.get().getSender();
	        if (player == null) {
	            return;
	        }       

            World world = player.getEntityWorld();                        
            
			ItemStack item = player.getHeldItem(Hand.MAIN_HAND);					
			if (null != item)
			{
				int oldCount = item.getCount();
								
				Vec3d hitVec = new Vec3d((double)pkt.blockPos.getX(),
						(double)pkt.blockPos.getY(),
						(double)pkt.blockPos.getZ());
				
				Direction faceIn = Direction.UP;
				
				BlockRayTraceResult result = new BlockRayTraceResult(hitVec, faceIn, pkt.blockPos, false);
				ItemUseContext context = new ItemUseContext(player, Hand.MAIN_HAND, result);
				ActionResultType actionResult = item.onItemUse(context);
				
                if (actionResult != ActionResultType.SUCCESS)
                {
                	player.sendMessage(new StringTextComponent(
                			"Cannot place " + item.getDisplayName().getString() + " here"));
                }
//                //FIXME: look into reported bug here
                item.setCount(oldCount); //some items are decremented; others aren't
        	}
			
			System.out.println("UseItemAtPositionMessage end");
			
			
			ctx.get().setPacketHandled(true);
		}
	}
}

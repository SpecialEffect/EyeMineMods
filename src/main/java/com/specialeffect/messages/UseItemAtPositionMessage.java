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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class UseItemAtPositionMessage implements IMessage {
    
    private BlockPos blockPos;
    private String playerName;

    public UseItemAtPositionMessage() { }

    public UseItemAtPositionMessage(EntityPlayer player, BlockPos pos) {
    	this.playerName = player.getName();
        this.blockPos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    	playerName = ByteBufUtils.readUTF8String(buf);
        int x = ByteBufUtils.readVarInt(buf, 5); 
        int y = ByteBufUtils.readVarInt(buf, 5); 
        int z = ByteBufUtils.readVarInt(buf, 5); 
        blockPos = new BlockPos(x, y, z);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, playerName);
        ByteBufUtils.writeVarInt(buf, blockPos.getX(), 5);
        ByteBufUtils.writeVarInt(buf, blockPos.getY(), 5);
        ByteBufUtils.writeVarInt(buf, blockPos.getZ(), 5);       
    }

    public static class Handler implements IMessageHandler<UseItemAtPositionMessage, IMessage> {        
    	@Override
        public IMessage onMessage(final UseItemAtPositionMessage message,final MessageContext ctx) {
            IThreadListener mainThread = (WorldServer) ctx.getServerHandler().playerEntity.world; // or Minecraft.getMinecraft() on the client
            mainThread.addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    World world = ctx.getServerHandler().playerEntity.world;
                    EntityPlayer player = world.getPlayerEntityByName(message.playerName);
					ItemStack item = player.getHeldItem(EnumHand.MAIN_HAND);					
					if (null != item)
					{
						int oldCount = item.getCount();
	                    EnumActionResult result = 
	                    item.onItemUse(player, world, 
					                    	   message.blockPos, EnumHand.MAIN_HAND, 
					                    	   EnumFacing.UP, 
					                    	   0.0f, 0.0f, 0.0f);
	                    if (result != EnumActionResult.SUCCESS)
	                    {
	                    	player.sendMessage(new TextComponentString(
	                    			"Cannot place " + item.getDisplayName() + " here"));
	                    }
	                    item.setCount(oldCount); //some items are decremented; others aren't
                	}
                }
            });
            return null; // no response in this case
        }
    }
}

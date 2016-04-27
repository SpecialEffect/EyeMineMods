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
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PickBlockMessage implements IMessage {
    private int entityId = 0;

    public PickBlockMessage() { }
    
    public PickBlockMessage(int id) {
        this.entityId = id;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    	entityId = buf.readInt();
    }
    
    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityId);
    }

    public static class Handler implements IMessageHandler<PickBlockMessage, IMessage> {        
    	@Override
        public IMessage onMessage(final PickBlockMessage message,final MessageContext ctx) {
            IThreadListener mainThread = (WorldServer) ctx.getServerHandler().playerEntity.worldObj; // or Minecraft.getMinecraft() on the client
            mainThread.addScheduledTask(new Runnable() {
                @Override
                public void run() {
					if(message.entityId != 0) {
	                    EntityPlayer player = ctx.getServerHandler().playerEntity;
			            World world = player.getEntityWorld();
			            Entity target = world.getEntityByID(message.entityId);
			            if(target != null && target instanceof EntityItem) {
			            	// Move item next to player to be picked up automatically
			                target.setPosition(player.posX,player.posY+0.5,player.posZ);
			            }
			        }

                }
            });
            return null; // no response in this case
        }
    }
}




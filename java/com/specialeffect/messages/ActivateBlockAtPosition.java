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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ActivateBlockAtPosition implements IMessage {
    
    private BlockPos blockPos;

    public ActivateBlockAtPosition() { }

    public ActivateBlockAtPosition(BlockPos pos) {
        this.blockPos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int x = ByteBufUtils.readVarInt(buf, 5); 
        int y = ByteBufUtils.readVarInt(buf, 5); 
        int z = ByteBufUtils.readVarInt(buf, 5); 
        blockPos = new BlockPos(x, y, z);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeVarInt(buf, blockPos.getX(), 5);
        ByteBufUtils.writeVarInt(buf, blockPos.getY(), 5);
        ByteBufUtils.writeVarInt(buf, blockPos.getZ(), 5);       
    }

    public static class Handler implements IMessageHandler<ActivateBlockAtPosition, IMessage> {        
    	@Override
        public IMessage onMessage(final ActivateBlockAtPosition message,final MessageContext ctx) {
            IThreadListener mainThread = (WorldServer) ctx.getServerHandler().playerEntity.world; // or Minecraft.getMinecraft() on the client
            mainThread.addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    EntityPlayer player = ctx.getServerHandler().playerEntity;
                    World world = player.getEntityWorld();
					Block block = world.getBlockState(message.blockPos).getBlock();

					IBlockState state = world.getBlockState(message.blockPos);								    
					block.onBlockActivated(world, message.blockPos, state, player, 
							EnumFacing.NORTH, 0.5f, 0.5f, 0.5f);	

                }
            });
            return null; // no response in this case
        }
    }
}

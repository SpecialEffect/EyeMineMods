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
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class DismountPlayerMessage implements IMessage {
    
    public DismountPlayerMessage() { }

    public static class Handler implements IMessageHandler<DismountPlayerMessage, IMessage> {        
    	@Override
        public IMessage onMessage(final DismountPlayerMessage message,final MessageContext ctx) {

            IThreadListener mainThread = (WorldServer) ctx.getServerHandler().playerEntity.world; // or Minecraft.getMinecraft() on the client
            mainThread.addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    EntityPlayer player = ctx.getServerHandler().playerEntity;

					if (player.isRiding()) {
						Entity riddenEntity = player.getRidingEntity();
						if (null != riddenEntity) {

							player.dismountEntity(riddenEntity);
							riddenEntity.updateRidden();
							player.updateRidden();
							player.motionY += 0.5D;
						}
					}
                }
            });
            return null; // no response in this case
        }
    }

	@Override
	public void fromBytes(ByteBuf buf) {
	}

	@Override
	public void toBytes(ByteBuf buf) {
	}
}

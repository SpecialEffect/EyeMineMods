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

import javax.vecmath.Point2d;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MovePlayerMessage implements IMessage {
    
    private float moveAmount;
    private float moveAngle;

    public MovePlayerMessage() { }

    public MovePlayerMessage(float moveAmount,
    						 float moveAngle) {
    	this.moveAmount = moveAmount;
    	this.moveAngle = moveAngle;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    	moveAmount = buf.readFloat();
    	moveAngle = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
    	buf.writeFloat(moveAmount);
    	buf.writeFloat(moveAngle);
    }

    public static class Handler implements IMessageHandler<MovePlayerMessage, IMessage> {        
    	@Override
        public IMessage onMessage(final MovePlayerMessage message,final MessageContext ctx) {
            IThreadListener mainThread = (WorldServer) ctx.getServerHandler().playerEntity.world; // or Minecraft.getMinecraft() on the client
            mainThread.addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    EntityPlayer player = ctx.getServerHandler().playerEntity;
                    if (player.isRiding()) {
//                    	player.moveForward = 1.0f;
                    	Entity riddenEntity = player.getRidingEntity();
                    	
                    	
						if (null != riddenEntity) {
							final double scaleMinecart = 1.0d/8.0d;
							final double scaleAnimal = 1.0d/2.0d;
							final double scaleBoat = 1.0d/3.0d;
							
							if (riddenEntity instanceof EntityMinecart) {
								EntityMinecart minecart = (EntityMinecart)riddenEntity;
								message.moveAmount *= scaleMinecart;

								Vec3d lookVec = player.getLookVec();
								System.out.println(message.moveAmount);
								minecart.motionX = lookVec.xCoord*message.moveAmount;
								minecart.motionZ = lookVec.zCoord*message.moveAmount;
								minecart.moveMinecartOnRail(null);
								minecart.onUpdate();
							}
							else if (riddenEntity instanceof EntityAnimal) {
								EntityAnimal animal = (EntityAnimal)riddenEntity;
								message.moveAmount *= scaleAnimal;
								
								// Make sure riding doesn't hurt animal (this can happen
								// if you ride down a drop, or collide) 
								animal.setHealth(animal.getMaxHealth());
								
								double yaw = Math.toRadians(riddenEntity.rotationYaw);
								Point2d xyDiff = polarToCartesian(message.moveAmount, 
																  message.moveAngle + yaw);
								
								// don't drive animal forward while it goes over a cliff
								if (!riddenEntity.isAirBorne) {
									riddenEntity.move(MoverType.SELF, xyDiff.x, 0, xyDiff.y);
									riddenEntity.updateRidden();
								}
							}
							else if (riddenEntity instanceof EntityBoat) {
								message.moveAmount *= scaleBoat;
								double yaw = Math.toRadians(riddenEntity.rotationYaw);
								Point2d xyDiff = polarToCartesian(message.moveAmount, 
																  message.moveAngle + yaw);
								riddenEntity.move(MoverType.SELF, xyDiff.x, 0, xyDiff.y);
								riddenEntity.updateRidden();
							}
							else {
								// Not sure what else you can ride... this may need replacing 
								// with specialised behaviour 
								double yaw = Math.toRadians(riddenEntity.rotationYaw);
								Point2d xyDiff = polarToCartesian(message.moveAmount, 
																  message.moveAngle + yaw);
								riddenEntity.move(MoverType.SELF, xyDiff.x, 0, xyDiff.y);
								riddenEntity.updateRidden();
							}
						}
                    }
                }
            });
            return null; // no response in this case
        }
    }
    
    private static Point2d polarToCartesian(double amount, double angle) {
    	Point2d xy = new Point2d();
    	xy.x =  -(float)(amount*Math.sin(angle));
		xy.y = (float)(amount*Math.cos(angle));
		
		return xy;
    }
}

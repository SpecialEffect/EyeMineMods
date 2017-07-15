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
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class AddItemToHotbar implements IMessage {
    
    private ItemStack item;

    public AddItemToHotbar() { }

    public AddItemToHotbar(ItemStack item) {
        this.item = item;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        item = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeItemStack(buf, item);
    }

    public static class Handler implements IMessageHandler<AddItemToHotbar, IMessage> {        
    	@Override
        public IMessage onMessage(final AddItemToHotbar message,final MessageContext ctx) {
            IThreadListener mainThread = (WorldServer) ctx.getServerHandler().playerEntity.world; // or Minecraft.getMinecraft() on the client
            mainThread.addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    EntityPlayer player = ctx.getServerHandler().playerEntity;
                    InventoryPlayer inventory = player.inventory;
                    
                    // stick the item in an arbitrary non-hotbar slot, then let the inventory 
            		// figure out how best to move it to the hotbar (e.g. to an empty slot).
            		int slotId = 12; 
            		inventory.setInventorySlotContents(slotId, message.item);
            		inventory.pickItem(slotId);
                }
            });
            return null; // no response in this case
        }
    }
}

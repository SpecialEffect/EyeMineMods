package com.specialeffect.eyegazemod;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MyMessage implements IMessage {
    
    private String text;
    private ItemStack item;
    private BlockPos blockPos;

    public MyMessage() { }

    public MyMessage(String text, ItemStack item, BlockPos pos) {
        this.text = text;
        this.item = item;
        this.blockPos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        text = ByteBufUtils.readUTF8String(buf); 
        item = ByteBufUtils.readItemStack(buf);
        int x = ByteBufUtils.readVarInt(buf, 5); 
        int y = ByteBufUtils.readVarInt(buf, 5); 
        int z = ByteBufUtils.readVarInt(buf, 5); 
        blockPos = new BlockPos(x, y, z);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, text);
        ByteBufUtils.writeItemStack(buf, item);
        ByteBufUtils.writeVarInt(buf, blockPos.getX(), 5);
        ByteBufUtils.writeVarInt(buf, blockPos.getY(), 5);
        ByteBufUtils.writeVarInt(buf, blockPos.getZ(), 5);       
    }

    public static class Handler implements IMessageHandler<MyMessage, IMessage> {        
    	@Override
        public IMessage onMessage(final MyMessage message,final MessageContext ctx) {
            IThreadListener mainThread = (WorldServer) ctx.getServerHandler().playerEntity.worldObj; // or Minecraft.getMinecraft() on the client
            mainThread.addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    System.out.println(String.format("Received %s from %s", message.text, ctx.getServerHandler().playerEntity.getDisplayName()));
                    System.out.println(String.format("SERVER x = %d, y = %d, z = %d", 		
                    					message.blockPos.getX(), 
                    					message.blockPos.getY(),
                    					message.blockPos.getZ()));
                    
                    EntityPlayer player = ctx.getServerHandler().playerEntity;
                    World world = player.getEntityWorld();
                    
                    message.item.onItemUse(player, world, 
				                    	   message.blockPos, EnumFacing.UP, 
				                    	   0.0f, 0.0f, 0.0f);
                }
            });
            return null; // no response in this case
        }
    }
}

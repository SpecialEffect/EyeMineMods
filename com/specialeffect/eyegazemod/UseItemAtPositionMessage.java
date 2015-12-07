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

public class UseItemAtPositionMessage implements IMessage {
    
    private ItemStack item;
    private BlockPos blockPos;

    public UseItemAtPositionMessage() { }

    public UseItemAtPositionMessage(ItemStack item, BlockPos pos) {
        this.item = item;
        this.blockPos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        item = ByteBufUtils.readItemStack(buf);
        int x = ByteBufUtils.readVarInt(buf, 5); 
        int y = ByteBufUtils.readVarInt(buf, 5); 
        int z = ByteBufUtils.readVarInt(buf, 5); 
        blockPos = new BlockPos(x, y, z);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeItemStack(buf, item);
        ByteBufUtils.writeVarInt(buf, blockPos.getX(), 5);
        ByteBufUtils.writeVarInt(buf, blockPos.getY(), 5);
        ByteBufUtils.writeVarInt(buf, blockPos.getZ(), 5);       
    }

    public static class Handler implements IMessageHandler<UseItemAtPositionMessage, IMessage> {        
    	@Override
        public IMessage onMessage(final UseItemAtPositionMessage message,final MessageContext ctx) {
            IThreadListener mainThread = (WorldServer) ctx.getServerHandler().playerEntity.worldObj; // or Minecraft.getMinecraft() on the client
            mainThread.addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    EntityPlayer player = ctx.getServerHandler().playerEntity;
                    World world = player.getEntityWorld();
                    
                    message.item.onItemUse(player, world, 
				                    	   message.blockPos, EnumFacing.UP, 
				                    	   0.0f, 0.0f, 0.0f);
                    
                    // TODO: Deprecate item stack in survival mode?
                    // TODO: Animate item use?
                }
            });
            return null; // no response in this case
        }
    }
}

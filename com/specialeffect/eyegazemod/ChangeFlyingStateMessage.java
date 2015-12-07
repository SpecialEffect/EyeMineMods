package com.specialeffect.eyegazemod;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
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

public class ChangeFlyingStateMessage implements IMessage {
    
    private boolean shouldBeFlying;
    private int flyHeight;

    public ChangeFlyingStateMessage() { }

    public ChangeFlyingStateMessage(boolean shouldBeFlying,
    								int flyHeight) {
        this.shouldBeFlying = shouldBeFlying;
        this.flyHeight = flyHeight;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    	shouldBeFlying = ByteBufUtils.readVarShort(buf) > 0;
    	flyHeight = ByteBufUtils.readVarShort(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeVarShort(buf, shouldBeFlying ? 1 : 0);
        ByteBufUtils.writeVarShort(buf, flyHeight);
    }

    public static class Handler implements IMessageHandler<ChangeFlyingStateMessage, IMessage> {        
    	@Override
    	public IMessage onMessage(final ChangeFlyingStateMessage message,final MessageContext ctx) {
    		EntityPlayer player = ctx.getServerHandler().playerEntity;                    

    		if (player.capabilities.allowFlying) {
    			if (message.shouldBeFlying) {
    				player.capabilities.isFlying = true;
    				player.motionY += message.flyHeight;
    			}
    			else {
    				player.capabilities.isFlying = false;
    			}
    		}
            return null; // no response in this case
        }
    }
}

package com.specialeffect.mods;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.callbacks.IOnLiving;
import com.specialeffect.callbacks.SingleShotOnLivingCallback;
import com.specialeffect.utils.ModUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = EasyLadderClimb.MODID, version = EasyLadderClimb.VERSION, name = EasyLadderClimb.NAME)
public class EasyLadderClimb {
	public static final String MODID = "specialeffect.EasyLadderClimb";
	public static final String VERSION = "0.1";
	public static final String NAME = "EasyLadderClimb";

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(this);

		ModUtils.setupModInfo(event, this.MODID, this.VERSION, this.NAME,
				"Automatically turn to face ladders, to simplify climbing with eye control.");

	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// Subscribe to event buses
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (event.entityLiving instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.entityLiving;
			World world = Minecraft.getMinecraft().theWorld;

			if (event.entityLiving.isOnLadder()) {
				System.out.println("LADDER");
				MovingObjectPosition mov = Minecraft.getMinecraft().objectMouseOver;
				if (mov != null) {
					BlockPos blockPos = mov.getBlockPos(); // may still be null
															// if there's an
															// entity there
					if (blockPos != null) {

						Block block = world.getBlockState(blockPos).getBlock();
						IBlockState state = world.getBlockState(blockPos);
						state = state.getBlock().getActualState(state, world, blockPos);
						if (block instanceof BlockLadder) {
							BlockLadder ladder = (BlockLadder) block;
							EnumFacing facing = (EnumFacing) state.getProperties().get(ladder.FACING);
							Vec3 playerPos = player.getPositionVector();
							
							// Rotate player to face ladder.
							player.setPositionAndRotation(playerPos.xCoord,
									playerPos.yCoord, playerPos.zCoord,
									getYawFromEnumFacing(facing), player.rotationPitch);
						}
					}
				}
			}
		}
	}

	private float getYawFromEnumFacing(EnumFacing facing) {
		switch (facing) {
		case NORTH:
			return 0.0f;
		case EAST:
			return 90.0f;
		case SOUTH:
			return 180.0f;
		case WEST:
			return -90.0f;
		default:
			return 0.0f;
		}
	}
}

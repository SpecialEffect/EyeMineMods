package com.specialeffect.eyegazemod;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.lwjgl.input.Keyboard;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
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

@Mod(modid = SpecialEffectUseDoor.MODID, version = SpecialEffectUseDoor.VERSION, name = SpecialEffectUseDoor.NAME)
public class SpecialEffectUseDoor extends BaseClassWithCallbacks {
	public static final String MODID = "specialeffect.usedoor";
	public static final String VERSION = "0.1";
	public static final String NAME = "SpecialEffectUseDoor";

	private static KeyBinding mUseDoorKB;

    public static SimpleNetworkWrapper network;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(this);
		
        network = NetworkRegistry.INSTANCE.newSimpleChannel(this.NAME);
        network.registerMessage(UseDoorAtPositionMessage.Handler.class, 
        						UseDoorAtPositionMessage.class, 0, Side.SERVER);

	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// Subscribe to event buses
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);

		// Register key bindings
		mUseDoorKB = new KeyBinding("Open/close nearby door", Keyboard.KEY_R, "SpecialEffect");

	}

	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (event.entityLiving instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.entityLiving;

			// Process any events which were queued by key events
			this.processQueuedCallbacks(event);
		}
	}

	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {

		if (mUseDoorKB.isPressed()) {
			this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving() {
				@Override
				public void onLiving(LivingUpdateEvent event) {
					EntityPlayer player = (EntityPlayer) event.entityLiving;
					World world = Minecraft.getMinecraft().theWorld;

					BlockPos playerPos = player.getPosition();

					// Look for nearby doors to open/close
					for (int i = -2; i <= 2; i++) {
						for (int j = -2; j <= 2; j++) {

							BlockPos blockPos = playerPos.add(i, 0, j);

							// Check if block is door, if so, activate it.
							Block block = world.getBlockState(blockPos).getBlock();
							if (block instanceof BlockDoor) {
								BlockDoor door = (BlockDoor) block;
								IBlockState state = world.getBlockState(blockPos);
								door.onBlockActivated(world, blockPos, state, player, EnumFacing.NORTH, 0.0f, 0.0f, 0.0f);
								
								// Ask server to activate door too
					    		SpecialEffectUseDoor.network.sendToServer(
					    				new UseDoorAtPositionMessage(blockPos));
							}
						}
					}
				}
			}));
		}
	}

}

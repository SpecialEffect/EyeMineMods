package com.specialeffect.mods;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.lwjgl.input.Keyboard;

import com.specialeffect.messages.UseDoorAtPositionMessage;
import com.specialeffect.utils.ModUtils;
import com.specialeffect.utils.OpenableBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
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

@Mod(modid = AutoOpenDoors.MODID, version = ModUtils.VERSION, name = AutoOpenDoors.NAME,
guiFactory = "com.specialeffect.gui.GuiFactoryAutoDoors")
public class AutoOpenDoors {
	public static final String MODID = "specialeffect.autoopendoors";
	public static final String VERSION = "0.1";
	public static final String NAME = "AutoOpenDoors";
    public static Configuration mConfig;

    public static SimpleNetworkWrapper network;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(this);
		
		ModUtils.setupModInfo(event, this.MODID, this.VERSION, this.NAME,
				"Automatically open doors/gates and close them behind you.");
		
        network = NetworkRegistry.INSTANCE.newSimpleChannel(this.NAME);
        network.registerMessage(UseDoorAtPositionMessage.Handler.class, 
        						UseDoorAtPositionMessage.class, 0, Side.SERVER);

        // Set up config
    	mConfig = new Configuration(event.getSuggestedConfigurationFile());
    	this.syncConfig();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// Subscribe to event buses
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);

		mOpenedDoors = new LinkedList<BlockPos>();
	}
	
	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if(eventArgs.modID.equals(this.MODID)) {
			syncConfig();
		}
	}
	
	public static void syncConfig() {
        mDoorRadius = mConfig.getInt("Distance to open doors", Configuration.CATEGORY_GENERAL, mDoorRadius, 
        						1, 20, "How far away a player needs to be from a door to automatically open/close");
        if (mConfig.hasChanged()) {
        	mConfig.save();
        }
	}
	
	// A list of the position of any doors we've opened that haven't yet been closed
	private LinkedList<BlockPos> mOpenedDoors;

	private static int mDoorRadius = 3;
	private BlockPos mLastPlayerPos;
	
	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (event.entityLiving instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.entityLiving;
			World world = Minecraft.getMinecraft().theWorld;

			BlockPos playerPos = player.getPosition();

			// Only check/update doors when (integer) position has changed. 
			if (mLastPlayerPos == null || mLastPlayerPos != playerPos) {
				mLastPlayerPos = playerPos;

				// Open any doors within 1 block
				synchronized (mOpenedDoors) {
					for (int x = -mDoorRadius; x <= mDoorRadius; x++) {
						for (int z = -mDoorRadius; z <= mDoorRadius; z++) {
							for (int y = -1; y <= 1; y++) { // look up/down for trapdoors
								
								BlockPos blockPos = playerPos.add(x, y, z);

								// For symmetry with door closing, we actually want to test a circular
								// area, not a square.
								if (playerPos.distanceSq(new Vec3i(blockPos.getX(), blockPos.getY(), blockPos.getZ()))
										<= mDoorRadius*mDoorRadius) {

									// Check if block is door, if so, activate it.
									Block block = world.getBlockState(blockPos).getBlock();

									if (OpenableBlock.isOpenableBlock(block)) {
										boolean haveOpened = OpenableBlock.open(world, block, blockPos);
										if (haveOpened) {
											mOpenedDoors.add(blockPos);

											// Ask server to open door too
											AutoOpenDoors.network.sendToServer(
													new UseDoorAtPositionMessage(blockPos, true));
										}
									}
								}
							}
						}
					}
				}

				// Close any doors that you've left behind
				synchronized (mOpenedDoors) {
					for (Iterator<BlockPos> iterator = mOpenedDoors.iterator(); iterator.hasNext();) {
						BlockPos pos = iterator.next();

						if (playerPos.distanceSq(new Vec3i(pos.getX(), pos.getY(), pos.getZ())) > mDoorRadius*mDoorRadius) {
							Block block = world.getBlockState(pos).getBlock();

							OpenableBlock.close(world, block, pos);

							// Ask server to close door too
							AutoOpenDoors.network.sendToServer(
									new UseDoorAtPositionMessage(pos, false));

							// Remove from list
							iterator.remove();
						}
					}
				}
			}
			
		}
	}

}

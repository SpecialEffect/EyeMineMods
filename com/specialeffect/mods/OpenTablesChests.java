package com.specialeffect.mods;

import java.util.Iterator;
import java.util.Queue;

import org.lwjgl.input.Keyboard;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.callbacks.IOnLiving;
import com.specialeffect.callbacks.SingleShotOnLivingCallback;
import com.specialeffect.messages.ActivateBlockAtPosition;
import com.specialeffect.messages.ChangeFlyingStateMessage;
import com.specialeffect.utils.ModUtils;
import com.specialeffect.utils.OpenableBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;
import net.minecraft.world.ILockableContainer;
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
import scala.actors.threadpool.LinkedBlockingQueue;

@Mod(modid = OpenTablesChests.MODID, 
version = OpenTablesChests.VERSION,
name = OpenTablesChests.NAME)
public class OpenTablesChests extends BaseClassWithCallbacks
{

	public static final String MODID = "specialeffect.OpenTablesChests";
	public static final String VERSION = "0.1";
	public static final String NAME = "OpenTablesChests";

    public static Configuration mConfig;
	private static KeyBinding mOpenChestKB;
	private static KeyBinding mOpenCraftingTableKB;	
	
    public static SimpleNetworkWrapper network;
    
    private static int mRadius = 5;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {    
		FMLCommonHandler.instance().bus().register(this);    	
		
		ModUtils.setupModInfo(event, this.MODID, this.VERSION, this.NAME,
				"Add key bindings to open nearby chests/crafting tables.");

        network = NetworkRegistry.INSTANCE.newSimpleChannel(this.NAME);
        network.registerMessage(ActivateBlockAtPosition.Handler.class, 
        						ActivateBlockAtPosition.class, 0, Side.SERVER);
        
		// Set up config
    	mConfig = new Configuration(event.getSuggestedConfigurationFile());
    	this.syncConfig();
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		// Subscribe to event buses
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);    	

		// Register key bindings	
		mOpenChestKB = new KeyBinding("Open chest", Keyboard.KEY_LBRACKET, "SpecialEffect");
		mOpenCraftingTableKB = new KeyBinding("Open crafting table", Keyboard.KEY_RBRACKET, "SpecialEffect");
		ClientRegistry.registerKeyBinding(mOpenChestKB);
		ClientRegistry.registerKeyBinding(mOpenCraftingTableKB);
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if(eventArgs.modID.equals(this.MODID)) {
			syncConfig();
		}
	}
	
	public static void syncConfig() {
        mRadius = mConfig.getInt("Distance to open chests/crafting tables", Configuration.CATEGORY_GENERAL, mRadius, 
				1, 100, "How far away a player needs to be from a chest/table to be able to open it");
        if (mConfig.hasChanged()) {
        	mConfig.save();
        }
	}

	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if(event.entityLiving instanceof EntityPlayer) {
			this.processQueuedCallbacks(event);
		}			
	}
	
	// Search for closest block of a certain class, within maximum radius
	private static BlockPos findClosestBlockOfType(String className, EntityPlayer player, World world, int radius) {
		BlockPos playerPos = player.getPosition();		
	    Class classType;
    	BlockPos closestBlockPos = null;

		try {
			classType = Class.forName(className);
			
	    	// Find closest chest (within radius)
	    	double closestDistanceSq = Double.MAX_VALUE;
	    	for (int x = -radius; x <= radius; x++) {
	    		for (int z = -radius; z <= radius; z++) {
	    			for (int y = -1; y <= 1; y++) { // look up/down 1

	    				BlockPos blockPos = playerPos.add(x, y, z);

	    				// Check if block is appropriate class
	    				Block block = world.getBlockState(blockPos).getBlock();
	    				if (classType.isInstance(block)) {
	    					double distSq = playerPos.distanceSq(new Vec3i(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
	    					if (distSq < closestDistanceSq) {
	    						closestBlockPos = blockPos;
	    						closestDistanceSq = distSq;
	    					}
	    				}
	    			}
	    		}
	    	}
		} catch (ClassNotFoundException e) {
			System.out.println("Could not find class: " + className);
		}
	    return closestBlockPos;
	}

	
	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		if(mOpenChestKB.isPressed()) {
			this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving() {
				@Override
				public void onLiving(LivingUpdateEvent event) {
					EntityPlayer player = (EntityPlayer) event.entityLiving;
					World world = Minecraft.getMinecraft().theWorld;

					BlockPos closestBlockPos = OpenTablesChests.findClosestBlockOfType(
							BlockChest.class.getName(), player, world, mRadius);
					
					// Ask server to open 
					if (null == closestBlockPos) {
						player.addChatComponentMessage(new ChatComponentText(
								"No chests found in range"));
					}
					else {
						OpenTablesChests.network.sendToServer(
								new ActivateBlockAtPosition(closestBlockPos));
					}
				}
			}));
		}
		else if(mOpenCraftingTableKB.isPressed()) {
			this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving() {
				@Override
				public void onLiving(LivingUpdateEvent event) {
					EntityPlayer player = (EntityPlayer) event.entityLiving;
					World world = Minecraft.getMinecraft().theWorld;

					BlockPos closestBlockPos = OpenTablesChests.findClosestBlockOfType(
							BlockWorkbench.class.getName(), player, world, mRadius);

					// Ask server to open 
					if (null == closestBlockPos) {
						player.addChatComponentMessage(new ChatComponentText(
								"No crafting tables found in range"));
					}
					else {
						OpenTablesChests.network.sendToServer(
								new ActivateBlockAtPosition(closestBlockPos));
					}
				}
			}));
		}
	}

}
